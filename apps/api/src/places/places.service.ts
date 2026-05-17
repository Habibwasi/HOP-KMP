import { BadRequestException, ForbiddenException, Injectable, NotFoundException } from '@nestjs/common'
import { ConfigService } from '@nestjs/config'
import { PrismaService } from '../prisma/prisma.service'
import { UpsertPlaceDto } from './places.controller'

@Injectable()
export class PlacesService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly config: ConfigService,
  ) {}

  listForUser(userId: string) {
    return this.prisma.savedPlace.findMany({
      where: { userId },
      orderBy: [{ kind: 'asc' }, { createdAt: 'asc' }],
    })
  }

  create(userId: string, dto: UpsertPlaceDto) {
    return this.prisma.savedPlace.upsert({
      where: { userId_label: { userId, label: dto.label } },
      create: {
        userId,
        label: dto.label,
        address: dto.address,
        lat: dto.lat,
        lng: dto.lng,
        kind: dto.kind ?? 'CUSTOM',
      },
      update: {
        address: dto.address,
        lat: dto.lat,
        lng: dto.lng,
        kind: dto.kind ?? 'CUSTOM',
      },
    })
  }

  async update(userId: string, id: string, dto: UpsertPlaceDto) {
    const existing = await this.prisma.savedPlace.findUnique({ where: { id } })
    if (!existing) throw new NotFoundException('Place not found')
    if (existing.userId !== userId) throw new ForbiddenException()
    return this.prisma.savedPlace.update({
      where: { id },
      data: {
        label: dto.label,
        address: dto.address,
        lat: dto.lat,
        lng: dto.lng,
        kind: dto.kind ?? existing.kind,
      },
    })
  }

  async remove(userId: string, id: string) {
    const existing = await this.prisma.savedPlace.findUnique({ where: { id } })
    if (!existing) throw new NotFoundException('Place not found')
    if (existing.userId !== userId) throw new ForbiddenException()
    await this.prisma.savedPlace.delete({ where: { id } })
    return { ok: true }
  }

  /**
   * Geocode a free-text address via Google Maps Geocoding API.
   * Returns { lat, lng, formattedAddress } or throws if nothing found.
   */
  async geocode(address: string): Promise<{ lat: number; lng: number; formattedAddress: string }> {
    const key = this.googleMapsKey()
    if (!key) throw new BadRequestException('Geocoding is not configured on this server')

    const url = `https://maps.googleapis.com/maps/api/geocode/json?address=${encodeURIComponent(address)}&key=${key}`
    const res = await fetch(url)
    if (!res.ok) throw new BadRequestException('Geocoding request failed')

    const json = await res.json() as {
      status: string
      results: Array<{
        formatted_address: string
        geometry: { location: { lat: number; lng: number } }
      }>
    }

    if (json.status !== 'OK' || !json.results.length) {
      throw new BadRequestException(`No geocoding result for: ${address}`)
    }

    const { lat, lng } = json.results[0].geometry.location
    return { lat, lng, formattedAddress: json.results[0].formatted_address }
  }

  /** Resolve a driving route via Google Directions using the server-side API key. */
  async route(origin: string, dest: string): Promise<{
    distanceMetres: number
    originLat: number
    originLng: number
    destLat: number
    destLng: number
  }> {
    const key = this.googleMapsKey()
    if (!key) throw new BadRequestException('Route calculation is not configured on this server')

    const cleanOrigin = origin?.trim()
    const cleanDest = dest?.trim()
    if (!cleanOrigin || !cleanDest) {
      throw new BadRequestException('origin and dest are required')
    }

    const url = new URL('https://maps.googleapis.com/maps/api/directions/json')
    url.searchParams.set('origin', cleanOrigin)
    url.searchParams.set('destination', cleanDest)
    url.searchParams.set('mode', 'driving')
    url.searchParams.set('key', key)

    const res = await fetch(url)
    if (!res.ok) throw new BadRequestException('Route calculation request failed')

    const json = await res.json() as {
      status: string
      error_message?: string
      routes: Array<{
        legs: Array<{
          distance: { value: number }
          start_location: { lat: number; lng: number }
          end_location: { lat: number; lng: number }
        }>
      }>
    }

    const leg = json.routes[0]?.legs[0]
    if (json.status !== 'OK' || !leg) {
      throw new BadRequestException(json.error_message ?? 'No driving route found between the selected addresses')
    }

    return {
      distanceMetres: leg.distance.value,
      originLat: leg.start_location.lat,
      originLng: leg.start_location.lng,
      destLat: leg.end_location.lat,
      destLng: leg.end_location.lng,
    }
  }

  private googleMapsKey(): string | undefined {
    return this.config.get<string>('GOOGLE_MAPS_API_KEY') ?? this.config.get<string>('MAPS_API_KEY')
  }
}
