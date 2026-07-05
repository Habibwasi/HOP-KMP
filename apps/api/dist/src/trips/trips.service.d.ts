import { Queue } from 'bullmq';
import { PrismaService } from '../prisma/prisma.service';
import { PricingService } from './pricing.service';
import { CreateTripDto } from './dto/create-trip.dto';
import { SearchTripsDto } from './dto/search-trips.dto';
import { UpdateTripDto } from './dto/update-trip.dto';
import { BookingsService } from '../bookings/bookings.service';
import { NotificationsService } from '../notifications/notifications.service';
export declare class TripsService {
    private prisma;
    private pricing;
    private alertsQueue;
    private bookingsQueue;
    private bookings;
    private notifications;
    private readonly logger;
    constructor(prisma: PrismaService, pricing: PricingService, alertsQueue: Queue, bookingsQueue: Queue, bookings: BookingsService, notifications: NotificationsService);
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
    getTripPassengers(tripId: string, driverId: string): Promise<{
        bookingId: string;
        passengerId: string;
        fullName: string;
        rating: number;
        seats: number;
        avatarUrl: string | null;
    }[]>;
    stopRecurring(tripId: string, userId: string): Promise<{
        stopped: boolean;
        cancelledCount: number;
    }>;
    cancel(tripId: string, userId: string): Promise<{
        cancelled: boolean;
    }>;
    update(tripId: string, userId: string, dto: UpdateTripDto): Promise<{
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
    complete(tripId: string, userId: string): Promise<{
        completed: boolean;
    }>;
    findByDriver(driverId: string): Promise<({
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
    findByPassenger(passengerId: string): Promise<{
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
}
