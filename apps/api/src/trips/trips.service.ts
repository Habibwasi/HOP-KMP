import {
  Injectable,
  forwardRef,
  Inject,
  Logger,
} from '@nestjs/common'
import { AppException } from '../common/errors/app-exception'
import { ApiErrorCode } from '../common/errors/api-error-codes'
import { InjectQueue } from '@nestjs/bullmq'
import { Queue } from 'bullmq'
import { PrismaService } from '../prisma/prisma.service'
import { PricingService } from './pricing.service'
import { CreateTripDto } from './dto/create-trip.dto'
import { SearchTripsDto } from './dto/search-trips.dto'
import { UpdateTripDto } from './dto/update-trip.dto'
import { BookingStatus, TripModel, TripStatus } from '@prisma/client'
import {
  SEARCH_ALERTS_QUEUE,
  MATCH_ALERTS_JOB,
} from '../search-alerts/search-alerts.processor'
import { BOOKINGS_QUEUE, CHECK_THRESHOLD_JOB } from './trips.constants'
import { BookingsService } from '../bookings/bookings.service'
import { NotificationsService } from '../notifications/notifications.service'

/** Maps ISO day-abbreviation codes to JS getUTCDay() values (0 = Sunday). */
const DAY_CODE_TO_UTC_DOW: Record<string, number> = {
  SUN: 0,
  MON: 1,
  TUE: 2,
  WED: 3,
  THU: 4,
  FRI: 5,
  SAT: 6,
}

const expandAddressToken = (token: string): string[] => {
  const aliases: Record<string, string[]> = {
    copenhagen: ['kobenhavn', 'kbh'],
    kobenhavn: ['copenhagen', 'kbh'],
    kbh: ['kobenhavn', 'copenhagen'],
    aarhus: ['arhus'],
    arhus: ['aarhus'],
  }
  return [token, ...(aliases[token] ?? [])]
}

const normalizeSearchText = (value?: string): string[] => {
  const tokens = (value ?? '')
    .toLowerCase()
    .replace(/æ/g, 'ae')
    .replace(/ø/g, 'o')
    .replace(/å/g, 'a')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .split(/[^a-z0-9]+/)
    .filter((part) => part.length >= 2)

  return [...new Set(tokens.flatMap(expandAddressToken))]
}

const addressMatches = (address: string, query?: string): boolean => {
  const queryTokens = normalizeSearchText(query)
  if (queryTokens.length === 0) return true

  const addressTokens = normalizeSearchText(address)
  const addressText = addressTokens.join(' ')
  return queryTokens.some((token) =>
    addressText.includes(token) ||
      addressTokens.some((addressToken) => addressToken.includes(token) || token.includes(addressToken)),
  )
}

/** Generate UTC Date objects for every occurrence of the given days-of-week
 *  within the next `windowDays` days, preserving the time from `anchorDate`. */
function buildRecurringDates(
  anchorDate: Date,
  dayCodes: string[],
  windowDays: number,
): Date[] {
  const targetDows = new Set(
    dayCodes.map((d) => DAY_CODE_TO_UTC_DOW[d.toUpperCase()]).filter((n) => n !== undefined),
  )
  const hours = anchorDate.getUTCHours()
  const minutes = anchorDate.getUTCMinutes()
  const dates: Date[] = []

  const cursor = new Date(
    Date.UTC(
      anchorDate.getUTCFullYear(),
      anchorDate.getUTCMonth(),
      anchorDate.getUTCDate(),
      hours,
      minutes,
      0,
      0,
    ),
  )

  for (let i = 0; i <= windowDays; i++) {
    if (targetDows.has(cursor.getUTCDay())) {
      dates.push(new Date(cursor))
    }
    cursor.setUTCDate(cursor.getUTCDate() + 1)
  }

  return dates
}

@Injectable()
export class TripsService {
  private readonly logger = new Logger(TripsService.name)

  constructor(
    private prisma: PrismaService,
    private pricing: PricingService,
    @InjectQueue(SEARCH_ALERTS_QUEUE) private alertsQueue: Queue,
    @InjectQueue(BOOKINGS_QUEUE) private bookingsQueue: Queue,
    @Inject(forwardRef(() => BookingsService)) private bookings: BookingsService,
    private notifications: NotificationsService,
  ) {}

