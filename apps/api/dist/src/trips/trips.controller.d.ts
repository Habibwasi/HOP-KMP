import { TripsService } from './trips.service';
import { CreateTripDto } from './dto/create-trip.dto';
import { SearchTripsDto } from './dto/search-trips.dto';
export declare class TripsController {
    private trips;
    constructor(trips: TripsService);
    create(req: any, dto: CreateTripDto): Promise<{
        driver: {
            id: string;
            firstName: string;
            lastName: string;
            avatarUrl: string | null;
        };
    } & {
        id: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        model: import("@prisma/client").$Enums.TripModel;
        status: import("@prisma/client").$Enums.TripStatus;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
        driverId: string;
    }>;
    search(dto: SearchTripsDto): Promise<{
        availableSeats: number;
        bookings: undefined;
        driver: {
            id: string;
            firstName: string;
            lastName: string;
            avatarUrl: string | null;
        };
        id: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        model: import("@prisma/client").$Enums.TripModel;
        status: import("@prisma/client").$Enums.TripStatus;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
        driverId: string;
    }[]>;
    myTrips(req: any): Promise<({
        bookings: {
            id: string;
            seats: number;
            status: string;
            createdAt: Date;
            updatedAt: Date;
            tripId: string;
            passengerId: string;
            totalOere: number;
        }[];
    } & {
        id: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        model: import("@prisma/client").$Enums.TripModel;
        status: import("@prisma/client").$Enums.TripStatus;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
        driverId: string;
    })[]>;
    findOne(id: string): Promise<{
        driver: {
            id: string;
            firstName: string;
            lastName: string;
            avatarUrl: string | null;
        };
        bookings: ({
            passenger: {
                id: string;
                firstName: string;
                lastName: string;
                avatarUrl: string | null;
            };
        } & {
            id: string;
            seats: number;
            status: string;
            createdAt: Date;
            updatedAt: Date;
            tripId: string;
            passengerId: string;
            totalOere: number;
        })[];
    } & {
        id: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        model: import("@prisma/client").$Enums.TripModel;
        status: import("@prisma/client").$Enums.TripStatus;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
        driverId: string;
    }>;
    cancel(id: string, req: any): Promise<{
        id: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        model: import("@prisma/client").$Enums.TripModel;
        status: import("@prisma/client").$Enums.TripStatus;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
        driverId: string;
    }>;
}
