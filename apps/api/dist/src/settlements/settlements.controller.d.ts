import { SettlementsService } from './settlements.service';
import { DisputeDto } from './dto/dispute.dto';
export declare class SettlementsController {
    private settlements;
    constructor(settlements: SettlementsService);
    getForTrip(tripId: string, req: any): Promise<{
        bookingId: string;
        passengerFirstName: string;
        passengerLastName: string;
        suggestedAmountOere: number;
        passengerPaidAt: Date | null;
        driverConfirmedAt: Date | null;
        bookingStatus: import("@prisma/client").$Enums.BookingStatus;
    }[]>;
    get(bookingId: string, req: any): Promise<{
        tripId: string | null;
        createdAt: Date;
        mobilepayNumber: string;
        bookingId: string;
        suggestedAmountOere: number;
        passengerPaidAt: Date | null;
        driverConfirmedAt: Date | null;
        disputedAt: Date | null;
        disputeReason: string | null;
    }>;
    markPaid(bookingId: string, req: any): Promise<{
        createdAt: Date;
        mobilepayNumber: string;
        bookingId: string;
        suggestedAmountOere: number;
        passengerPaidAt: Date | null;
        driverConfirmedAt: Date | null;
        disputedAt: Date | null;
        disputeReason: string | null;
    }>;
    unmarkPaid(bookingId: string, req: any): Promise<{
        createdAt: Date;
        mobilepayNumber: string;
        bookingId: string;
        suggestedAmountOere: number;
        passengerPaidAt: Date | null;
        driverConfirmedAt: Date | null;
        disputedAt: Date | null;
        disputeReason: string | null;
    }>;
    confirmReceived(bookingId: string, req: any): Promise<{
        createdAt: Date;
        mobilepayNumber: string;
        bookingId: string;
        suggestedAmountOere: number;
        passengerPaidAt: Date | null;
        driverConfirmedAt: Date | null;
        disputedAt: Date | null;
        disputeReason: string | null;
    }>;
    dispute(bookingId: string, dto: DisputeDto, req: any): Promise<{
        createdAt: Date;
        mobilepayNumber: string;
        bookingId: string;
        suggestedAmountOere: number;
        passengerPaidAt: Date | null;
        driverConfirmedAt: Date | null;
        disputedAt: Date | null;
        disputeReason: string | null;
    }>;
}
