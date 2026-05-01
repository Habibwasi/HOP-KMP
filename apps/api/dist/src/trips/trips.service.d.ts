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
    findByPassenger(passengerId: string): Promise<({
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
}
