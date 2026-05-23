import { Injectable } from '@nestjs/common'
import { PrismaService } from '../prisma/prisma.service'
import { AppException } from '../common/errors/app-exception'
import { ApiErrorCode } from '../common/errors/api-error-codes'

@Injectable()
export class AdminService {
  constructor(private prisma: PrismaService) {}

  // ─── USERS ───────────────────────────────────────────────────────────────────

  async listUsers(page = 1, limit = 20) {
    const skip = (page - 1) * limit
    const [users, total] = await Promise.all([
      this.prisma.user.findMany({
        skip,
        take: limit,
        orderBy: { createdAt: 'desc' },
        select: {
          id: true, phone: true, email: true,
          firstName: true, lastName: true,
          role: true, isVerified: true, isBanned: true,
          banExpiresAt: true, isAdmin: true, createdAt: true,
        },
      }),
      this.prisma.user.count(),
    ])
    return { users, total, page, pages: Math.ceil(total / limit) }
  }

  async banUser(userId: string, durationDays: number | 'permanent') {
    const user = await this.prisma.user.findUnique({ where: { id: userId } })
    if (!user) throw new AppException(ApiErrorCode.USER_NOT_FOUND)

    const banExpiresAt =
      durationDays === 'permanent'
        ? new Date('9999-12-31')
        : new Date(Date.now() + durationDays * 24 * 60 * 60 * 1000)

    return this.prisma.user.update({
      where: { id: userId },
      data: { isBanned: true, banExpiresAt },
    })
  }

  async unbanUser(userId: string) {
    return this.prisma.user.update({
      where: { id: userId },
      data: { isBanned: false, banExpiresAt: null },
    })
  }

  async makeAdmin(userId: string) {
    return this.prisma.user.update({
      where: { id: userId },
      data: { isAdmin: true },
    })
  }

  // ─── LICENCES ────────────────────────────────────────────────────────────────

  async getPendingLicences() {
    return this.prisma.driverLicence.findMany({
      where: { status: 'PENDING' },
      include: {
        user: { select: { id: true, firstName: true, lastName: true, phone: true } },
      },
      orderBy: { createdAt: 'asc' },
    })
  }

  async reviewLicence(licenceId: string, adminId: string, approved: boolean) {
    const licence = await this.prisma.driverLicence.findUnique({ where: { id: licenceId } })
    if (!licence) throw new AppException(ApiErrorCode.LICENCE_NOT_FOUND)

    await this.prisma.driverLicence.update({
      where: { id: licenceId },
      data: {
        status: approved ? 'CONFIRMED' : 'CANCELLED',
        reviewedAt: new Date(),
        reviewedBy: adminId,
      },
    })

    if (approved) {
      await this.prisma.user.update({
        where: { id: licence.userId },
        data: { role: 'DRIVER' },
      })
    }

    return { reviewed: true, approved }
  }

  // ─── TRIPS ───────────────────────────────────────────────────────────────────

  async listTrips(page = 1, limit = 20) {
    const skip = (page - 1) * limit
    const [trips, total] = await Promise.all([
      this.prisma.trip.findMany({
        skip,
        take: limit,
        orderBy: { createdAt: 'desc' },
        include: {
          driver: { select: { id: true, firstName: true, lastName: true } },
          _count: { select: { bookings: true } },
        },
      }),
      this.prisma.trip.count(),
    ])
    return { trips, total, page, pages: Math.ceil(total / limit) }
  }

  // ─── STATS ───────────────────────────────────────────────────────────────────

  async getDashboardStats() {
    const [
      totalUsers,
      verifiedUsers,
      bannedUsers,
      totalTrips,
      activeTrips,
      totalBookings,
      confirmedBookings,
      pendingLicences,
    ] = await Promise.all([
      this.prisma.user.count(),
      this.prisma.user.count({ where: { isVerified: true } }),
      this.prisma.user.count({ where: { isBanned: true } }),
      this.prisma.trip.count(),
      this.prisma.trip.count({ where: { status: 'ACTIVE' } }),
      this.prisma.booking.count(),
      this.prisma.booking.count({ where: { status: 'CONFIRMED' } }),
      this.prisma.driverLicence.count({ where: { status: 'PENDING' } }),
    ])

    return {
      users: { total: totalUsers, verified: verifiedUsers, banned: bannedUsers },
      trips: { total: totalTrips, active: activeTrips },
      bookings: { total: totalBookings, confirmed: confirmedBookings },
      pendingLicenceReviews: pendingLicences,
    }
  }
}
