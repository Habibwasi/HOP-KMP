import {
  Controller, Post, Body, Param, UseGuards,
  Req, Headers, HttpCode,
} from '@nestjs/common'
import type { RawBodyRequest } from '@nestjs/common'
import { AuthGuard } from '@nestjs/passport'
import { PaymentsService } from './payments.service'
import { InitiatePaymentDto } from './dto/initiate-payment.dto'

@Controller('payments')
export class PaymentsController {
  constructor(private payments: PaymentsService) {}

  @Post('initiate')
  @UseGuards(AuthGuard('jwt'))
  initiate(@Body() dto: InitiatePaymentDto) {
    return this.payments.initiatePayment(dto.bookingId, dto.provider)
  }

  @Post('capture/:bookingId')
  @UseGuards(AuthGuard('jwt'))
  @HttpCode(200)
  capture(@Param('bookingId') bookingId: string) {
    return this.payments.capturePayment(bookingId)
  }

  @Post('refund/:bookingId')
  @UseGuards(AuthGuard('jwt'))
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
  mobilepayWebhook(@Body() body: any) {
    return this.payments.handleMobilepayWebhook(body)
  }
}
