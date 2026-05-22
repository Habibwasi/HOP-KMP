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

  private static readonly MAX_RECENT_SEARCHES = 10

  /**
   * Idempotent upsert keyed on (userId, origin, dest).
   * Bumps lastUsedAt and increments useCount on repeat.
   * After upsert, trims to the MAX_RECENT_SEARCHES most-recent rows for
   * the user so the table does not grow unboundedly.
   */
  async record(userId: string, originLabel: string, destLabel: string) {
    const row = await this.prisma.recentSearch.upsert({
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

    // Keep only the most-recent MAX_RECENT_SEARCHES rows; delete the rest.
    const overflow = await this.prisma.recentSearch.findMany({
      where: { userId },
      orderBy: { lastUsedAt: 'desc' },
      skip: SearchHistoryService.MAX_RECENT_SEARCHES,
      select: { id: true },
    })

    if (overflow.length > 0) {
      await this.prisma.recentSearch.deleteMany({
        where: { id: { in: overflow.map((r) => r.id) } },
      })
    }

    return row
  }

  async remove(userId: string, id: string) {
    const row = await this.prisma.recentSearch.findUnique({ where: { id } })
    if (!row) throw new NotFoundException('Search not found')
    if (row.userId !== userId) throw new ForbiddenException()
    await this.prisma.recentSearch.delete({ where: { id } })
    return { ok: true }
  }
}
