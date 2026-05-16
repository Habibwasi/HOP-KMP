import { TripModel } from '@prisma/client';
export declare class CreateTripDto {
    model: TripModel;
    originLat: number;
    originLng: number;
    originAddress: string;
    destLat: number;
    destLng: number;
    destAddress: string;
    distanceMetres?: number;
    departureAt: string;
    seats: number;
    recurringDays?: string[];
    minPassengers?: number;
    thresholdDeadline?: string;
}
