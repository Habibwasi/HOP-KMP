import { Injectable } from '@nestjs/common'
import { BookingStatus, TripStatus } from '@prisma/client'
import { PrismaService } from '../prisma/prisma.service'

/**
 * Per-completed-trip CO₂ savings estimate (kg). Conservative average for a
 * shared 30 km Danish commute vs. driving solo. Treated as a constant until
 * a real distance-weighted calculation lands.
 */
const CO2_KG_PER_TRIP = 4.2

@Injectable()
export class AggregatesService {
  constructor(private readonly prisma: PrismaService) {}

  /**
   * Daily earnings totals (in øre) for the authenticated driver over the
   * last `days` days. Bookings counted are CONFIRMED on COMPLETED trips so
   * cancelled / refunded amounts never appear in the series.
   */
  async getDriverEarningsSeries(driverId: string, days: number) {
    const since = new Date()
    since.setHours(0, 0, 0, 0)
    since.setDate(since.getDate() - (days - 1))

    const bookings = await this.prisma.booking.findMany({
      where: {
        status: BookingStatus.CONFIRMED,
        trip: {
          driverId,
          status: TripStatus.COMPLETED,
          departureAt: { gte: since },
        },
      },
      select: { totalOere: true, trip: { select: { departureAt: true } } },
    })

    const dailyMap = new Map<string, number>()
    let totalOere = 0
    for (const b of bookings) {
      const dateKey = b.trip.departureAt.toISOString().split('T')[0]
      dailyMap.set(dateKey, (dailyMap.get(dateKey) ?? 0) + b.totalOere)
      totalOere += b.totalOere
    }

    const series: { date: string; earningsOere: number }[] = []
    for (let i = days - 1; i >= 0; i--) {
      const d = new Date()
      d.setHours(0, 0, 0, 0)
      d.setDate(d.getDate() - i)
      const key = d.toISOString().split('T')[0]
      series.push({ date: key, earningsOere: dailyMap.get(key) ?? 0 })
    }
    return { series, totalOere }
  }

  /**
   * Globally most-booked routes in the last 7 days. Grouped by exact
   * (originAddress, destAddress) string match — coarse but stable until
   * geographic clustering is in place.
   */
  async getPopularRoutes(limit: number) {
    const since = new Date()
    since.setDate(since.getDate() - 7)

    const trips = await this.prisma.trip.findMany({
      where: { createdAt: { gte: since } },
      select: {
        originAddress: true,
        destAddress: true,
        bookings: {
          where: { status: BookingStatus.CONFIRMED },
          select: { id: true },
        },
      },
    })

    const map = new Map<
      string,
      { origin: string; destination: string; count: number }
    >()
    for (const t of trips) {
      const key = `${t.originAddress}|${t.destAddress}`
      const cur = map.get(key) ?? {
        origin: t.originAddress,
        destination: t.destAddress,
        count: 0,
      }
      cur.count += t.bookings.length
      map.set(key, cur)
    }

    const routes = Array.from(map.values())
      .sort((a, b) => b.count - a.count)
      .slice(0, limit)
      .map((r) => ({
        origin: r.origin,
        destination: r.destination,
        tripsThisWeek: r.count,
      }))

    return { routes }
  }

  /**
   * Booking-density "hotspots" grouped by trip origin address. Returns the
   * top 5 origins by total CONFIRMED bookings in the last 7 days. lat/lng
   * are taken from the most recent trip in the bucket as a representative
   * marker.
   */
  async getDemandHotspots() {
    const since = new Date()
    since.setDate(since.getDate() - 7)

    const trips = await this.prisma.trip.findMany({
      where: { createdAt: { gte: since } },
      orderBy: { createdAt: 'desc' },
      select: {
        originAddress: true,
        originLat: true,
        originLng: true,
        bookings: {
          where: { status: BookingStatus.CONFIRMED },
          select: { id: true },
        },
      },
    })

    const map = new Map<
      string,
      { areaName: string; lat: number; lng: number; count: number }
    >()
    for (const t of trips) {
      const cur = map.get(t.originAddress) ?? {
        areaName: t.originAddress,
        lat: t.originLat,
        lng: t.originLng,
        count: 0,
      }
      cur.count += t.bookings.length
      map.set(t.originAddress, cur)
    }

    const hotspots = Array.from(map.values())
      .filter((h) => h.count > 0)
      .sort((a, b) => b.count - a.count)
      .slice(0, 5)
      .map((h) => ({
        areaName: h.areaName,
        demandCount: h.count,
        lat: h.lat,
        lng: h.lng,
      }))

    return { hotspots }
  }

  /**
   * Lifetime CO₂ saved estimate. Approximated as
   * `completedConfirmedBookings * CO2_KG_PER_TRIP`.
   */
  async getUserCo2Saved(userId: string) {
    const completedTrips = await this.prisma.booking.count({
      where: {
        passengerId: userId,
        status: BookingStatus.CONFIRMED,
        trip: { status: TripStatus.COMPLETED },
      },
    })
    const co2SavedKg = Math.round(completedTrips * CO2_KG_PER_TRIP)
    return { co2SavedKg }
  }
}
