import { Controller, Get, Post, Param, Body, UseGuards, Req, HttpCode } from '@nestjs/common'
import { SupabaseGuard } from '../auth/supabase.guard'
import { SettlementsService } from './settlements.service'
import { DisputeDto } from './dto/dispute.dto'

@Controller('settlements')
@UseGuards(SupabaseGuard)
export class SettlementsController {
  constructor(private settlements: SettlementsService) {}

  @Get('trip/:tripId')
  getForTrip(@Param('tripId') tripId: string, @Req() req: any) {
    return this.settlements.getSettlementsForTrip(tripId, req.user.id)
  }

  @Get(':bookingId')
  get(@Param('bookingId') bookingId: string, @Req() req: any) {
    return this.settlements.getSettlement(bookingId, req.user.id)
  }

  @Post(':bookingId/mark-paid')
  @HttpCode(200)
  markPaid(@Param('bookingId') bookingId: string, @Req() req: any) {
    return this.settlements.markPassengerPaid(bookingId, req.user.id)
  }

  @Post(':bookingId/unmark-paid')
  @HttpCode(200)
  unmarkPaid(@Param('bookingId') bookingId: string, @Req() req: any) {
    return this.settlements.unmarkPaid(bookingId, req.user.id)
  }

  @Post(':bookingId/confirm-received')
  @HttpCode(200)
  confirmReceived(@Param('bookingId') bookingId: string, @Req() req: any) {
    return this.settlements.markDriverConfirmed(bookingId, req.user.id)
  }

  @Post(':bookingId/dispute')
  @HttpCode(200)
  dispute(
    @Param('bookingId') bookingId: string,
    @Body() dto: DisputeDto,
    @Req() req: any,
  ) {
    return this.settlements.dispute(bookingId, req.user.id, dto.reason)
  }
}
