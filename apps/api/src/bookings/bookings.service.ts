import { Injectable, Logger } from '@nestjs/common'
import { InjectQueue } from '@nestjs/bullmq'
import { Queue } from 'bullmq'
import { PrismaService } from '../prisma/prisma.service'
import { CreateBookingDto } from './dto/create-booking.dto'
import { BookingStatus, TripModel, TripStatus } from '@prisma/client'
import { NotificationsService } from '../notifications/notifications.service'
import { AppException } from '../common/errors/app-exception'
import { ApiErrorCode } from '../common/errors/api-error-codes'

@Injectable()
export class BookingsService {
  private readonly logger = new Logger(BookingsService.name)

  constructor(
    private prisma: PrismaService,
    @InjectQueue('bookings') private bookingsQueue: Queue,
    private notifications: NotificationsService,
  ) {}

  async create(passengerId: string, dto: CreateBookingDto) {
    // Use interactive transaction for SELECT FOR UPDATE atomicity
    const booking = await this.prisma.$transaction(async (tx) => {
      const trip = await tx.trip.findUnique({ where: { id: dto.tripId } })

      if (!trip) throw new AppException(ApiErrorCode.TRIP_NOT_FOUND)
      if (trip.status !== TripStatus.ACTIVE) {
        throw new AppException(ApiErrorCode.TRIP_NOT_ACTIVE)
      }
      if (trip.driverId === passengerId) {
        throw new AppException(ApiErrorCode.CANNOT_BOOK_OWN_TRIP)
      }

      // Check available seats (count CONFIRMED + PENDING to prevent concurrent overbooking)
      const activeBookings = await tx.booking.aggregate({
        where: {
          tripId: dto.tripId,
          status: { in: [BookingStatus.CONFIRMED, BookingStatus.PENDING] },
        },
        _sum: { seats: true },
      })
      const bookedSeats = activeBookings._sum.seats ?? 0
      const available = trip.seats - bookedSeats

      if (dto.seats > available) {
        throw new AppException(ApiErrorCode.INSUFFICIENT_SEATS, `Only ${available} seat(s) available`)
      }

      // Check passenger doesn't already have an active booking
      const existing = await tx.booking.findFirst({
        where: {
          tripId: dto.tripId,
          passengerId,
          status: { in: [BookingStatus.PENDING, BookingStatus.CONFIRMED] },
        },
      })
      if (existing) throw new AppException(ApiErrorCode.ALREADY_BOOKED)

      const totalOere = trip.pricePerSeat * dto.seats

      const booking = await tx.booking.create({
        data: {
          tripId: dto.tripId,
          passengerId,
          seats: dto.seats,
          totalOere,
          // Model A trips confirm immediately; Model B trips wait for the threshold deadline.
          status: trip.model === TripModel.A ? BookingStatus.CONFIRMED : BookingStatus.PENDING,
        },
        include: {
          trip: { include: { driver: { select: { id: true, firstName: true, lastName: true } } } },
          passenger: { select: { id: true, firstName: true, lastName: true } },
        },
      })

      // Schedule Model B threshold check if needed
      if (trip.model === TripModel.B && trip.thresholdDeadline) {
        const delay = new Date(trip.thresholdDeadline).getTime() - Date.now()
        if (delay > 0) {
          await this.bookingsQueue.add(
            'check-threshold',
            { tripId: dto.tripId },
            { delay, jobId: `threshold-${dto.tripId}`, removeOnComplete: true },
          )
        }
      }

      return booking
    })

    // Notify the driver — fire-and-forget, never block the response
    const passengerName = booking.passenger.firstName
    this.notifications
      .sendToUser(
        booking.trip.driverId,
        'New booking 🎉',
        `${passengerName} booked ${dto.seats} seat(s) on your trip`,
        { type: 'NEW_BOOKING', tripId: dto.tripId, bookingId: booking.id },
      )
      .catch(() => {/* ignore push errors */})

    // Persist in-app notification so the driver sees it in their notification inbox.
    this.prisma.notification.create({
      data: {
        userId: booking.trip.driverId,
        type: 'NEW_BOOKING',
        title: 'New booking 🎉',
        body: `${passengerName} booked ${dto.seats} seat(s) on your trip`,
        deepLinkId: booking.id,
      },
    }).catch(() => {/* non-fatal */})

    return booking
  }

