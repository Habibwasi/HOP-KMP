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
    findById(id: string): Promise<{
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
    cancel(tripId: string, userId: string): Promise<{
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
    findByDriver(driverId: string): Promise<({
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
}
