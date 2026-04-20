import {
  Injectable,
  BadRequestException,
  ConflictException,
  NotFoundException,
} from '@nestjs/common'
import { PrismaService } from '../prisma/prisma.service'
import { CreateRatingDto } from './dto/create-rating.dto'
import { BookingStatus } from '@prisma/client'

const RATING_WINDOW_HOURS = 48

@Injectable()
export class RatingsService {
  constructor(private prisma: PrismaService) {}

  async create(raterId: string, dto: CreateRatingDto) {
    if (raterId === dto.rateeId) {
      throw new BadRequestException('Cannot rate yourself')
    }

    // Verify rater was part of the trip
    const trip = await this.prisma.trip.findUnique({
      where: { id: dto.tripId },
      include: {
        bookings: {
          where: {
            status: BookingStatus.CONFIRMED,
            passengerId: raterId,
          },
        },
      },
    })

    if (!trip) throw new NotFoundException('Trip not found')

    const isDriver = trip.driverId === raterId
    const isPassenger = trip.bookings.length > 0

    if (!isDriver && !isPassenger) {
      throw new BadRequestException('You were not part of this trip')
    }

    // Enforce 48hr rating window
    const windowEnd = new Date(trip.departureAt)
    windowEnd.setHours(windowEnd.getHours() + RATING_WINDOW_HOURS)
    if (new Date() > windowEnd) {
      throw new BadRequestException('Rating window has closed (48 hours after departure)')
    }

    // Prevent duplicate rating
    const existing = await this.prisma.rating.findFirst({
      where: { raterId, rateeId: dto.rateeId },
    })
    if (existing) throw new ConflictException('Already rated this user for this trip')

    return this.prisma.rating.create({
      data: {
        raterId,
        rateeId: dto.rateeId,
        score: dto.score,
        comment: dto.comment,
      },
    })
  }

  async getUserRatings(userId: string) {
    const ratings = await this.prisma.rating.findMany({
      where: { rateeId: userId },
      include: {
        rater: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
      },
      orderBy: { createdAt: 'desc' },
    })

    const avg =
      ratings.length > 0
        ? ratings.reduce((sum, r) => sum + r.score, 0) / ratings.length
        : null

    return {
      userId,
      averageScore: avg ? Math.round(avg * 10) / 10 : null,
      totalRatings: ratings.length,
      ratings,
    }
  }
}
