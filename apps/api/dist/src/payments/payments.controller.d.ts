import type { RawBodyRequest } from '@nestjs/common';
import { PaymentsService } from './payments.service';
import { InitiatePaymentDto } from './dto/initiate-payment.dto';
export declare class PaymentsController {
    private payments;
    constructor(payments: PaymentsService);
    initiate(dto: InitiatePaymentDto): Promise<{
        paymentId: string;
        provider: string;
        redirectUrl: string;
        amountOere: any;
    } | {
        clientSecret: string | null;
        paymentIntentId: string;
        amountOere: any;
    }>;
    capture(bookingId: string): Promise<{
        captured: boolean;
    }>;
    refund(bookingId: string): Promise<{
        refunded: boolean;
    }>;
    stripeWebhook(req: RawBodyRequest<any>, sig: string): Promise<{
        received: boolean;
    }>;
    mobilepayWebhook(req: RawBodyRequest<any>, signature: string): Promise<{
        received: boolean;
    }>;
}
