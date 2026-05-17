import {
  Injectable,
  BadRequestException,
  NotFoundException,
  ForbiddenException,
  forwardRef,
  Inject,
  Logger,
} from '@nestjs/common'
import { InjectQueue } from '@nestjs/bullmq'
import { Queue } from 'bullmq'
import { PrismaService } from '../prisma/prisma.service'
import { PricingService } from './pricing.service'
import { CreateTripDto } from './dto/create-trip.dto'
import { SearchTripsDto } from './dto/search-trips.dto'
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
      throw new BadRequestException(
        'Please add your MobilePay number in your profile before creating a trip',
      )
    }

    // Validate Model B requirements
    if (dto.model === TripModel.B) {
      if (!dto.minPassengers) {
        throw new BadRequestException('minPassengers is required for Model B trips')
      }
      if (!dto.thresholdDeadline) {
        throw new BadRequestException('thresholdDeadline is required for Model B trips')
      }
      const deadline = new Date(dto.thresholdDeadline)
      const departure = new Date(dto.departureAt)
      if (deadline >= departure) {
        throw new BadRequestException('thresholdDeadline must be before departureAt')
      }
    }

    // Model A: validate recurringDays present
    if (dto.model === TripModel.A) {
      if (!dto.recurringDays || dto.recurringDays.length === 0) {
        throw new BadRequestException('recurringDays is required for Model A trips')
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
      minPassengers: dto.minPassengers,
      thresholdDeadline: dto.thresholdDeadline ? new Date(dto.thresholdDeadline) : null,
    }

    // ── Model A: generate one trip instance per occurrence in 30-day window ──
    if (dto.model === TripModel.A) {
      const anchor = new Date(dto.departureAt)
      const dates = buildRecurringDates(anchor, dto.recurringDays!, 30)

      if (dates.length === 0) {
        throw new BadRequestException(
          'No occurrences found in the next 30 days for the selected days',
        )
      }

      const instances = dates.map((d) => ({ ...baseData, departureAt: d }))

      await this.prisma.trip.createMany({ data: instances })

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
      }

      return firstTrip
    }

    // ── Model B: single trip instance ────────────────────────────────────────
    const trip = await this.prisma.trip.create({
      data: {
        ...baseData,
        departureAt: new Date(dto.departureAt),
      },
      include: { driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
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
   * Extends the 30-day instance window for all active recurring (Model A) trips.
   * Called daily by the trips queue repeatable job.
   * Finds routes whose latest future instance is within the next 7 days and
   * creates new instances to bring the window back to 30 days.
   */
  async extendRecurringWindow() {
    const now = new Date()
    const horizon = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000)

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

      // Generate new dates from the day after maxDate up to horizon
      const extendAnchor = new Date(maxDate.getTime() + 24 * 60 * 60 * 1000)
      const newDates = buildRecurringDates(extendAnchor, template.recurringDays, 30)

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
        }))

      if (newInstances.length > 0) {
        await this.prisma.trip.createMany({ data: newInstances, skipDuplicates: true })
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

    // Build where clause — text search when no coordinates provided
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

    // Filter by proximity (haversine) when coordinates provided, otherwise just by available seats
    return trips
      .filter((trip) => {
        const bookedSeats = trip.bookings.reduce((sum, b) => sum + b.seats, 0)
        const availableSeats = trip.seats - bookedSeats

        if (useCoordinates) {
          const originDist = this.pricing.calculateDistance(
            dto.originLat!,
            dto.originLng!,
            trip.originLat,
            trip.originLng,
          )
          const destDist = this.pricing.calculateDistance(
            dto.destLat!,
            dto.destLng!,
            trip.destLat,
            trip.destLng,
          )
          return originDist <= radiusKm && destDist <= radiusKm && availableSeats >= seats
        }

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
    if (!trip) throw new NotFoundException('Trip not found')
    return trip
  }

  async getTripPassengers(tripId: string, driverId: string) {
    const trip = await this.prisma.trip.findUnique({ where: { id: tripId } })
    if (!trip) throw new NotFoundException('Trip not found')
    if (trip.driverId !== driverId) throw new ForbiddenException('Not your trip')

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
      }
    })
  }

  async cancel(tripId: string, userId: string) {
    const trip = await this.prisma.trip.findUnique({ where: { id: tripId } })
    if (!trip) throw new NotFoundException('Trip not found')
    if (trip.driverId !== userId) throw new ForbiddenException('Not your trip')
    if (trip.status === TripStatus.CANCELLED) {
      throw new BadRequestException('Trip already cancelled')
    }

    await this.prisma.trip.update({
      where: { id: tripId },
      data: { status: TripStatus.CANCELLED, isActive: false },
    })

    // Cancel all active bookings and refund passengers
    await this.bookings.cancelAllForTrip(tripId).catch((err) => {
      this.logger.error(`[Trips] cancelAllForTrip failed for trip ${tripId}: ${err?.message}`)
    })

    return { cancelled: true }
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
    if (!trip) throw new NotFoundException('Trip not found')
    if (trip.driverId !== userId) throw new ForbiddenException('Not your trip')
    if (!trip.driver.mobilepayNumber) {
      throw new BadRequestException('Add your MobilePay number before completing a trip')
    }
    const driver = trip.driver
    if (trip.status === TripStatus.COMPLETED) return { completed: true }
    if (trip.status !== TripStatus.ACTIVE) {
      throw new BadRequestException('Trip must be ACTIVE to complete')
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
    return this.prisma.trip.findMany({
      where: { driverId },
      orderBy: { departureAt: 'desc' },
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
