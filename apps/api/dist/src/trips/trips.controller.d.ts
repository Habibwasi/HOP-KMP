import { TripsService } from './trips.service';
import { CreateTripDto } from './dto/create-trip.dto';
import { SearchTripsDto } from './dto/search-trips.dto';
import { UpdateTripDto } from './dto/update-trip.dto';
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
    }) | null>;
    search(dto: SearchTripsDto): Promise<{
        availableSeats: number;
        bookedSeats: undefined;
        id: string;
        driverId: string;
        model: string;
        originLat: number;
        originLng: number;
        originAddress: string;
        destLat: number;
        destLng: number;
        destAddress: string;
        departureAt: Date;
        seats: number;
        pricePerSeat: number;
        distanceKm: number | null;
        status: string;
        isActive: boolean;
        isRecurring: boolean;
        recurringDays: string[];
        minPassengers: number | null;
        thresholdDeadline: Date | null;
        createdAt: Date;
        updatedAt: Date;
        driver: {
            id: string;
            firstName: string;
            lastName: string;
            avatarUrl: string | null;
        };
    }[] | {
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
    }[]>;
    myTripsAsDriver(req: any): Promise<({
        bookings: {
            id: string;
            createdAt: Date;
            status: import("@prisma/client").$Enums.BookingStatus;
            seats: number;
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
    })[]>;
    myTripsAsPassenger(req: any): Promise<{
        bookingId: string;
        bookingStatus: import("@prisma/client").$Enums.BookingStatus;
        bookings: {
            id: string;
            status: import("@prisma/client").$Enums.BookingStatus;
            seats: number;
        }[];
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
    }[]>;
    myTrips(req: any): Promise<({
        bookings: {
            id: string;
            createdAt: Date;
            status: import("@prisma/client").$Enums.BookingStatus;
            seats: number;
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
    })[]>;
    getTripPassengers(id: string, req: any): Promise<{
        bookingId: string;
        passengerId: string;
        fullName: string;
        rating: number;
        seats: number;
        avatarUrl: string | null;
    }[]>;
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
    }>;
    cancel(id: string, req: any): Promise<{
        cancelled: boolean;
    }>;
    update(id: string, req: any, dto: UpdateTripDto): Promise<{
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
    }>;
    stopRecurring(id: string, req: any): Promise<{
        stopped: boolean;
        cancelledCount: number;
    }>;
    complete(id: string, req: any): Promise<{
        completed: boolean;
    }>;
}
