import { Module } from '@nestjs/common'
import { BullModule } from '@nestjs/bullmq'
import { TripsService } from './trips.service'
import { TripsController } from './trips.controller'
import { PricingService } from './pricing.service'
import { SEARCH_ALERTS_QUEUE } from '../search-alerts/search-alerts.processor'

@Module({
  imports: [BullModule.registerQueue({ name: SEARCH_ALERTS_QUEUE })],
  providers: [TripsService, PricingService],
  controllers: [TripsController],
  exports: [TripsService],
})
export class TripsModule {}
