import { Module, OnModuleInit } from '@nestjs/common'
import { InjectQueue } from '@nestjs/bullmq'
import { BullModule } from '@nestjs/bullmq'
import { Queue } from 'bullmq'
import { TripsService } from './trips.service'
import { TripsController } from './trips.controller'
import { PricingService } from './pricing.service'
import { TripsProcessor, TRIPS_QUEUE, EXTEND_RECURRING_JOB } from './trips.processor'
import { SEARCH_ALERTS_QUEUE } from '../search-alerts/search-alerts.processor'
import { BOOKINGS_QUEUE } from './trips.constants'

export { BOOKINGS_QUEUE, CHECK_THRESHOLD_JOB } from './trips.constants'

@Module({
  imports: [
    BullModule.registerQueue({ name: SEARCH_ALERTS_QUEUE }),
    BullModule.registerQueue({ name: BOOKINGS_QUEUE }),
    BullModule.registerQueue({ name: TRIPS_QUEUE }),
  ],
  providers: [TripsService, PricingService, TripsProcessor],
  controllers: [TripsController],
  exports: [TripsService],
})
export class TripsModule implements OnModuleInit {
  constructor(@InjectQueue(TRIPS_QUEUE) private tripsQueue: Queue) {}

  async onModuleInit() {
    // Schedule a daily repeatable job to extend the 30-day recurring window.
    await this.tripsQueue.add(
      EXTEND_RECURRING_JOB,
      {},
      {
        repeat: { pattern: '0 3 * * *' }, // 03:00 UTC daily
        jobId: 'extend-recurring-daily',
        removeOnComplete: true,
      },
    )
  }
}
