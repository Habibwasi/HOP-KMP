import { Module } from '@nestjs/common'
import { BullModule } from '@nestjs/bullmq'
import { BookingsService } from './bookings.service'
import { BookingsController } from './bookings.controller'
import { BookingsProcessor } from './bookings.processor'

@Module({
  imports: [
    BullModule.registerQueue({ name: 'bookings' }),
  ],
  providers: [BookingsService, BookingsProcessor],
  controllers: [BookingsController],
  exports: [BookingsService],
})
export class BookingsModule {}
