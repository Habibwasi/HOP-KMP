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
        createdAt: Date;
        updatedAt: Date;
        model: import("@prisma/client").$Enums.TripModel;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        recurringDays: number[];
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        pricePerSeat: number;
        status: import("@prisma/client").$Enums.TripStatus;
        distanceKm: number | null;
        isRecurring: boolean;
        isActive: boolean;
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
        createdAt: Date;
        updatedAt: Date;
        model: import("@prisma/client").$Enums.TripModel;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        recurringDays: number[];
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        pricePerSeat: number;
        status: import("@prisma/client").$Enums.TripStatus;
        distanceKm: number | null;
        isRecurring: boolean;
        isActive: boolean;
        driverId: string;
    }[]>;
    myTrips(req: any): Promise<({
        bookings: {
            id: string;
            createdAt: Date;
            updatedAt: Date;
            seats: number;
            status: import("@prisma/client").$Enums.BookingStatus;
            tripId: string;
            passengerId: string;
            totalOere: number;
        }[];
    } & {
        id: string;
        createdAt: Date;
        updatedAt: Date;
        model: import("@prisma/client").$Enums.TripModel;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        recurringDays: number[];
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        pricePerSeat: number;
        status: import("@prisma/client").$Enums.TripStatus;
        distanceKm: number | null;
        isRecurring: boolean;
        isActive: boolean;
        driverId: string;
    })[]>;
    findOne(id: string): Promise<{
        bookings: ({
            passenger: {
                id: string;
                firstName: string;
                lastName: string;
                avatarUrl: string | null;
            };
        } & {
            id: string;
            createdAt: Date;
            updatedAt: Date;
            seats: number;
            status: import("@prisma/client").$Enums.BookingStatus;
            tripId: string;
            passengerId: string;
            totalOere: number;
        })[];
        driver: {
            id: string;
            firstName: string;
            lastName: string;
            avatarUrl: string | null;
        };
    } & {
        id: string;
        createdAt: Date;
        updatedAt: Date;
        model: import("@prisma/client").$Enums.TripModel;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        recurringDays: number[];
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        pricePerSeat: number;
        status: import("@prisma/client").$Enums.TripStatus;
        distanceKm: number | null;
        isRecurring: boolean;
        isActive: boolean;
        driverId: string;
    }>;
    cancel(id: string, req: any): Promise<{
        id: string;
        createdAt: Date;
        updatedAt: Date;
        model: import("@prisma/client").$Enums.TripModel;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        recurringDays: number[];
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        pricePerSeat: number;
        status: import("@prisma/client").$Enums.TripStatus;
        distanceKm: number | null;
        isRecurring: boolean;
        isActive: boolean;
        driverId: string;
    }>;
}
