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
   * Geocode a free-text address via Mapbox Geocoding API.
   * Returns { lat, lng, formattedAddress } or throws if nothing found.
   */
  async geocode(address: string): Promise<{ lat: number; lng: number; formattedAddress: string }> {
    const key = this.mapboxKey()
    if (!key) throw new BadRequestException('Geocoding is not configured on this server')

    const url = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(address)}.json?access_token=${key}&limit=1`
    const res = await fetch(url)
    if (!res.ok) throw new BadRequestException('Geocoding request failed')

    const json = await res.json() as {
      features: Array<{
        place_name: string
        center: [number, number] // [lng, lat]
      }>
    }

    if (!json.features.length) {
      throw new BadRequestException(`No geocoding result for: ${address}`)
    }

    const [lng, lat] = json.features[0].center
    return { lat, lng, formattedAddress: json.features[0].place_name }
  }

  /** Resolve a driving route via Mapbox Directions API. */
  async route(origin: string, dest: string): Promise<{
    distanceMetres: number
    originLat: number
    originLng: number
    destLat: number
    destLng: number
  }> {
    const key = this.mapboxKey()
    if (!key) throw new BadRequestException('Route calculation is not configured on this server')

    const cleanOrigin = origin?.trim()
    const cleanDest = dest?.trim()
    if (!cleanOrigin || !cleanDest) {
      throw new BadRequestException('origin and dest are required')
    }

    // Geocode both addresses first
    const [originGeo, destGeo] = await Promise.all([
      this.geocode(cleanOrigin),
      this.geocode(cleanDest),
    ])

    // Mapbox Directions: coordinates as "lng,lat;lng,lat"
    const coords = `${originGeo.lng},${originGeo.lat};${destGeo.lng},${destGeo.lat}`
    const url = `https://api.mapbox.com/directions/v5/mapbox/driving/${coords}?access_token=${key}&overview=false`

    const res = await fetch(url)
    if (!res.ok) throw new BadRequestException('Route calculation request failed')

    const json = await res.json() as {
      code: string
      message?: string
      routes: Array<{ distance: number }>
    }

    if (json.code !== 'Ok' || !json.routes.length) {
      throw new BadRequestException(json.message ?? `No driving route found (Mapbox code: ${json.code})`)
    }

    return {
      distanceMetres: Math.round(json.routes[0].distance),
      originLat: originGeo.lat,
      originLng: originGeo.lng,
      destLat: destGeo.lat,
      destLng: destGeo.lng,
    }
  }

  private mapboxKey(): string | undefined {
    return this.config.get<string>('MAPBOX_ACCESS_TOKEN')
  }
}
