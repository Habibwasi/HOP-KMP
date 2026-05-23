import { SettlementsService } from './settlements.service';
import { DisputeDto } from './dto/dispute.dto';
export declare class SettlementsController {
    private settlements;
    constructor(settlements: SettlementsService);
    get(bookingId: string, req: any): Promise<{
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
