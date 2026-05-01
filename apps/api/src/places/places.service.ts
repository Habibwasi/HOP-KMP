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
    const key = this.config.get<string>('GOOGLE_MAPS_API_KEY')
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
}
