import { PrismaService } from '../prisma/prisma.service';
import { NotificationsService } from '../notifications/notifications.service';
export declare class SettlementsService {
    private prisma;
    private notifications;
    constructor(prisma: PrismaService, notifications: NotificationsService);
    private assertParty;
    getSettlement(bookingId: string, userId: string): Promise<{
        createdAt: Date;
        mobilepayNumber: string;
        bookingId: string;
        suggestedAmountOere: number;
        passengerPaidAt: Date | null;
        driverConfirmedAt: Date | null;
        disputedAt: Date | null;
        disputeReason: string | null;
    }>;
    markPassengerPaid(bookingId: string, userId: string): Promise<{
        createdAt: Date;
        mobilepayNumber: string;
        bookingId: string;
        suggestedAmountOere: number;
        passengerPaidAt: Date | null;
        driverConfirmedAt: Date | null;
        disputedAt: Date | null;
        disputeReason: string | null;
    }>;
    markDriverConfirmed(bookingId: string, userId: string): Promise<{
        createdAt: Date;
        mobilepayNumber: string;
        bookingId: string;
        suggestedAmountOere: number;
        passengerPaidAt: Date | null;
        driverConfirmedAt: Date | null;
        disputedAt: Date | null;
        disputeReason: string | null;
    }>;
    dispute(bookingId: string, userId: string, reason: string): Promise<{
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
