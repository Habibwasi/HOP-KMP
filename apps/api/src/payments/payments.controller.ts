import {
  Controller, Post, Body, Param, UseGuards,
  Req, Headers, HttpCode, ForbiddenException,
} from '@nestjs/common'
import type { RawBodyRequest } from '@nestjs/common'
import { SupabaseGuard } from '../auth/supabase.guard'
import { PaymentsService } from './payments.service'
import { InitiatePaymentDto } from './dto/initiate-payment.dto'

@Controller('payments')
export class PaymentsController {
  constructor(private payments: PaymentsService) {}

  @Post('initiate')
  @UseGuards(SupabaseGuard)
  initiate(@Body() dto: InitiatePaymentDto) {
    return this.payments.initiatePayment(dto.bookingId, dto.provider)
  }

  @Post('capture/:bookingId')
  @UseGuards(SupabaseGuard)
  @HttpCode(200)
  async capture(@Param('bookingId') bookingId: string, @Req() req: any) {
    return this.payments.capturePayment(bookingId, req.user.id)
  }

  @Post('refund/:bookingId')
  @UseGuards(SupabaseGuard)
  @HttpCode(200)
  refund(@Param('bookingId') bookingId: string) {
    return this.payments.refundPayment(bookingId)
  }

  @Post('webhooks/stripe')
  @HttpCode(200)
  stripeWebhook(
    @Req() req: RawBodyRequest<any>,
    @Headers('stripe-signature') sig: string,
  ) {
    return this.payments.handleStripeWebhook(req.rawBody, sig)
  }

  @Post('webhooks/mobilepay')
  @HttpCode(200)
  mobilepayWebhook(
    @Req() req: RawBodyRequest<any>,
    @Headers('authorization') signature: string,
  ) {
    return this.payments.handleMobilepayWebhook(req.rawBody, signature ?? '')
  }
}
