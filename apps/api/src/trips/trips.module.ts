import { Module } from '@nestjs/common'
import { TripsService } from './trips.service'
import { TripsController } from './trips.controller'
import { PricingService } from './pricing.service'

@Module({
  providers: [TripsService, PricingService],
  controllers: [TripsController],
  exports: [TripsService],
})
export class TripsModule {}
