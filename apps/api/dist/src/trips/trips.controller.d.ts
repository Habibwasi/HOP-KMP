import { TripsService } from './trips.service';
import { CreateTripDto } from './dto/create-trip.dto';
import { SearchTripsDto } from './dto/search-trips.dto';
export declare class TripsController {
    private trips;
    constructor(trips: TripsService);
    create(req: any, dto: CreateTripDto): Promise<({
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
        seats: number;
        isActive: boolean;
        status: import("@prisma/client").$Enums.TripStatus;
        driverId: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: string[];
        isRecurring: boolean;
    }) | null>;
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
        seats: number;
        isActive: boolean;
        status: import("@prisma/client").$Enums.TripStatus;
        driverId: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: string[];
        isRecurring: boolean;
    }[]>;
    myTripsAsDriver(req: any): Promise<({
        bookings: {
            id: string;
            createdAt: Date;
            updatedAt: Date;
            seats: number;
            tripId: string;
            passengerId: string;
            totalOere: number;
            status: import("@prisma/client").$Enums.BookingStatus;
        }[];
    } & {
        id: string;
        createdAt: Date;
        updatedAt: Date;
        model: import("@prisma/client").$Enums.TripModel;
        seats: number;
        isActive: boolean;
        status: import("@prisma/client").$Enums.TripStatus;
        driverId: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: string[];
        isRecurring: boolean;
    })[]>;
    myTripsAsPassenger(req: any): Promise<({
        bookings: {
            id: string;
            createdAt: Date;
            updatedAt: Date;
            seats: number;
            tripId: string;
            passengerId: string;
            totalOere: number;
            status: import("@prisma/client").$Enums.BookingStatus;
        }[];
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
        seats: number;
        isActive: boolean;
        status: import("@prisma/client").$Enums.TripStatus;
        driverId: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: string[];
        isRecurring: boolean;
    })[]>;
    myTrips(req: any): Promise<({
        bookings: {
            id: string;
            createdAt: Date;
            updatedAt: Date;
            seats: number;
            tripId: string;
            passengerId: string;
            totalOere: number;
            status: import("@prisma/client").$Enums.BookingStatus;
        }[];
    } & {
        id: string;
        createdAt: Date;
        updatedAt: Date;
        model: import("@prisma/client").$Enums.TripModel;
        seats: number;
        isActive: boolean;
        status: import("@prisma/client").$Enums.TripStatus;
        driverId: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: string[];
        isRecurring: boolean;
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
            tripId: string;
            passengerId: string;
            totalOere: number;
            status: import("@prisma/client").$Enums.BookingStatus;
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
        seats: number;
        isActive: boolean;
        status: import("@prisma/client").$Enums.TripStatus;
        driverId: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: string[];
        isRecurring: boolean;
    }>;
    cancel(id: string, req: any): Promise<{
        cancelled: boolean;
    }>;
    complete(id: string, req: any): Promise<{
        completed: boolean;
    }>;
}
