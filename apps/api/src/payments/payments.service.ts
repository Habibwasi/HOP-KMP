import { Injectable, NotFoundException, BadRequestException, Inject, forwardRef } from '@nestjs/common'
import { ConfigService } from '@nestjs/config'
import { createHmac, timingSafeEqual } from 'crypto'
import { PrismaService } from '../prisma/prisma.service'
import { BookingsService } from '../bookings/bookings.service'
import { PaymentProvider, PaymentStatus, BookingStatus } from '@prisma/client'
import Stripe from 'stripe'

type StripeEvent = ReturnType<InstanceType<typeof Stripe>['webhooks']['constructEvent']>
type StripePaymentIntent = StripeEvent['data']['object'] & { metadata: Record<string, string> }

@Injectable()
export class PaymentsService {
  private stripe: InstanceType<typeof Stripe>

  constructor(
    private prisma: PrismaService,
    @Inject(forwardRef(() => BookingsService)) private bookings: BookingsService,
    private config: ConfigService,
  ) {
    this.stripe = new Stripe(config.getOrThrow('STRIPE_SECRET_KEY'))
  }

  // ─── INITIATE ────────────────────────────────────────────────────────────────

  async initiatePayment(bookingId: string, provider: PaymentProvider) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      include: { trip: true, passenger: true },
    })
    if (!booking) throw new NotFoundException('Booking not found')
    if (booking.status !== BookingStatus.PENDING) {
      throw new BadRequestException('Booking is not in PENDING state')
    }

    if (provider === PaymentProvider.MOBILEPAY) {
      return this.initiateMobilepay(booking)
    }
    return this.initiateStripe(booking)
  }

  // ─── MOBILEPAY ───────────────────────────────────────────────────────────────

  /** Fetch a short-lived Vipps MobilePay access token via client-credentials. */
  private async getVippsAccessToken(): Promise<string> {
    const base = this.config.getOrThrow('VIPPS_API_BASE')
    const res = await fetch(`${base}/accesstoken/get`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'client_id': this.config.getOrThrow('VIPPS_CLIENT_ID'),
        'client_secret': this.config.getOrThrow('VIPPS_CLIENT_SECRET'),
        'Ocp-Apim-Subscription-Key': this.config.getOrThrow('VIPPS_SUBSCRIPTION_KEY'),
        'Merchant-Serial-Number': this.config.getOrThrow('VIPPS_MSN'),
      },
    })
    if (!res.ok) {
      const text = await res.text()
      throw new BadRequestException(`Vipps token exchange failed: ${text}`)
    }
    const json = await res.json() as { access_token: string }
    return json.access_token
  }

  private async initiateMobilepay(booking: any) {
    const payment = await this.prisma.payment.upsert({
      where: { bookingId: booking.id },
      create: {
        bookingId: booking.id,
        amountOere: booking.totalOere,
        provider: PaymentProvider.MOBILEPAY,
        status: PaymentStatus.PENDING,
      },
      update: { status: PaymentStatus.PENDING },
    })

    const token = await this.getVippsAccessToken()
    const base = this.config.getOrThrow('VIPPS_API_BASE')
    const msn = this.config.getOrThrow('VIPPS_MSN')
    const subscriptionKey = this.config.getOrThrow('VIPPS_SUBSCRIPTION_KEY')
    const returnUrl = this.config.get('VIPPS_RETURN_URL', 'hop://payments/callback')

    const body = {
      amount: { currency: 'DKK', value: booking.totalOere },
      merchantSerialNumber: msn,
      reference: payment.id,
      userFlow: 'NATIVE_REDIRECT',
      returnUrl,
      paymentMethod: { type: 'WALLET' },
      paymentDescription: `Hop ride booking ${booking.id}`,
    }

    const res = await fetch(`${base}/epayment/v1/payments`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
        'Ocp-Apim-Subscription-Key': subscriptionKey,
        'Merchant-Serial-Number': msn,
      },
      body: JSON.stringify(body),
    })

    if (!res.ok) {
      const text = await res.text()
      throw new BadRequestException(`Vipps payment creation failed: ${text}`)
    }

    const json = await res.json() as { reference: string; redirectUrl: string }

    // Persist the Vipps payment reference so we can capture/refund later.
    await this.prisma.payment.update({
      where: { bookingId: booking.id },
      data: { providerRef: json.reference },
    })

    return {
      paymentId: payment.id,
      provider: 'MOBILEPAY',
      redirectUrl: json.redirectUrl,
      amountOere: booking.totalOere,
    }
  }

  // ─── STRIPE ──────────────────────────────────────────────────────────────────

  private async initiateStripe(booking: any) {
    const paymentIntent = await this.stripe.paymentIntents.create({
      amount: booking.totalOere,
      currency: 'dkk',
      metadata: { bookingId: booking.id },
      capture_method: 'manual', // hold funds, capture after trip
    })

    await this.prisma.payment.upsert({
      where: { bookingId: booking.id },
      create: {
        bookingId: booking.id,
        amountOere: booking.totalOere,
        provider: PaymentProvider.STRIPE,
        status: PaymentStatus.HELD,
        providerRef: paymentIntent.id,
      },
      update: {
        status: PaymentStatus.HELD,
        providerRef: paymentIntent.id,
      },
    })

    return {
      clientSecret: paymentIntent.client_secret,
      paymentIntentId: paymentIntent.id,
      amountOere: booking.totalOere,
    }
  }

  // ─── CAPTURE (after trip completes) ─────────────────────────────────────────

  async capturePayment(bookingId: string) {
    const payment = await this.prisma.payment.findUnique({ where: { bookingId } })
    if (!payment) throw new NotFoundException('Payment not found')

    if (payment.provider === PaymentProvider.STRIPE && payment.providerRef) {
      await this.stripe.paymentIntents.capture(payment.providerRef)
    } else if (payment.provider === PaymentProvider.MOBILEPAY && payment.providerRef) {
      const token = await this.getVippsAccessToken()
      const base = this.config.getOrThrow('VIPPS_API_BASE')
      const msn = this.config.getOrThrow('VIPPS_MSN')
      const subscriptionKey = this.config.getOrThrow('VIPPS_SUBSCRIPTION_KEY')
      const res = await fetch(
        `${base}/epayment/v1/payments/${payment.providerRef}/capture`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
            'Ocp-Apim-Subscription-Key': subscriptionKey,
            'Merchant-Serial-Number': msn,
          },
          body: JSON.stringify({
            modificationAmount: { currency: 'DKK', value: payment.amountOere },
          }),
        },
      )
      if (!res.ok) {
        const text = await res.text()
        throw new BadRequestException(`Vipps capture failed: ${text}`)
      }
    }

    await this.prisma.payment.update({
      where: { bookingId },
      data: { status: PaymentStatus.CAPTURED },
    })

    // Confirm the booking
    await this.bookings.confirm(bookingId)

    // Create tax record
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      include: { trip: true },
    })
    if (booking?.trip) {
      await this.prisma.taxRecord.upsert({
        where: { bookingId },
        create: {
          driverId: booking.trip.driverId,
          tripId: booking.tripId,
          bookingId,
          amountOere: booking.totalOere,
          distanceKm: booking.trip.distanceKm ?? 0,
          ratePerKm: 0.27,
        },
        update: {},
      })
    }

    return { captured: true }
  }

  // ─── REFUND ──────────────────────────────────────────────────────────────────

  async refundPayment(bookingId: string) {
    const payment = await this.prisma.payment.findUnique({ where: { bookingId } })
    if (!payment) throw new NotFoundException('Payment not found')
    if (payment.status === PaymentStatus.REFUNDED) {
      throw new BadRequestException('Already refunded')
    }

    if (payment.provider === PaymentProvider.STRIPE && payment.providerRef) {
      await this.stripe.refunds.create({ payment_intent: payment.providerRef })
    } else if (payment.provider === PaymentProvider.MOBILEPAY && payment.providerRef) {
      const token = await this.getVippsAccessToken()
      const base = this.config.getOrThrow('VIPPS_API_BASE')
      const msn = this.config.getOrThrow('VIPPS_MSN')
      const subscriptionKey = this.config.getOrThrow('VIPPS_SUBSCRIPTION_KEY')
      const res = await fetch(
        `${base}/epayment/v1/payments/${payment.providerRef}/refund`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
            'Ocp-Apim-Subscription-Key': subscriptionKey,
            'Merchant-Serial-Number': msn,
          },
          body: JSON.stringify({
            modificationAmount: { currency: 'DKK', value: payment.amountOere },
          }),
        },
      )
      if (!res.ok) {
        const text = await res.text()
        throw new BadRequestException(`Vipps refund failed: ${text}`)
      }
    }

    await this.prisma.payment.update({
      where: { bookingId },
      data: { status: PaymentStatus.REFUNDED },
    })

    return { refunded: true }
  }

  // ─── WEBHOOKS ────────────────────────────────────────────────────────────────

  async handleStripeWebhook(rawBody: Buffer, signature: string) {
    const secret = this.config.getOrThrow('STRIPE_WEBHOOK_SECRET')
    let event: StripeEvent

    try {
      event = this.stripe.webhooks.constructEvent(rawBody, signature, secret)
    } catch {
      throw new BadRequestException('Invalid Stripe webhook signature')
    }

    if (event.type === 'payment_intent.amount_capturable_updated') {
      const intent = event.data.object as StripePaymentIntent
      const bookingId = intent.metadata.bookingId
      if (bookingId) {
        await this.prisma.payment.update({
          where: { bookingId },
          data: { status: PaymentStatus.HELD },
        })
        await this.bookings.confirm(bookingId)
      }
    }

    return { received: true }
  }

  async handleMobilepayWebhook(rawBody: Buffer, signature: string) {
    const secret = this.config.getOrThrow('VIPPS_WEBHOOK_SECRET')
    const expected = createHmac('sha256', secret).update(rawBody).digest('hex')
    const received = Buffer.from(signature, 'hex')
    const expectedBuf = Buffer.from(expected, 'hex')
    if (
      received.length !== expectedBuf.length ||
      !timingSafeEqual(received, expectedBuf)
    ) {
      throw new BadRequestException('Invalid MobilePay webhook signature')
    }

    const body = JSON.parse(rawBody.toString())
    const { eventType, data } = body as { eventType: string; data: Record<string, string> }
    // Vipps uses payment.id as the reference, which we stored in providerRef.
    const providerRef = data?.reference
    if (!providerRef) return { received: true }

    const payment = await this.prisma.payment.findFirst({ where: { providerRef } })
    if (!payment) return { received: true }
    const bookingId = payment.bookingId

    if (eventType === 'epayments.payment.reserved.v1') {
      await this.prisma.payment.update({
        where: { bookingId },
        data: { status: PaymentStatus.HELD },
      })
      await this.bookings.confirm(bookingId)
    } else if (eventType === 'epayments.payment.cancelled.v1') {
      await this.prisma.payment.update({
        where: { bookingId },
        data: { status: PaymentStatus.FAILED },
      })
    } else if (eventType === 'epayments.payment.captured.v1') {
      await this.prisma.payment.update({
        where: { bookingId },
        data: { status: PaymentStatus.CAPTURED },
      })
    }

    return { received: true }
  }
}
