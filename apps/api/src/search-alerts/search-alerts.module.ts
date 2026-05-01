import { Module } from '@nestjs/common'
import { BullModule } from '@nestjs/bullmq'
import { PrismaModule } from '../prisma/prisma.module'
import { AuthModule } from '../auth/auth.module'
import { NotificationsModule } from '../notifications/notifications.module'
import { SearchAlertsController } from './search-alerts.controller'
import { SearchAlertsService } from './search-alerts.service'
import { SearchAlertsProcessor, SEARCH_ALERTS_QUEUE } from './search-alerts.processor'

@Module({
  imports: [
    PrismaModule,
    AuthModule,
    NotificationsModule,
    BullModule.registerQueue({ name: SEARCH_ALERTS_QUEUE }),
  ],
  controllers: [SearchAlertsController],
  providers: [SearchAlertsService, SearchAlertsProcessor],
  exports: [SearchAlertsService, BullModule],
})
export class SearchAlertsModule {}
