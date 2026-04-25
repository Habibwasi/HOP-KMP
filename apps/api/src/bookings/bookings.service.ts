import {
  Injectable,
  BadRequestException,
  NotFoundException,
  ForbiddenException,
  ConflictException,
} from '@nestjs/common'
import { InjectQueue } from '@nestjs/bullmq'
import { Queue } from 'bullmq'
import { PrismaService } from '../prisma/prisma.service'
import { CreateBookingDto } from './dto/create-booking.dto'
import { BookingStatus, TripModel, TripStatus } from '@prisma/client'

@Injectable()
export class BookingsService {
  constructor(
    private prisma: PrismaService,
    @InjectQueue('bookings') private bookingsQueue: Queue,
  ) {}

  async create(passengerId: string, dto: CreateBookingDto) {
    // Use interactive transaction for SELECT FOR UPDATE atomicity
    return this.prisma.$transaction(async (tx) => {
      const trip = await tx.trip.findUnique({ where: { id: dto.tripId } })

      if (!trip) throw new NotFoundException('Trip not found')
      if (trip.status !== TripStatus.ACTIVE) {
        throw new BadRequestException('Trip is not active')
      }
      if (trip.driverId === passengerId) {
        throw new BadRequestException('Cannot book your own trip')
      }

      // Check available seats
      const confirmedBookings = await tx.booking.aggregate({
        where: { tripId: dto.tripId, status: BookingStatus.CONFIRMED },
        _sum: { seats: true },
      })
      const bookedSeats = confirmedBookings._sum.seats ?? 0
      const available = trip.seats - bookedSeats

      if (dto.seats > available) {
        throw new ConflictException(`Only ${available} seat(s) available`)
      }

      // Check passenger doesn't already have an active booking
      const existing = await tx.booking.findFirst({
        where: {
          tripId: dto.tripId,
          passengerId,
          status: { in: [BookingStatus.PENDING, BookingStatus.CONFIRMED] },
        },
      })
      if (existing) throw new ConflictException('Already booked this trip')

      const totalOere = trip.pricePerSeat * dto.seats

      const booking = await tx.booking.create({
        data: {
          tripId: dto.tripId,
          passengerId,
          seats: dto.seats,
          totalOere,
          status: BookingStatus.PENDING,
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
  }

  async confirm(bookingId: string) {
    const booking = await this.prisma.booking.findUnique({ where: { id: bookingId } })
    if (!booking) throw new NotFoundException('Booking not found')
    if (booking.status !== BookingStatus.PENDING) {
      throw new BadRequestException('Booking is not pending')
    }
    return this.prisma.booking.update({
      where: { id: bookingId },
      data: { status: BookingStatus.CONFIRMED },
    })
  }

  async cancel(bookingId: string, userId: string) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      include: { trip: true },
    })
    if (!booking) throw new NotFoundException('Booking not found')

    const isPassenger = booking.passengerId === userId
    const isDriver = booking.trip.driverId === userId
    if (!isPassenger && !isDriver) throw new ForbiddenException('Not authorised')

    if (booking.status === BookingStatus.CANCELLED) {
      throw new BadRequestException('Already cancelled')
    }

    return this.prisma.booking.update({
      where: { id: bookingId },
      data: { status: BookingStatus.CANCELLED },
    })
  }

  async findById(id: string) {
    const booking = await this.prisma.booking.findUnique({
      where: { id },
      include: {
        trip: true,
        passenger: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
        payment: true,
      },
    })
    if (!booking) throw new NotFoundException('Booking not found')
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
        payment: true,
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
        payment: true,
      },
    })
  }

  // Called by BullMQ processor
  async checkModelBThreshold(tripId: string) {
    const trip = await this.prisma.trip.findUnique({ where: { id: tripId } })
    if (!trip || trip.status !== TripStatus.ACTIVE) return

    const confirmed = await this.prisma.booking.aggregate({
      where: { tripId, status: BookingStatus.CONFIRMED },
      _sum: { seats: true },
    })
    const bookedSeats = confirmed._sum.seats ?? 0

    if (bookedSeats < (trip.minPassengers ?? 0)) {
      // Cancel trip and all pending/confirmed bookings
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
    }
  }
}
