import { Injectable } from '@nestjs/common'
import { SKAT_RATE_DKK_PER_KM } from '../common/tax-constants'

@Injectable()
export class PricingService {
  // SKAT 2024 rate: DKK 2.28/km total trip cost
  private readonly RATE_PER_KM = SKAT_RATE_DKK_PER_KM
  private readonly PLATFORM_FEE_RATE = 0.15

  // Returns passenger-pays price per seat in oere (integer) — never floats.
  // Formula: (distanceKm × 2.28 DKK/km) / seats, then add 15% platform fee.
  calculatePricePerSeat(distanceKm: number, seats: number): number {
    const totalTripCostOere = Math.round(distanceKm * this.RATE_PER_KM * 100)
    const driverNetPerSeatOere = Math.floor(totalTripCostOere / seats)
    const passengerPaysPerSeatOere = Math.round(driverNetPerSeatOere / (1 - this.PLATFORM_FEE_RATE))
    return Math.max(passengerPaysPerSeatOere, 100) // minimum 1 DKK
  }

  // Haversine formula — distance between two lat/lng points in km
  calculateDistance(
    originLat: number,
    originLng: number,
    destLat: number,
    destLng: number,
  ): number {
    const R = 6371
    const dLat = this.toRad(destLat - originLat)
    const dLng = this.toRad(destLng - originLng)
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.toRad(originLat)) *
        Math.cos(this.toRad(destLat)) *
        Math.sin(dLng / 2) *
        Math.sin(dLng / 2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
  }

  private toRad(deg: number): number {
    return (deg * Math.PI) / 180
  }
}
