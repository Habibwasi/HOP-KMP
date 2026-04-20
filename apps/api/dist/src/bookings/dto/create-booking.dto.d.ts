import { PaymentProvider } from '@prisma/client';
export declare class CreateBookingDto {
    tripId: string;
    seats: number;
    paymentProvider: PaymentProvider;
}
