import { Controller, Get, Query, Req, UseGuards } from '@nestjs/common'
import { AppException } from '../common/errors/app-exception'
import { ApiErrorCode } from '../common/errors/api-error-codes'
import { SupabaseGuard } from '../auth/supabase.guard'
import { AggregatesService } from './aggregates.service'

/**
 * Read-side aggregate endpoints powering the Driver home hero cards and the
 * shared CO₂ stat. Computed on demand from existing tables — no cron job.
 */
@Controller('aggregates')
export class AggregatesController {
  constructor(private readonly aggregates: AggregatesService) {}

  @Get('driver/earnings-series')
  @UseGuards(SupabaseGuard)
  async driverEarningsSeries(
    @Req() req: any,
    @Query('days') daysStr?: string,
  ) {
    const days = daysStr ? parseInt(daysStr, 10) : 7
    if (isNaN(days) || days < 1 || days > 30) {
      throw new AppException(ApiErrorCode.INVALID_PARAM, 'days must be between 1 and 30')
    }
    return this.aggregates.getDriverEarningsSeries(req.user.id, days)
  }

  @Get('popular-routes')
  async popularRoutes(@Query('limit') limitStr?: string) {
    const limit = limitStr ? parseInt(limitStr, 10) : 5
    if (isNaN(limit) || limit < 1 || limit > 50) {
      throw new AppException(ApiErrorCode.INVALID_PARAM, 'limit must be between 1 and 50')
    }
    return this.aggregates.getPopularRoutes(limit)
  }

  @Get('demand-hotspots')
  async demandHotspots() {
    return this.aggregates.getDemandHotspots()
  }

  @Get('co2-saved')
  @UseGuards(SupabaseGuard)
  async co2Saved(@Req() req: any) {
    return this.aggregates.getUserCo2Saved(req.user.id)
  }
}
