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
        driverId: string;
        status: import("@prisma/client").$Enums.TripStatus;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
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
        driverId: string;
        status: import("@prisma/client").$Enums.TripStatus;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
    }[]>;
    myTripsAsDriver(req: any): Promise<({
        bookings: {
            id: string;
            createdAt: Date;
            updatedAt: Date;
            status: import("@prisma/client").$Enums.BookingStatus;
            passengerId: string;
            seats: number;
            tripId: string;
            totalOere: number;
        }[];
    } & {
        id: string;
        createdAt: Date;
        updatedAt: Date;
        model: import("@prisma/client").$Enums.TripModel;
        driverId: string;
        status: import("@prisma/client").$Enums.TripStatus;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
    })[]>;
    myTripsAsPassenger(req: any): Promise<({
        bookings: {
            id: string;
            createdAt: Date;
            updatedAt: Date;
            status: import("@prisma/client").$Enums.BookingStatus;
            passengerId: string;
            seats: number;
            tripId: string;
            totalOere: number;
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
        driverId: string;
        status: import("@prisma/client").$Enums.TripStatus;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
    })[]>;
    myTrips(req: any): Promise<({
        bookings: {
            id: string;
            createdAt: Date;
            updatedAt: Date;
            status: import("@prisma/client").$Enums.BookingStatus;
            passengerId: string;
            seats: number;
            tripId: string;
            totalOere: number;
        }[];
    } & {
        id: string;
        createdAt: Date;
        updatedAt: Date;
        model: import("@prisma/client").$Enums.TripModel;
        driverId: string;
        status: import("@prisma/client").$Enums.TripStatus;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
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
            status: import("@prisma/client").$Enums.BookingStatus;
            passengerId: string;
            seats: number;
            tripId: string;
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
        driverId: string;
        status: import("@prisma/client").$Enums.TripStatus;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
    }>;
    cancel(id: string, req: any): Promise<{
        id: string;
        createdAt: Date;
        updatedAt: Date;
        model: import("@prisma/client").$Enums.TripModel;
        driverId: string;
        status: import("@prisma/client").$Enums.TripStatus;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        distanceKm: number | null;
        recurringDays: number[];
        isRecurring: boolean;
        isActive: boolean;
    }>;
}
