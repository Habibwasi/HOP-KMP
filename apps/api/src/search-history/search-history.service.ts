import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common'
import { PrismaService } from '../prisma/prisma.service'

@Injectable()
export class SearchHistoryService {
  constructor(private readonly prisma: PrismaService) {}

  list(userId: string, limit: number) {
    return this.prisma.recentSearch.findMany({
      where: { userId },
      orderBy: { lastUsedAt: 'desc' },
      take: limit,
    })
  }

  /**
   * Idempotent upsert keyed on (userId, origin, dest).
   * Bumps lastUsedAt and increments useCount on repeat.
   */
  record(userId: string, originLabel: string, destLabel: string) {
    return this.prisma.recentSearch.upsert({
      where: {
        userId_originLabel_destLabel: {
          userId,
          originLabel,
          destLabel,
        },
      },
      create: { userId, originLabel, destLabel },
      update: {
        lastUsedAt: new Date(),
        useCount: { increment: 1 },
      },
    })
  }

  async remove(userId: string, id: string) {
    const row = await this.prisma.recentSearch.findUnique({ where: { id } })
    if (!row) throw new NotFoundException('Search not found')
    if (row.userId !== userId) throw new ForbiddenException()
    await this.prisma.recentSearch.delete({ where: { id } })
    return { ok: true }
  }
}
