import { Injectable } from '@nestjs/common'

@Injectable()
export class PricingService {
  // Danish tax authority allows DKK 0.27/km per passenger (2024 rate)
  private readonly RATE_PER_KM = 0.27

  // Returns price in oere (integer) — never floats
  calculatePricePerSeat(distanceKm: number, seats: number): number {
    const pricePerSeatDkk = distanceKm * this.RATE_PER_KM
    const pricePerSeatOere = Math.round(pricePerSeatDkk * 100)
    return Math.max(pricePerSeatOere, 100) // minimum 1 DKK
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
