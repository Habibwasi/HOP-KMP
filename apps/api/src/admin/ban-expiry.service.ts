import { Injectable, Logger } from '@nestjs/common'
import { Cron, CronExpression } from '@nestjs/schedule'
import { PrismaService } from '../prisma/prisma.service'

/**
 * Automatically lifts timed bans once banExpiresAt has passed.
 * Runs every 15 minutes and bulk-updates expired ban records.
 */
@Injectable()
export class BanExpiryService {
  private readonly logger = new Logger(BanExpiryService.name)

  constructor(private readonly prisma: PrismaService) {}

  @Cron(CronExpression.EVERY_10_MINUTES)
  async liftExpiredBans(): Promise<void> {
    const result = await this.prisma.user.updateMany({
      where: {
        isBanned: true,
        banExpiresAt: { lte: new Date() },
      },
      data: {
        isBanned: false,
        banExpiresAt: null,
      },
    })

    if (result.count > 0) {
      this.logger.log(`Lifted ${result.count} expired ban(s)`)
    }
  }
}
