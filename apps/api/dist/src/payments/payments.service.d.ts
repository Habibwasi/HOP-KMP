import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../prisma/prisma.service';
import { BookingsService } from '../bookings/bookings.service';
import { PaymentProvider } from '@prisma/client';
export declare class PaymentsService {
    private prisma;
    private bookings;
    private config;
    private stripe;
    constructor(prisma: PrismaService, bookings: BookingsService, config: ConfigService);
    initiatePayment(bookingId: string, provider: PaymentProvider): Promise<{
        paymentId: string;
        provider: string;
        redirectUrl: string;
        amountOere: any;
    } | {
        clientSecret: string | null;
        paymentIntentId: string;
        amountOere: any;
    }>;
    private initiateMobilepay;
    private initiateStripe;
    capturePayment(bookingId: string): Promise<{
        captured: boolean;
    }>;
    refundPayment(bookingId: string): Promise<{
        refunded: boolean;
    }>;
    handleStripeWebhook(rawBody: Buffer, signature: string): Promise<{
        received: boolean;
    }>;
    handleMobilepayWebhook(body: any): Promise<{
        received: boolean;
    }>;
}
