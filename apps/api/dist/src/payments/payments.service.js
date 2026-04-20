"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.PaymentsService = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const prisma_service_1 = require("../prisma/prisma.service");
const bookings_service_1 = require("../bookings/bookings.service");
const client_1 = require("@prisma/client");
const stripe_1 = __importDefault(require("stripe"));
let PaymentsService = class PaymentsService {
    prisma;
    bookings;
    config;
    stripe;
    constructor(prisma, bookings, config) {
        this.prisma = prisma;
        this.bookings = bookings;
        this.config = config;
        this.stripe = new stripe_1.default(config.getOrThrow('STRIPE_SECRET_KEY'));
    }
    async initiatePayment(bookingId, provider) {
        const booking = await this.prisma.booking.findUnique({
            where: { id: bookingId },
            include: { trip: true, passenger: true },
        });
        if (!booking)
            throw new common_1.NotFoundException('Booking not found');
        if (booking.status !== client_1.BookingStatus.PENDING) {
            throw new common_1.BadRequestException('Booking is not in PENDING state');
        }
        if (provider === client_1.PaymentProvider.MOBILEPAY) {
            return this.initiateMobilepay(booking);
        }
        return this.initiateStripe(booking);
    }
    async initiateMobilepay(booking) {
        const payment = await this.prisma.payment.upsert({
            where: { bookingId: booking.id },
            create: {
                bookingId: booking.id,
                amountOere: booking.totalOere,
                provider: client_1.PaymentProvider.MOBILEPAY,
                status: client_1.PaymentStatus.PENDING,
            },
            update: { status: client_1.PaymentStatus.PENDING },
        });
        return {
            paymentId: payment.id,
            provider: 'MOBILEPAY',
            redirectUrl: 'mobilepay://TODO',
            amountOere: booking.totalOere,
        };
    }
    async initiateStripe(booking) {
        const paymentIntent = await this.stripe.paymentIntents.create({
            amount: booking.totalOere,
            currency: 'dkk',
            metadata: { bookingId: booking.id },
            capture_method: 'manual',
        });
        await this.prisma.payment.upsert({
            where: { bookingId: booking.id },
            create: {
                bookingId: booking.id,
                amountOere: booking.totalOere,
                provider: client_1.PaymentProvider.STRIPE,
                status: client_1.PaymentStatus.HELD,
                providerRef: paymentIntent.id,
            },
            update: {
                status: client_1.PaymentStatus.HELD,
                providerRef: paymentIntent.id,
            },
        });
        return {
            clientSecret: paymentIntent.client_secret,
            paymentIntentId: paymentIntent.id,
            amountOere: booking.totalOere,
        };
    }
    async capturePayment(bookingId) {
        const payment = await this.prisma.payment.findUnique({ where: { bookingId } });
        if (!payment)
            throw new common_1.NotFoundException('Payment not found');
        if (payment.provider === client_1.PaymentProvider.STRIPE && payment.providerRef) {
            await this.stripe.paymentIntents.capture(payment.providerRef);
        }
        await this.prisma.payment.update({
            where: { bookingId },
            data: { status: client_1.PaymentStatus.CAPTURED },
        });
        await this.bookings.confirm(bookingId);
        const booking = await this.prisma.booking.findUnique({
            where: { id: bookingId },
            include: { trip: true },
        });
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
            });
        }
        return { captured: true };
    }
    async refundPayment(bookingId) {
        const payment = await this.prisma.payment.findUnique({ where: { bookingId } });
        if (!payment)
            throw new common_1.NotFoundException('Payment not found');
        if (payment.status === client_1.PaymentStatus.REFUNDED) {
            throw new common_1.BadRequestException('Already refunded');
        }
        if (payment.provider === client_1.PaymentProvider.STRIPE && payment.providerRef) {
            await this.stripe.refunds.create({ payment_intent: payment.providerRef });
        }
        await this.prisma.payment.update({
            where: { bookingId },
            data: { status: client_1.PaymentStatus.REFUNDED },
        });
        return { refunded: true };
    }
    async handleStripeWebhook(rawBody, signature) {
        const secret = this.config.getOrThrow('STRIPE_WEBHOOK_SECRET');
        let event;
        try {
            event = this.stripe.webhooks.constructEvent(rawBody, signature, secret);
        }
        catch {
            throw new common_1.BadRequestException('Invalid Stripe webhook signature');
        }
        if (event.type === 'payment_intent.amount_capturable_updated') {
            const intent = event.data.object;
            const bookingId = intent.metadata.bookingId;
            if (bookingId) {
                await this.prisma.payment.update({
                    where: { bookingId },
                    data: { status: client_1.PaymentStatus.HELD },
                });
                await this.bookings.confirm(bookingId);
            }
        }
        return { received: true };
    }
    async handleMobilepayWebhook(body) {
        const { eventType, data } = body;
        const bookingId = data?.reference;
        if (!bookingId)
            return { received: true };
        if (eventType === 'payment.reserved') {
            await this.prisma.payment.update({
                where: { bookingId },
                data: { status: client_1.PaymentStatus.HELD, providerRef: data.paymentId },
            });
            await this.bookings.confirm(bookingId);
        }
        if (eventType === 'payment.cancelled') {
            await this.prisma.payment.update({
                where: { bookingId },
                data: { status: client_1.PaymentStatus.FAILED },
            });
        }
        return { received: true };
    }
};
exports.PaymentsService = PaymentsService;
exports.PaymentsService = PaymentsService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        bookings_service_1.BookingsService,
        config_1.ConfigService])
], PaymentsService);
//# sourceMappingURL=payments.service.js.map