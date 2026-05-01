import {
  Injectable,
  BadRequestException,
  NotFoundException,
  ForbiddenException,
} from '@nestjs/common'
import { InjectQueue } from '@nestjs/bullmq'
import { Queue } from 'bullmq'
import { PrismaService } from '../prisma/prisma.service'
import { PricingService } from './pricing.service'
import { CreateTripDto } from './dto/create-trip.dto'
import { SearchTripsDto } from './dto/search-trips.dto'
import { TripModel, TripStatus } from '@prisma/client'
import {
  SEARCH_ALERTS_QUEUE,
  MATCH_ALERTS_JOB,
} from '../search-alerts/search-alerts.processor'

@Injectable()
export class TripsService {
  constructor(
    private prisma: PrismaService,
    private pricing: PricingService,
    @InjectQueue(SEARCH_ALERTS_QUEUE) private alertsQueue: Queue,
  ) {}

  async create(driverId: string, dto: CreateTripDto) {
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

    // Calculate distance and price
    const distanceKm = this.pricing.calculateDistance(
      dto.originLat,
      dto.originLng,
      dto.destLat,
      dto.destLng,
    )
    const pricePerSeat = this.pricing.calculatePricePerSeat(distanceKm, dto.seats)

    const trip = await this.prisma.trip.create({
      data: {
        driverId,
        model: dto.model,
        originLat: dto.originLat,
        originLng: dto.originLng,
        originAddress: dto.originAddress,
        destLat: dto.destLat,
        destLng: dto.destLng,
        destAddress: dto.destAddress,
        departureAt: new Date(dto.departureAt),
        seats: dto.seats,
        pricePerSeat,
        distanceKm,
        isRecurring: dto.model === TripModel.A,
        recurringDays: dto.recurringDays ?? [],
        minPassengers: dto.minPassengers,
        thresholdDeadline: dto.thresholdDeadline ? new Date(dto.thresholdDeadline) : null,
      },
      include: { driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
    })

    // Enqueue alert-matching as a fire-and-forget background job.
    await this.alertsQueue.add(MATCH_ALERTS_JOB, {
      tripId: trip.id,
      originAddress: trip.originAddress,
      destAddress: trip.destAddress,
    })

    return trip
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

    if (!useCoordinates) {
      if (dto.origin) {
        where.originAddress = { contains: dto.origin, mode: 'insensitive' }
      }
      if (dto.dest) {
        where.destAddress = { contains: dto.dest, mode: 'insensitive' }
      }
    }

    // Fetch active trips on requested date
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

        return availableSeats >= seats
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
          where: { status: 'CONFIRMED' },
          include: { passenger: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
        },
      },
    })
    if (!trip) throw new NotFoundException('Trip not found')
    return trip
  }

  async cancel(tripId: string, userId: string) {
    const trip = await this.prisma.trip.findUnique({ where: { id: tripId } })
    if (!trip) throw new NotFoundException('Trip not found')
    if (trip.driverId !== userId) throw new ForbiddenException('Not your trip')
    if (trip.status === TripStatus.CANCELLED) {
      throw new BadRequestException('Trip already cancelled')
    }

    return this.prisma.trip.update({
      where: { id: tripId },
      data: { status: TripStatus.CANCELLED, isActive: false },
    })
  }

  async findByDriver(driverId: string) {
    return this.prisma.trip.findMany({
      where: { driverId },
      orderBy: { departureAt: 'desc' },
      include: {
        bookings: { where: { status: 'CONFIRMED' } },
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
            bookings: { where: { status: 'CONFIRMED' } },
          },
        },
      },
      orderBy: { createdAt: 'desc' },
    })
    return bookings.map((b) => b.trip)
  }
}
