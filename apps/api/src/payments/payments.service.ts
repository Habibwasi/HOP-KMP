import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common'
import { ConfigService } from '@nestjs/config'
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
    private bookings: BookingsService,
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

  private async initiateMobilepay(booking: any) {
    // MobilePay ePayment — create payment intent
    // Returns a deeplink/redirect URL for the app to open
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

    // TODO: call MobilePay ePayment API to create payment
    // POST https://api.mobilepay.dk/v1/payments
    // Returns { paymentId, mobilePayAppRedirectUri }
    // Store paymentId as providerRef

    return {
      paymentId: payment.id,
      provider: 'MOBILEPAY',
      redirectUrl: 'mobilepay://TODO', // replace with actual MobilePay deeplink
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
    }
    // TODO: MobilePay capture via API

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
    }
    // TODO: MobilePay refund via API

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

  async handleMobilepayWebhook(body: any) {
    // TODO: verify MobilePay webhook signature
    // Handle events: payment.reserved, payment.cancelled, payment.captured
    const { eventType, data } = body
    const bookingId = data?.reference

    if (!bookingId) return { received: true }

    if (eventType === 'payment.reserved') {
      await this.prisma.payment.update({
        where: { bookingId },
        data: { status: PaymentStatus.HELD, providerRef: data.paymentId },
      })
      await this.bookings.confirm(bookingId)
    }

    if (eventType === 'payment.cancelled') {
      await this.prisma.payment.update({
        where: { bookingId },
        data: { status: PaymentStatus.FAILED },
      })
    }

    return { received: true }
  }
}
