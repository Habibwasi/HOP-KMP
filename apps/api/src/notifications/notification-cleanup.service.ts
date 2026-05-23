import { Injectable, Logger } from '@nestjs/common'
import { Cron, CronExpression } from '@nestjs/schedule'
import { PrismaService } from '../prisma/prisma.service'

/**
 * Deletes notifications older than 90 days once per day at midnight.
 * Prevents the Notification table from growing unboundedly.
 */
@Injectable()
export class NotificationCleanupService {
  private readonly logger = new Logger(NotificationCleanupService.name)

  constructor(private readonly prisma: PrismaService) {}

  @Cron(CronExpression.EVERY_DAY_AT_MIDNIGHT)
  async deleteOldNotifications(): Promise<void> {
    const cutoff = new Date()
    cutoff.setDate(cutoff.getDate() - 90)

    const result = await this.prisma.notification.deleteMany({
      where: { createdAt: { lt: cutoff } },
    })

    if (result.count > 0) {
      this.logger.log(`Deleted ${result.count} notification(s) older than 90 days`)
    }
  }
}
