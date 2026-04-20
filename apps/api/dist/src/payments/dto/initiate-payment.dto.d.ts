import { PaymentProvider } from '@prisma/client';
export declare class InitiatePaymentDto {
    bookingId: string;
    provider: PaymentProvider;
}
