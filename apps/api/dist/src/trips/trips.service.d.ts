import { Queue } from 'bullmq';
import { PrismaService } from '../prisma/prisma.service';
import { PricingService } from './pricing.service';
import { CreateTripDto } from './dto/create-trip.dto';
import { SearchTripsDto } from './dto/search-trips.dto';
import { BookingsService } from '../bookings/bookings.service';
import { PaymentsService } from '../payments/payments.service';
export declare class TripsService {
    private prisma;
    private pricing;
    private alertsQueue;
    private bookingsQueue;
    private bookings;
    private payments;
    private readonly logger;
    constructor(prisma: PrismaService, pricing: PricingService, alertsQueue: Queue, bookingsQueue: Queue, bookings: BookingsService, payments: PaymentsService);
    create(driverId: string, dto: CreateTripDto): Promise<({
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
    extendRecurringWindow(): Promise<void>;
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
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
    cancel(tripId: string, userId: string): Promise<{
        cancelled: boolean;
    }>;
    complete(tripId: string, userId: string): Promise<{
        completed: boolean;
    }>;
    findByDriver(driverId: string): Promise<({
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
    })[]>;
    findByPassenger(passengerId: string): Promise<({
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
        recurringDays: string[];
        isRecurring: boolean;
        isActive: boolean;
    })[]>;
}
