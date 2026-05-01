import { Module, forwardRef } from '@nestjs/common'
import { BullModule } from '@nestjs/bullmq'
import { BookingsService } from './bookings.service'
import { BookingsController } from './bookings.controller'
import { BookingsProcessor } from './bookings.processor'
import { NotificationsModule } from '../notifications/notifications.module'
import { PaymentsModule } from '../payments/payments.module'

@Module({
  imports: [
    BullModule.registerQueue({ name: 'bookings' }),
    NotificationsModule,
    forwardRef(() => PaymentsModule),
  ],
  providers: [BookingsService, BookingsProcessor],
  controllers: [BookingsController],
  exports: [BookingsService],
})
export class BookingsModule {}
