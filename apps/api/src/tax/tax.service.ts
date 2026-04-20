import { Injectable } from '@nestjs/common'
import { PrismaService } from '../prisma/prisma.service'

@Injectable()
export class TaxService {
  constructor(private prisma: PrismaService) {}

  async getMonthlyDashboard(driverId: string, year: number, month: number) {
    const start = new Date(year, month - 1, 1)
    const end = new Date(year, month, 0, 23, 59, 59)

    const records = await this.prisma.taxRecord.findMany({
      where: {
        driverId,
        recordedAt: { gte: start, lte: end },
      },
      include: {
        trip: {
          select: {
            originAddress: true,
            destAddress: true,
            departureAt: true,
            distanceKm: true,
          },
        },
      },
      orderBy: { recordedAt: 'asc' },
    })

    const totalOere = records.reduce((sum, r) => sum + r.amountOere, 0)
    const totalKm = records.reduce((sum, r) => sum + r.distanceKm, 0)
    const taxableAmountOere = records.reduce(
      (sum, r) => sum + Math.round(r.distanceKm * r.ratePerKm * 100),
      0,
    )

    return {
      year,
      month,
      totalTrips: records.length,
      totalEarnedOere: totalOere,
      totalKm: Math.round(totalKm * 10) / 10,
      taxableAmountOere,
      taxableAmountDkk: taxableAmountOere / 100,
      records,
    }
  }

  async getAnnualSummary(driverId: string, year: number) {
    const months = await Promise.all(
      Array.from({ length: 12 }, (_, i) =>
        this.getMonthlyDashboard(driverId, year, i + 1),
      ),
    )

    const totalOere = months.reduce((sum, m) => sum + m.totalEarnedOere, 0)
    const totalKm = months.reduce((sum, m) => sum + m.totalKm, 0)
    const taxableOere = months.reduce((sum, m) => sum + m.taxableAmountOere, 0)

    return {
      year,
      totalTrips: months.reduce((sum, m) => sum + m.totalTrips, 0),
      totalEarnedOere: totalOere,
      totalEarnedDkk: totalOere / 100,
      totalKm: Math.round(totalKm * 10) / 10,
      taxableAmountOere: taxableOere,
      taxableAmountDkk: taxableOere / 100,
      months,
    }
  }

  async getAllRecords(driverId: string) {
    return this.prisma.taxRecord.findMany({
      where: { driverId },
      orderBy: { recordedAt: 'desc' },
      include: {
        trip: {
          select: { originAddress: true, destAddress: true, departureAt: true },
        },
      },
    })
  }
}
