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
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.PaymentsService = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const crypto_1 = require("crypto");
const prisma_service_1 = require("../prisma/prisma.service");
const bookings_service_1 = require("../bookings/bookings.service");
const client_1 = require("@prisma/client");
const stripe_1 = __importDefault(require("stripe"));
const tax_constants_1 = require("../common/tax-constants");
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
    async getVippsAccessToken() {
        const base = this.config.getOrThrow('VIPPS_API_BASE');
        const res = await fetch(`${base}/accesstoken/get`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'client_id': this.config.getOrThrow('VIPPS_CLIENT_ID'),
                'client_secret': this.config.getOrThrow('VIPPS_CLIENT_SECRET'),
                'Ocp-Apim-Subscription-Key': this.config.getOrThrow('VIPPS_SUBSCRIPTION_KEY'),
                'Merchant-Serial-Number': this.config.getOrThrow('VIPPS_MSN'),
            },
        });
        if (!res.ok) {
            const text = await res.text();
            throw new common_1.BadRequestException(`Vipps token exchange failed: ${text}`);
        }
        const json = await res.json();
        return json.access_token;
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
        const token = await this.getVippsAccessToken();
        const base = this.config.getOrThrow('VIPPS_API_BASE');
        const msn = this.config.getOrThrow('VIPPS_MSN');
        const subscriptionKey = this.config.getOrThrow('VIPPS_SUBSCRIPTION_KEY');
        const returnUrl = this.config.get('VIPPS_RETURN_URL', 'ridly://payments/callback');
        const body = {
            amount: { currency: 'DKK', value: booking.totalOere },
            merchantSerialNumber: msn,
            reference: payment.id,
            userFlow: 'NATIVE_REDIRECT',
            returnUrl,
            paymentMethod: { type: 'WALLET' },
            paymentDescription: `Ridly ride booking ${booking.id}`,
        };
        const res = await fetch(`${base}/epayment/v1/payments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`,
                'Ocp-Apim-Subscription-Key': subscriptionKey,
                'Merchant-Serial-Number': msn,
            },
            body: JSON.stringify(body),
        });
        if (!res.ok) {
            const text = await res.text();
            throw new common_1.BadRequestException(`Vipps payment creation failed: ${text}`);
        }
        const json = await res.json();
        await this.prisma.payment.update({
            where: { bookingId: booking.id },
            data: { providerRef: json.reference },
        });
        return {
            paymentId: payment.id,
            provider: 'MOBILEPAY',
            redirectUrl: json.redirectUrl,
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
    async capturePayment(bookingId, requestingUserId) {
        const payment = await this.prisma.payment.findUnique({ where: { bookingId } });
        if (!payment)
            throw new common_1.NotFoundException('Payment not found');
        if (requestingUserId) {
            const booking = await this.prisma.booking.findUnique({
                where: { id: bookingId },
                include: { trip: { select: { driverId: true } } },
            });
            if (!booking)
                throw new common_1.NotFoundException('Booking not found');
            if (booking.trip.driverId !== requestingUserId) {
                throw new common_1.ForbiddenException('Only the driver of this trip can capture payment');
            }
        }
        if (payment.provider === client_1.PaymentProvider.STRIPE && payment.providerRef) {
            await this.stripe.paymentIntents.capture(payment.providerRef);
        }
        else if (payment.provider === client_1.PaymentProvider.MOBILEPAY && payment.providerRef) {
            const token = await this.getVippsAccessToken();
            const base = this.config.getOrThrow('VIPPS_API_BASE');
            const msn = this.config.getOrThrow('VIPPS_MSN');
            const subscriptionKey = this.config.getOrThrow('VIPPS_SUBSCRIPTION_KEY');
            const res = await fetch(`${base}/epayment/v1/payments/${payment.providerRef}/capture`, {
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
            });
            if (!res.ok) {
                const text = await res.text();
                throw new common_1.BadRequestException(`Vipps capture failed: ${text}`);
            }
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
                    ratePerKm: tax_constants_1.SKAT_RATE_DKK_PER_KM,
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
        else if (payment.provider === client_1.PaymentProvider.MOBILEPAY && payment.providerRef) {
            const token = await this.getVippsAccessToken();
            const base = this.config.getOrThrow('VIPPS_API_BASE');
            const msn = this.config.getOrThrow('VIPPS_MSN');
            const subscriptionKey = this.config.getOrThrow('VIPPS_SUBSCRIPTION_KEY');
            const res = await fetch(`${base}/epayment/v1/payments/${payment.providerRef}/refund`, {
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
            });
            if (!res.ok) {
                const text = await res.text();
                throw new common_1.BadRequestException(`Vipps refund failed: ${text}`);
            }
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
    async handleMobilepayWebhook(rawBody, signature) {
        const secret = this.config.getOrThrow('VIPPS_WEBHOOK_SECRET');
        const expected = (0, crypto_1.createHmac)('sha256', secret).update(rawBody).digest('hex');
        const received = Buffer.from(signature, 'hex');
        const expectedBuf = Buffer.from(expected, 'hex');
        if (received.length !== expectedBuf.length ||
            !(0, crypto_1.timingSafeEqual)(received, expectedBuf)) {
            throw new common_1.BadRequestException('Invalid MobilePay webhook signature');
        }
        const body = JSON.parse(rawBody.toString());
        const { eventType, data } = body;
        const providerRef = data?.reference;
        if (!providerRef)
            return { received: true };
        const payment = await this.prisma.payment.findFirst({ where: { providerRef } });
        if (!payment)
            return { received: true };
        const bookingId = payment.bookingId;
        if (eventType === 'epayments.payment.reserved.v1') {
            await this.prisma.payment.update({
                where: { bookingId },
                data: { status: client_1.PaymentStatus.HELD },
            });
            await this.bookings.confirm(bookingId);
        }
        else if (eventType === 'epayments.payment.cancelled.v1') {
            await this.prisma.payment.update({
                where: { bookingId },
                data: { status: client_1.PaymentStatus.FAILED },
            });
        }
        else if (eventType === 'epayments.payment.captured.v1') {
            await this.prisma.payment.update({
                where: { bookingId },
                data: { status: client_1.PaymentStatus.CAPTURED },
            });
        }
        return { received: true };
    }
};
exports.PaymentsService = PaymentsService;
exports.PaymentsService = PaymentsService = __decorate([
    (0, common_1.Injectable)(),
    __param(1, (0, common_1.Inject)((0, common_1.forwardRef)(() => bookings_service_1.BookingsService))),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        bookings_service_1.BookingsService,
        config_1.ConfigService])
], PaymentsService);
//# sourceMappingURL=payments.service.js.map