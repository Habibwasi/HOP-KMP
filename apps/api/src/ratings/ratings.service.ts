import {
  Injectable,
  BadRequestException,
  ConflictException,
  NotFoundException,
  ForbiddenException,
} from '@nestjs/common'
import { PrismaService } from '../prisma/prisma.service'
import { CreateRatingDto } from './dto/create-rating.dto'

@Injectable()
export class RatingsService {
  constructor(private prisma: PrismaService) {}

  async create(raterId: string, dto: CreateRatingDto) {
    if (raterId === dto.rateeId) {
      throw new BadRequestException('Cannot rate yourself')
    }

    // Verify rater was part of the trip and resolve the valid ratee
    const trip = await this.prisma.trip.findUnique({
      where: { id: dto.tripId },
      include: {
        bookings: {
          where: { passengerId: raterId },
          select: { passengerId: true },
        },
      },
    })

    if (!trip) throw new NotFoundException('Trip not found')

    const isDriver = trip.driverId === raterId
    const isPassenger = trip.bookings.length > 0

    if (!isDriver && !isPassenger) {
      throw new BadRequestException('You were not part of this trip')
    }

    // Validate that rateeId is the actual counterparty for this trip.
    // Driver may only rate a passenger on this trip; passenger may only rate the driver.
    if (isDriver) {
      const passengerIds = new Set(trip.bookings.map((b) => b.passengerId))
      if (!passengerIds.has(dto.rateeId)) {
        throw new ForbiddenException('You can only rate passengers on this trip')
      }
    } else {
      if (dto.rateeId !== trip.driverId) {
        throw new ForbiddenException('You can only rate the driver of this trip')
      }
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