  async confirm(bookingId: string, driverId: string) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      include: { trip: { select: { driverId: true } } },
    })
    if (!booking) throw new AppException(ApiErrorCode.BOOKING_NOT_FOUND)
    if (booking.trip.driverId !== driverId) throw new AppException(ApiErrorCode.DRIVER_ONLY)
    // Idempotent: if already confirmed, just return.
    if (booking.status === BookingStatus.CONFIRMED) return booking
    if (booking.status !== BookingStatus.PENDING) {
      throw new AppException(ApiErrorCode.BOOKING_NOT_PENDING)
    }
    return this.prisma.booking.update({
      where: { id: bookingId },
      data: { status: BookingStatus.CONFIRMED },
    })
  }

  /**
   * Like findById but also verifies the requesting user is a party to the
   * booking (passenger or driver). Throws ForbiddenException otherwise.
   */
  async findByIdAuthorized(id: string, requesterId: string) {
    const booking = await this.findById(id)
    const isParty =
      booking.passengerId === requesterId || booking.trip.driverId === requesterId
    if (!isParty) throw new AppException(ApiErrorCode.NOT_A_PARTY)
    return booking
  }

  async cancel(bookingId: string, userId: string) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      include: { trip: true },
    })
    if (!booking) throw new AppException(ApiErrorCode.BOOKING_NOT_FOUND)

    const isPassenger = booking.passengerId === userId
    const isDriver = booking.trip.driverId === userId
    if (!isPassenger && !isDriver) throw new AppException(ApiErrorCode.NOT_A_PARTY)

    if (booking.status === BookingStatus.CANCELLED) {
      throw new AppException(ApiErrorCode.ALREADY_CANCELLED)
    }

    await this.prisma.booking.update({
      where: { id: bookingId },
      data: { status: BookingStatus.CANCELLED },
    })

    return { cancelled: true }
  }

  /**
   * Cancel all active bookings for a trip (called when a driver cancels a trip).
   */
  async cancelAllForTrip(tripId: string) {
    await this.prisma.booking.updateMany({
      where: {
        tripId,
        status: { in: [BookingStatus.PENDING, BookingStatus.CONFIRMED] },
      },
      data: { status: BookingStatus.CANCELLED },
    })
  }

  async findById(id: string) {
    const booking = await this.prisma.booking.findUnique({
      where: { id },
      include: {
        trip: true,
        passenger: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
      },
    })
    if (!booking) throw new AppException(ApiErrorCode.BOOKING_NOT_FOUND)
    return booking
  }

  /**
   * Returns the next upcoming confirmed booking for a passenger, or null.
   * Used by the home screen "active trip" hero card.
   */
  async findActiveForPassenger(passengerId: string) {
    return this.prisma.booking.findFirst({
      where: {
        passengerId,
        status: BookingStatus.CONFIRMED,
        trip: { departureAt: { gte: new Date() }, status: TripStatus.ACTIVE },
      },
      orderBy: { trip: { departureAt: 'asc' } },
      include: {
        trip: {
          include: {
            driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
          },
        },
      },
    })
  }

  async findByPassenger(passengerId: string) {
    return this.prisma.booking.findMany({
      where: { passengerId },
      orderBy: { createdAt: 'desc' },
      include: {
        trip: {
          include: { driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
        },
      },
    })
  }

  async findMyChats(userId: string) {
    const [asPassenger, asDriver] = await Promise.all([
      this.prisma.booking.findMany({
        where: { passengerId: userId, status: { not: 'CANCELLED' } },
        orderBy: { createdAt: 'desc' },
        include: {
          trip: {
            include: { driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
          },
        },
      }),
      this.prisma.booking.findMany({
        where: { trip: { driverId: userId }, status: { not: 'CANCELLED' } },
        orderBy: { createdAt: 'desc' },
        include: {
          passenger: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
          trip: true,
        },
      }),
    ])

    const passengerChats = asPassenger.map((b) => ({
      bookingId: b.id,
      bookingStatus: b.status,
      tripOrigin: b.trip.originAddress,
      tripDest: b.trip.destAddress,
      departureAt: b.trip.departureAt.toISOString(),
      otherPartyId: b.trip.driver.id,
      otherPartyName: `${b.trip.driver.firstName} ${b.trip.driver.lastName}`.trim(),
      otherPartyAvatarUrl: b.trip.driver.avatarUrl ?? null,
      myRole: 'PASSENGER',
    }))

    const driverChats = asDriver.map((b) => ({
      bookingId: b.id,
      bookingStatus: b.status,
      tripOrigin: b.trip.originAddress,
      tripDest: b.trip.destAddress,
      departureAt: b.trip.departureAt.toISOString(),
      otherPartyId: b.passenger.id,
      otherPartyName: `${b.passenger.firstName} ${b.passenger.lastName}`.trim(),
      otherPartyAvatarUrl: b.passenger.avatarUrl ?? null,
      myRole: 'DRIVER',
    }))

    return [...passengerChats, ...driverChats].sort(
      (a, b) => new Date(b.departureAt).getTime() - new Date(a.departureAt).getTime(),
    )
  }

  // Called by BullMQ processor
  async checkModelBThreshold(tripId: string) {
    const trip = await this.prisma.trip.findUnique({
      where: { id: tripId },
      include: { driver: { select: { id: true, firstName: true, lastName: true } } },
    })
    if (!trip || trip.status !== TripStatus.ACTIVE) return

    // Count PENDING bookings — these are the passengers waiting to confirm
    const pending = await this.prisma.booking.aggregate({
      where: { tripId, status: BookingStatus.PENDING },
      _sum: { seats: true },
    })
    const pendingSeats = pending._sum.seats ?? 0

    if (pendingSeats >= (trip.minPassengers ?? 0)) {
      // Threshold met — confirm all pending bookings and notify
      await this.prisma.booking.updateMany({
        where: { tripId, status: BookingStatus.PENDING },
        data: { status: BookingStatus.CONFIRMED },
      })

      const confirmedBookings = await this.prisma.booking.findMany({
        where: { tripId, status: BookingStatus.CONFIRMED },
        select: { passengerId: true },
      })
      const title = 'Trip is a go!'
      const body = `Your Model B trip from ${trip.originAddress} to ${trip.destAddress} has reached the minimum threshold. It\'s confirmed!`
      await this.notifications.sendToUser(trip.driverId, title, body, { type: 'THRESHOLD_MET', tripId })
      await this.prisma.notification.create({
        data: { userId: trip.driverId, type: 'THRESHOLD_MET', title, body, deepLinkId: tripId },
      })
      for (const { passengerId } of confirmedBookings) {
        await this.notifications.sendToUser(passengerId, title, body, { type: 'THRESHOLD_MET', tripId })
        await this.prisma.notification.create({
          data: { userId: passengerId, type: 'THRESHOLD_MET', title, body, deepLinkId: tripId },
        })
      }
      return
    }

    // Threshold NOT met — cancel trip and all active bookings
    const activeBookings = await this.prisma.booking.findMany({
      where: { tripId, status: { in: [BookingStatus.PENDING, BookingStatus.CONFIRMED] } },
      select: { passengerId: true },
    })

    await this.prisma.$transaction([
      this.prisma.trip.update({
        where: { id: tripId },
        data: { status: TripStatus.THRESHOLD_NOT_MET, isActive: false },
      }),
      this.prisma.booking.updateMany({
        where: {
          tripId,
          status: { in: [BookingStatus.PENDING, BookingStatus.CONFIRMED] },
        },
        data: { status: BookingStatus.CANCELLED },
      }),
    ])

    // Notify passengers and send notifications
    const cancelTitle = 'Trip cancelled — threshold not reached'
    const cancelBody = `The Model B trip from ${trip.originAddress} to ${trip.destAddress} did not reach the minimum passenger count and has been cancelled.`

    await this.notifications.sendToUser(trip.driverId, cancelTitle, cancelBody, { type: 'GENERAL', tripId })
    await this.prisma.notification.create({
      data: { userId: trip.driverId, type: 'GENERAL', title: cancelTitle, body: cancelBody, deepLinkId: tripId },
    })

    for (const { passengerId } of activeBookings) {
      await this.notifications.sendToUser(passengerId, cancelTitle, cancelBody, { type: 'BOOKING_CANCELLED', tripId })
      await this.prisma.notification.create({
        data: { userId: passengerId, type: 'BOOKING_CANCELLED', title: cancelTitle, body: cancelBody, deepLinkId: tripId },
      })
    }
  }
}
