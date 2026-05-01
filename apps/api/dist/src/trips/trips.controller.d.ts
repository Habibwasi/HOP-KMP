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
        driverId: string;
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
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
        driverId: string;
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
    }[]>;
    myTripsAsDriver(req: any): Promise<({
        bookings: {
            id: string;
            seats: number;
            status: import("@prisma/client").$Enums.BookingStatus;
            createdAt: Date;
            updatedAt: Date;
            tripId: string;
            passengerId: string;
            totalOere: number;
        }[];
    } & {
        id: string;
        driverId: string;
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
    })[]>;
    myTripsAsPassenger(req: any): Promise<({
        driver: {
            id: string;
            firstName: string;
            lastName: string;
            avatarUrl: string | null;
        };
        bookings: {
            id: string;
            seats: number;
            status: import("@prisma/client").$Enums.BookingStatus;
            createdAt: Date;
            updatedAt: Date;
            tripId: string;
            passengerId: string;
            totalOere: number;
        }[];
    } & {
        id: string;
        driverId: string;
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
    })[]>;
    myTrips(req: any): Promise<({
        bookings: {
            id: string;
            seats: number;
            status: import("@prisma/client").$Enums.BookingStatus;
            createdAt: Date;
            updatedAt: Date;
            tripId: string;
            passengerId: string;
            totalOere: number;
        }[];
    } & {
        id: string;
        driverId: string;
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
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
            status: import("@prisma/client").$Enums.BookingStatus;
            createdAt: Date;
            updatedAt: Date;
            tripId: string;
            passengerId: string;
            totalOere: number;
        })[];
    } & {
        id: string;
        driverId: string;
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
    }>;
    cancel(id: string, req: any): Promise<{
        id: string;
        driverId: string;
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
        createdAt: Date;
        updatedAt: Date;
    }>;
}
