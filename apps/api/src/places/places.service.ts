import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common'
import { PrismaService } from '../prisma/prisma.service'
import { UpsertPlaceDto } from './places.controller'

@Injectable()
export class PlacesService {
  constructor(private readonly prisma: PrismaService) {}

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
}
