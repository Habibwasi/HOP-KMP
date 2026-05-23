import { Module, forwardRef } from '@nestjs/common'
import { BullModule } from '@nestjs/bullmq'
import { BookingsService } from './bookings.service'
import { BookingsController } from './bookings.controller'
import { BookingsProcessor } from './bookings.processor'
import { NotificationsModule } from '../notifications/notifications.module'
import { RatingsModule } from '../ratings/ratings.module'

@Module({
  imports: [
    BullModule.registerQueue({ name: 'bookings' }),
    NotificationsModule,
    RatingsModule,
  ],
  providers: [BookingsService, BookingsProcessor],
  controllers: [BookingsController],
  exports: [BookingsService],
})
export class BookingsModule {}
