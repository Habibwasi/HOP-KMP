import { TaxService } from './tax.service';
export declare class TaxController {
    private tax;
    constructor(tax: TaxService);
    allRecords(req: any): Promise<({
        trip: {
            originAddress: string;
            destAddress: string;
            departureAt: Date;
        };
    } & {
        id: string;
        tripId: string;
        driverId: string;
        distanceKm: number;
        bookingId: string;
        amountOere: number;
        ratePerKm: number;
        recordedAt: Date;
    })[]>;
    monthly(req: any, year: number, month: number): Promise<{
        year: number;
        month: number;
        totalTrips: number;
        totalEarnedOere: number;
        totalKm: number;
        taxableAmountOere: number;
        taxableAmountDkk: number;
        records: ({
            trip: {
                originAddress: string;
                destAddress: string;
                departureAt: Date;
                distanceKm: number | null;
            };
        } & {
            id: string;
            tripId: string;
            driverId: string;
            distanceKm: number;
            bookingId: string;
            amountOere: number;
            ratePerKm: number;
            recordedAt: Date;
        })[];
    }>;
    annual(req: any, year: number): Promise<{
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
                    originAddress: string;
                    destAddress: string;
                    departureAt: Date;
                    distanceKm: number | null;
                };
            } & {
                id: string;
                tripId: string;
                driverId: string;
                distanceKm: number;
                bookingId: string;
                amountOere: number;
                ratePerKm: number;
                recordedAt: Date;
            })[];
        }[];
    }>;
}
