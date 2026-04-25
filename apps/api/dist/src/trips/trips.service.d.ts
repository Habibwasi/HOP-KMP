import { PrismaService } from '../prisma/prisma.service';
import { PricingService } from './pricing.service';
import { CreateTripDto } from './dto/create-trip.dto';
import { SearchTripsDto } from './dto/search-trips.dto';
export declare class TripsService {
    private prisma;
    private pricing;
    constructor(prisma: PrismaService, pricing: PricingService);
    create(driverId: string, dto: CreateTripDto): Promise<{
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
    findById(id: string): Promise<{
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
    cancel(tripId: string, userId: string): Promise<{
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
    findByDriver(driverId: string): Promise<({
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
    findByPassenger(passengerId: string): Promise<({
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
}
