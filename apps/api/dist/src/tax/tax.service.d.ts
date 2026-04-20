import { PrismaService } from '../prisma/prisma.service';
export declare class TaxService {
    private prisma;
    constructor(prisma: PrismaService);
    getMonthlyDashboard(driverId: string, year: number, month: number): Promise<{
        year: number;
        month: number;
        totalTrips: number;
        totalEarnedOere: number;
        totalKm: number;
        taxableAmountOere: number;
        taxableAmountDkk: number;
        records: ({
            trip: {
                distanceKm: number | null;
                originAddress: string;
                destAddress: string;
                departureAt: Date;
            };
        } & {
            id: string;
            driverId: string;
            tripId: string;
            bookingId: string;
            amountOere: number;
            distanceKm: number;
            ratePerKm: number;
            recordedAt: Date;
        })[];
    }>;
    getAnnualSummary(driverId: string, year: number): Promise<{
        year: number;
        totalTrips: number;
        totalEarnedOere: number;
        totalEarnedDkk: number;
        totalKm: number;
        taxableAmountOere: number;
        taxableAmountDkk: number;
        months: {
            year: number;
            month: number;
            totalTrips: number;
            totalEarnedOere: number;
            totalKm: number;
            taxableAmountOere: number;
            taxableAmountDkk: number;
            records: ({
                trip: {
                    distanceKm: number | null;
                    originAddress: string;
                    destAddress: string;
                    departureAt: Date;
                };
            } & {
                id: string;
                driverId: string;
                tripId: string;
                bookingId: string;
                amountOere: number;
                distanceKm: number;
                ratePerKm: number;
                recordedAt: Date;
            })[];
        }[];
    }>;
    getAllRecords(driverId: string): Promise<({
        trip: {
            originAddress: string;
            destAddress: string;
            departureAt: Date;
        };
    } & {
        id: string;
        driverId: string;
        tripId: string;
        bookingId: string;
        amountOere: number;
        distanceKm: number;
        ratePerKm: number;
        recordedAt: Date;
    })[]>;
}