  async create(driverId: string, dto: CreateTripDto) {
    // Ensure driver has a MobilePay number before publishing any trip
    const driver = await this.prisma.user.findUnique({ where: { id: driverId } })
    if (!driver?.mobilepayNumber) {
      throw new AppException(ApiErrorCode.MOBILEPAY_MISSING)
    }

    // Validate Model B requirements
    if (dto.model === TripModel.B) {
      if (!dto.minPassengers) {
        throw new AppException(ApiErrorCode.MODEL_B_MISSING_MIN_PASSENGERS)
      }
      if (!dto.thresholdDeadline) {
        throw new AppException(ApiErrorCode.MODEL_B_MISSING_DEADLINE)
      }
      const deadline = new Date(dto.thresholdDeadline)
      const departure = new Date(dto.departureAt)
      if (deadline >= departure) {
        throw new AppException(ApiErrorCode.MODEL_B_DEADLINE_AFTER_DEPARTURE)
      }
    }

    // Model A: validate recurringDays present
    if (dto.model === TripModel.A) {
      if (!dto.recurringDays || dto.recurringDays.length === 0) {
        throw new AppException(ApiErrorCode.MODEL_A_MISSING_RECURRING_DAYS)
      }
    }

    // Prefer the client-provided driving route distance so the stored price
    // matches the driver review screen. Fall back to haversine for older clients.
    const distanceKm = dto.distanceMetres
      ? dto.distanceMetres / 1000
      : this.pricing.calculateDistance(
          dto.originLat,
          dto.originLng,
          dto.destLat,
          dto.destLng,
        )
    const pricePerSeat = this.pricing.calculatePricePerSeat(distanceKm, dto.seats)

    const baseData = {
      driverId,
      model: dto.model,
      originLat: dto.originLat,
      originLng: dto.originLng,
      originAddress: dto.originAddress,
      destLat: dto.destLat,
      destLng: dto.destLng,
      destAddress: dto.destAddress,
      seats: dto.seats,
      pricePerSeat,
      distanceKm,
      isRecurring: dto.model === TripModel.A,
      recurringDays: dto.recurringDays ?? [],
      windowDays: dto.windowDays ?? 30,
      minPassengers: dto.minPassengers,
      thresholdDeadline: dto.thresholdDeadline ? new Date(dto.thresholdDeadline) : null,
    }

    // ── Model A: generate one trip instance per occurrence in driver-chosen window ──
    if (dto.model === TripModel.A) {
      const anchor = new Date(dto.departureAt)
      const dates = buildRecurringDates(anchor, dto.recurringDays!, dto.windowDays ?? 30)

      if (dates.length === 0) {
        throw new AppException(ApiErrorCode.NO_OCCURRENCES)
      }

      const instances = dates.map((d) => ({ ...baseData, departureAt: d }))

      await this.prisma.trip.createMany({ data: instances })

      // Sync PostGIS geography columns for newly created instances.
      // Filter by driverId + originAddress + destAddress to avoid cross-contaminating
      // a concurrent batch with the same origin but a different destination.
      await this.prisma.$executeRaw`
        UPDATE "Trip"
        SET origin_geo = extensions.ST_SetSRID(extensions.ST_MakePoint(${dto.originLng}, ${dto.originLat}), 4326)::extensions.geography,
            dest_geo   = extensions.ST_SetSRID(extensions.ST_MakePoint(${dto.destLng},   ${dto.destLat}),   4326)::extensions.geography
        WHERE "driverId" = ${driverId}
          AND "originAddress" = ${dto.originAddress}
          AND "destAddress"   = ${dto.destAddress}
          AND origin_geo IS NULL
      `

      // Fetch the first created instance to return a consistent response shape
      const firstTrip = await this.prisma.trip.findFirst({
        where: {
          driverId,
          originAddress: dto.originAddress,
          destAddress: dto.destAddress,
          isRecurring: true,
          departureAt: dates[0],
        },
        include: {
          driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
        },
      })

      if (firstTrip) {
        await this.alertsQueue.add(MATCH_ALERTS_JOB, {
          tripId: firstTrip.id,
          originAddress: firstTrip.originAddress,
          destAddress: firstTrip.destAddress,
        })
      } else {
        // Trips were created by createMany but findFirst returned null.
        // This is a data-integrity inconsistency (e.g. a race between createMany
        // and findFirst on a busy DB). Log it so it can be investigated; the trips
        // exist but the MATCH_ALERTS_JOB will not fire for this batch.
        this.logger.error(
          `[TripsService] Model A trips created for driver ${driverId} but findFirst returned null — MATCH_ALERTS_JOB not queued.`,
        )
      }

      return firstTrip
    }

    // ── Model B: single trip instance ────────────────────────────────────────
    // create + geo sync run atomically: if the geo UPDATE fails the trip row is
    // rolled back, preventing an orphaned row that would silently vanish from
    // coordinate-based search results.
    const trip = await this.prisma.$transaction(async (tx) => {
      const created = await tx.trip.create({
        data: {
          ...baseData,
          departureAt: new Date(dto.departureAt),
        },
        include: { driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
      })
      await tx.$executeRaw`
        UPDATE "Trip"
        SET origin_geo = extensions.ST_SetSRID(extensions.ST_MakePoint(${dto.originLng}, ${dto.originLat}), 4326)::extensions.geography,
            dest_geo   = extensions.ST_SetSRID(extensions.ST_MakePoint(${dto.destLng},   ${dto.destLat}),   4326)::extensions.geography
        WHERE id = ${created.id}
      `
      return created
    })

    // Enqueue alert-matching as a fire-and-forget background job.
    await this.alertsQueue.add(MATCH_ALERTS_JOB, {
      tripId: trip.id,
      originAddress: trip.originAddress,
      destAddress: trip.destAddress,
    })

    // For Model B: schedule the threshold check at the deadline.
    // Uses a stable jobId so subsequent booking-creation attempts
    // to enqueue the same job are deduplicated by BullMQ.
    if (trip.thresholdDeadline) {
      const delay = new Date(trip.thresholdDeadline).getTime() - Date.now()
      if (delay > 0) {
        await this.bookingsQueue.add(
          CHECK_THRESHOLD_JOB,
          { tripId: trip.id },
          { delay, jobId: `threshold-${trip.id}`, removeOnComplete: true },
        )
      }
    }

    return trip
  }

  /**
   * Extends the rolling instance window for all active recurring (Model A) trips.
   * Called daily by the trips queue repeatable job.
   * Finds routes whose latest future instance is within the next 7 days and
   * creates new instances to bring the window back to each route's windowDays.
   */
  async extendRecurringWindow() {
    const now = new Date()

    // Fetch all future recurring trips grouped by route key
    const futureTrips = await this.prisma.trip.findMany({
      where: {
        isRecurring: true,
        departureAt: { gte: now },
        status: { not: TripStatus.CANCELLED },
      },
    })

    // Group by driverId + originAddress + destAddress
    const groups = new Map<string, typeof futureTrips>()
    for (const trip of futureTrips) {
      const key = `${trip.driverId}||${trip.originAddress}||${trip.destAddress}`
      if (!groups.has(key)) groups.set(key, [])
      groups.get(key)!.push(trip)
    }

    for (const [, trips] of groups) {
      const template = trips[0]
      const maxDate = trips.reduce(
        (mx, t) => (t.departureAt > mx ? t.departureAt : mx),
        trips[0].departureAt,
      )

      // Only extend if window is running short (< 7 days remaining)
      const daysRemaining = (maxDate.getTime() - now.getTime()) / (24 * 60 * 60 * 1000)
      if (daysRemaining >= 7) continue

      const tripWindowDays = (template as any).windowDays ?? 30
      const horizon = new Date(now.getTime() + tripWindowDays * 24 * 60 * 60 * 1000)

      // Generate new dates from the day after maxDate up to horizon
      const extendAnchor = new Date(maxDate.getTime() + 24 * 60 * 60 * 1000)
      const newDates = buildRecurringDates(extendAnchor, template.recurringDays, tripWindowDays)

      if (newDates.length === 0) continue

      const newInstances = newDates
        .filter((d) => d <= horizon)
        .map((d) => ({
          driverId: template.driverId,
          model: template.model,
          originLat: template.originLat,
          originLng: template.originLng,
          originAddress: template.originAddress,
          destLat: template.destLat,
          destLng: template.destLng,
          destAddress: template.destAddress,
          departureAt: d,
          seats: template.seats,
          pricePerSeat: template.pricePerSeat,
          distanceKm: template.distanceKm,
          isRecurring: true,
          recurringDays: template.recurringDays,
          windowDays: (template as any).windowDays ?? 30,
        }))

      if (newInstances.length > 0) {
        await this.prisma.trip.createMany({ data: newInstances, skipDuplicates: true })
        await this.prisma.$executeRaw`
          UPDATE "Trip"
          SET origin_geo = extensions.ST_SetSRID(extensions.ST_MakePoint(${template.originLng}, ${template.originLat}), 4326)::extensions.geography,
              dest_geo   = extensions.ST_SetSRID(extensions.ST_MakePoint(${template.destLng},   ${template.destLat}),   4326)::extensions.geography
          WHERE "driverId" = ${template.driverId}
            AND "originAddress" = ${template.originAddress}
            AND "destAddress"   = ${template.destAddress}
            AND origin_geo IS NULL
        `
      }
    }
  }

  async search(dto: SearchTripsDto) {
    const radiusKm = dto.radiusKm ?? 5
    const seats = dto.seats ?? 1

    const date = new Date(dto.date)
    const dayStart = new Date(date)
    dayStart.setHours(0, 0, 0, 0)
    const dayEnd = new Date(date)
    dayEnd.setHours(23, 59, 59, 999)

    const useCoordinates =
      dto.originLat != null &&
      dto.originLng != null &&
      dto.destLat != null &&
      dto.destLng != null

    // ── Coordinate-based search: PostGIS ST_DWithin ───────────────────────────
    if (useCoordinates) {
      const radiusMetres = radiusKm * 1000

      type TripSearchRow = {
        id: string; driverId: string; model: string
        originLat: number; originLng: number; originAddress: string
        destLat: number; destLng: number; destAddress: string
        departureAt: Date; seats: number; pricePerSeat: number
        distanceKm: number | null; status: string
        isActive: boolean; isRecurring: boolean; recurringDays: string[]
        minPassengers: number | null; thresholdDeadline: Date | null
        createdAt: Date; updatedAt: Date
        driver: { id: string; firstName: string; lastName: string; avatarUrl: string | null }
        bookedSeats: bigint
      }

      const rows = await this.prisma.$queryRaw<TripSearchRow[]>`
        SELECT t.id, t."driverId", t.model,
               t."originLat", t."originLng", t."originAddress",
               t."destLat",   t."destLng",   t."destAddress",
               t."departureAt", t.seats, t."pricePerSeat",
               t."distanceKm", t.status, t."isActive",
               t."isRecurring", t."recurringDays",
               t."minPassengers", t."thresholdDeadline",
               t."createdAt", t."updatedAt",
               json_build_object(
                 'id',        u.id,
                 'firstName', u."firstName",
                 'lastName',  u."lastName",
                 'avatarUrl', u."avatarUrl"
               ) AS driver,
               COALESCE((
                 SELECT SUM(b.seats)
                 FROM "Booking" b
                 WHERE b."tripId" = t.id AND b.status = 'CONFIRMED'
               ), 0) AS "bookedSeats"
        FROM "Trip" t
        JOIN "User" u ON u.id = t."driverId"
        WHERE t.status = 'ACTIVE'
          AND t."departureAt" BETWEEN ${dayStart} AND ${dayEnd}
          AND t.seats >= ${seats}
          AND t.origin_geo IS NOT NULL
          AND extensions.ST_DWithin(
                t.origin_geo,
                extensions.ST_SetSRID(extensions.ST_MakePoint(${dto.originLng!}, ${dto.originLat!}), 4326)::extensions.geography,
                ${radiusMetres}
              )
          AND extensions.ST_DWithin(
                t.dest_geo,
                extensions.ST_SetSRID(extensions.ST_MakePoint(${dto.destLng!}, ${dto.destLat!}), 4326)::extensions.geography,
                ${radiusMetres}
              )
      `

      return rows
        .filter((row) => (row.seats - Number(row.bookedSeats)) >= seats)
        .map((row) => ({ ...row, availableSeats: row.seats - Number(row.bookedSeats), bookedSeats: undefined }))
    }

    // ── Text-based search (no coordinates): unchanged behaviour ───────────────
    const where: any = {
      status: TripStatus.ACTIVE,
      departureAt: { gte: dayStart, lte: dayEnd },
      seats: { gte: seats },
    }

    // Fetch active trips on requested date. Text route matching is applied
    // in memory below so Danish character folding and city aliases work
    // consistently across database collations.
    const trips = await this.prisma.trip.findMany({
      where,
      include: {
        driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
        bookings: { where: { status: 'CONFIRMED' } },
      },
    })

    return trips
      .filter((trip) => {
        const bookedSeats = trip.bookings.reduce((sum, b) => sum + b.seats, 0)
        const availableSeats = trip.seats - bookedSeats
        return availableSeats >= seats &&
          addressMatches(trip.originAddress, dto.origin) &&
          addressMatches(trip.destAddress, dto.dest)
      })
      .map((trip) => {
        const bookedSeats = trip.bookings.reduce((sum, b) => sum + b.seats, 0)
        return {
          ...trip,
          availableSeats: trip.seats - bookedSeats,
          bookings: undefined,
        }
      })
  }

  async findById(id: string) {
    const trip = await this.prisma.trip.findUnique({
      where: { id },
      include: {
        driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
        bookings: {
          where: { status: { in: ['PENDING', 'CONFIRMED'] } },
          include: { passenger: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
        },
      },
    })
    if (!trip) throw new AppException(ApiErrorCode.TRIP_NOT_FOUND)
    return trip
  }

  async getTripPassengers(tripId: string, driverId: string) {
    const trip = await this.prisma.trip.findUnique({ where: { id: tripId } })
    if (!trip) throw new AppException(ApiErrorCode.TRIP_NOT_FOUND)
    if (trip.driverId !== driverId) throw new AppException(ApiErrorCode.NOT_YOUR_TRIP)

    const bookings = await this.prisma.booking.findMany({
      where: {
        tripId,
        status: { in: [BookingStatus.PENDING, BookingStatus.CONFIRMED] },
      },
      include: {
        passenger: {
          select: {
            id: true,
            firstName: true,
            lastName: true,
            avatarUrl: true,
            ratingsReceived: { select: { score: true } },
          },
        },
      },
    })

    return bookings.map((b) => {
      const scores = b.passenger.ratingsReceived.map((r) => r.score)
      const avgRating =
        scores.length > 0
          ? Math.round((scores.reduce((s, r) => s + r, 0) / scores.length) * 10) / 10
          : 0
      return {
        bookingId: b.id,
        passengerId: b.passenger.id,
        fullName: `${b.passenger.firstName} ${b.passenger.lastName}`.trim(),
        rating: avgRating,
        seats: b.seats,
        avatarUrl: b.passenger.avatarUrl ?? null,
      }
    })
  }

  /**
   * Stops a recurring (Model A) route entirely.
   * Cancels every future instance that shares the same driver + origin + destination,
   * notifies all passengers, and marks them isRecurring=false so the rolling-window
   * extension job never recreates them.
   */
  async stopRecurring(tripId: string, userId: string) {
    const trip = await this.prisma.trip.findUnique({ where: { id: tripId } })
    if (!trip) throw new AppException(ApiErrorCode.TRIP_NOT_FOUND)
    if (trip.driverId !== userId) throw new AppException(ApiErrorCode.NOT_YOUR_TRIP)
    if (!trip.isRecurring) throw new AppException(ApiErrorCode.TRIP_NOT_FOUND)

    const now = new Date()
    const futureInstances = await this.prisma.trip.findMany({
      where: {
        driverId: userId,
        originAddress: trip.originAddress,
        destAddress: trip.destAddress,
        isRecurring: true,
        departureAt: { gte: now },
        status: { not: TripStatus.CANCELLED },
      },
    })

    if (futureInstances.length > 0) {
      const ids = futureInstances.map((t) => t.id)
      await this.prisma.trip.updateMany({
        where: { id: { in: ids } },
        data: { status: TripStatus.CANCELLED, isActive: false, isRecurring: false },
      })
      for (const instance of futureInstances) {
        await this.bookings.cancelAllForTrip(instance.id).catch((err) => {
          this.logger.error(`[Trips] cancelAllForTrip failed for trip ${instance.id}: ${err?.message}`)
        })
      }
    }

    return { stopped: true, cancelledCount: futureInstances.length }
  }

  async cancel(tripId: string, userId: string) {
    const trip = await this.prisma.trip.findUnique({ where: { id: tripId } })
    if (!trip) throw new AppException(ApiErrorCode.TRIP_NOT_FOUND)
    if (trip.driverId !== userId) throw new AppException(ApiErrorCode.NOT_YOUR_TRIP)
    if (trip.status === TripStatus.CANCELLED) {
      throw new AppException(ApiErrorCode.TRIP_ALREADY_CANCELLED)
    }

    await this.prisma.trip.update({
      where: { id: tripId },
      data: { status: TripStatus.CANCELLED, isActive: false },
    })

    // Cancel all active bookings and notify passengers
    await this.bookings.cancelAllForTrip(tripId).catch((err) => {
      this.logger.error(`[Trips] cancelAllForTrip failed for trip ${tripId}: ${err?.message}`)
    })

    return { cancelled: true }
  }

  /**
   * Update an upcoming trip's route or departure time.
   * Only allowed when there are no confirmed or pending bookings (seatsBooked == 0).
   */
  async update(tripId: string, userId: string, dto: UpdateTripDto) {
    const trip = await this.prisma.trip.findUnique({
      where: { id: tripId },
      include: {
        bookings: { where: { status: { in: [BookingStatus.PENDING, BookingStatus.CONFIRMED] } } },
      },
    })
    if (!trip) throw new AppException(ApiErrorCode.TRIP_NOT_FOUND)
    if (trip.driverId !== userId) throw new AppException(ApiErrorCode.NOT_YOUR_TRIP)
    if (trip.status === TripStatus.CANCELLED || trip.status === TripStatus.COMPLETED) {
      throw new AppException(ApiErrorCode.TRIP_NOT_ACTIVE_FOR_COMPLETE)
    }

    const seatsBooked = trip.bookings.reduce((sum, b) => sum + b.seats, 0)
    if (seatsBooked > 0) {
      throw new AppException(ApiErrorCode.TRIP_HAS_BOOKINGS)
    }

    const newOriginLat = dto.originLat ?? trip.originLat
    const newOriginLng = dto.originLng ?? trip.originLng
    const newDestLat = dto.destLat ?? trip.destLat
    const newDestLng = dto.destLng ?? trip.destLng
    const newOriginAddress = dto.originAddress ?? trip.originAddress
    const newDestAddress = dto.destAddress ?? trip.destAddress
    const newDepartureAt = dto.departureAt ? new Date(dto.departureAt) : trip.departureAt

    const distanceKm = dto.distanceMetres
      ? dto.distanceMetres / 1000
      : (dto.originLat != null || dto.destLat != null)
        ? this.pricing.calculateDistance(newOriginLat, newOriginLng, newDestLat, newDestLng)
        : trip.distanceKm ?? 0
    const pricePerSeat = this.pricing.calculatePricePerSeat(distanceKm, trip.seats)

    const routeChanged =
      dto.originLat != null || dto.originLng != null ||
      dto.destLat != null || dto.destLng != null ||
      dto.originAddress != null || dto.destAddress != null

    const updated = await this.prisma.$transaction(async (tx) => {
      const result = await tx.trip.update({
        where: { id: tripId },
        data: {
          originLat: newOriginLat,
          originLng: newOriginLng,
          originAddress: newOriginAddress,
          destLat: newDestLat,
          destLng: newDestLng,
          destAddress: newDestAddress,
          departureAt: newDepartureAt,
          distanceKm,
          pricePerSeat,
        },
        include: { driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
      })
      if (routeChanged) {
        await tx.$executeRaw`
          UPDATE "Trip"
          SET origin_geo = extensions.ST_SetSRID(extensions.ST_MakePoint(${newOriginLng}, ${newOriginLat}), 4326)::extensions.geography,
              dest_geo   = extensions.ST_SetSRID(extensions.ST_MakePoint(${newDestLng},   ${newDestLat}),   4326)::extensions.geography
          WHERE id = ${tripId}
        `
      }
      return result
    })

    return updated
  }

  /**
   * Mark a trip as COMPLETED and capture held payment for each confirmed booking.
   * Called by the driver after all passengers have been dropped off.
   */
  async complete(tripId: string, userId: string) {
    const trip = await this.prisma.trip.findUnique({
      where: { id: tripId },
      include: {
        driver: { select: { mobilepayNumber: true } },
        bookings: { where: { status: { in: [BookingStatus.PENDING, BookingStatus.CONFIRMED] } } },
      },
    })
    if (!trip) throw new AppException(ApiErrorCode.TRIP_NOT_FOUND)
    if (trip.driverId !== userId) throw new AppException(ApiErrorCode.NOT_YOUR_TRIP)
    if (!trip.driver.mobilepayNumber) {
      throw new AppException(ApiErrorCode.MOBILEPAY_MISSING)
    }
    const driver = trip.driver
    if (trip.status === TripStatus.COMPLETED) return { completed: true }
    if (trip.status !== TripStatus.ACTIVE) {
      throw new AppException(ApiErrorCode.TRIP_NOT_ACTIVE_FOR_COMPLETE)
    }

    await this.prisma.trip.update({
      where: { id: tripId },
      data: { status: TripStatus.COMPLETED, isActive: false },
    })

    // Create RideSettlement for each confirmed booking and transition to AWAITING_PAYMENT
    for (const booking of trip.bookings) {
      try {
        await this.prisma.$transaction([
          this.prisma.rideSettlement.create({
            data: {
              bookingId: booking.id,
              suggestedAmountOere: booking.totalOere,
              mobilepayNumber: driver.mobilepayNumber!,
            },
          }),
          this.prisma.booking.update({
            where: { id: booking.id },
            data: { status: BookingStatus.AWAITING_PAYMENT },
          }),
        ])

        // Notify passenger
        await this.notifications.sendToUser(
          booking.passengerId,
          'Time to pay your driver',
          `Please send DKK ${Math.round(booking.totalOere / 100)} to your driver via MobilePay.`,
          { type: 'RIDE_AWAITING_PAYMENT', bookingId: booking.id },
        ).catch(() => {/* non-fatal */})
        await this.prisma.notification.create({
          data: {
            userId: booking.passengerId,
            type: 'RIDE_AWAITING_PAYMENT',
            title: 'Time to pay your driver',
            body: `Please send DKK ${Math.round(booking.totalOere / 100)} to your driver via MobilePay.`,
            deepLinkId: booking.id,
          },
        })
      } catch (err) {
        this.logger.error(
          `[Trips] Failed to create settlement for booking ${booking.id}: ${err?.message}`,
        )
      }
    }

    return { completed: true }
  }

  async findByDriver(driverId: string) {
    const cutoff = new Date()
    cutoff.setDate(cutoff.getDate() - 30) // only return trips from the last 30 days onward
    return this.prisma.trip.findMany({
      where: { driverId, departureAt: { gte: cutoff } },
      orderBy: { departureAt: 'asc' },
      include: {
        bookings: {
          where: { status: { in: ['PENDING', 'CONFIRMED', 'AWAITING_PAYMENT', 'COMPLETED'] } },
          select: { id: true, seats: true, status: true, createdAt: true },
        },
      },
    })
  }

  async findByPassenger(passengerId: string) {
    const bookings = await this.prisma.booking.findMany({
      where: { passengerId, status: { not: 'CANCELLED' } },
      include: {
        trip: {
          include: {
            driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
            bookings: {
              where: { status: { in: ['PENDING', 'CONFIRMED', 'AWAITING_PAYMENT'] } },
              select: { id: true, seats: true, status: true },
            },
          },
        },
      },
      orderBy: { createdAt: 'desc' },
    })
    return bookings.map((b) => ({ ...b.trip, bookingId: b.id, bookingStatus: b.status }))
  }
}
