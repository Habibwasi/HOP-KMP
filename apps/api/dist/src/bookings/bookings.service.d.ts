import { Queue } from 'bullmq';
import { PrismaService } from '../prisma/prisma.service';
import { CreateBookingDto } from './dto/create-booking.dto';
export declare class BookingsService {
    private prisma;
    private bookingsQueue;
    constructor(prisma: PrismaService, bookingsQueue: Queue);
    create(passengerId: string, dto: CreateBookingDto): Promise<{
        trip: {
            driver: {
                id: string;
                firstName: string;
                lastName: string;
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
        };
        passenger: {
            id: string;
            firstName: string;
            lastName: string;
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
    }>;
    confirm(bookingId: string): Promise<{
        id: string;
        createdAt: Date;
        updatedAt: Date;
        status: import("@prisma/client").$Enums.BookingStatus;
        passengerId: string;
        seats: number;
        tripId: string;
        totalOere: number;
    }>;
    cancel(bookingId: string, userId: string): Promise<{
        id: string;
        createdAt: Date;
        updatedAt: Date;
        status: import("@prisma/client").$Enums.BookingStatus;
        passengerId: string;
        seats: number;
        tripId: string;
        totalOere: number;
    }>;
    findById(id: string): Promise<{
        trip: {
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
        };
        payment: {
            id: string;
            createdAt: Date;
            updatedAt: Date;
            status: import("@prisma/client").$Enums.PaymentStatus;
            bookingId: string;
            amountOere: number;
            provider: import("@prisma/client").$Enums.PaymentProvider;
            providerRef: string | null;
            webhookData: import("@prisma/client/runtime/client").JsonValue | null;
        } | null;
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
    }>;
    findActiveForPassenger(passengerId: string): Promise<({
        trip: {
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
        };
        payment: {
            id: string;
            createdAt: Date;
            updatedAt: Date;
            status: import("@prisma/client").$Enums.PaymentStatus;
            bookingId: string;
            amountOere: number;
            provider: import("@prisma/client").$Enums.PaymentProvider;
            providerRef: string | null;
            webhookData: import("@prisma/client/runtime/client").JsonValue | null;
        } | null;
    } & {
        id: string;
        createdAt: Date;
        updatedAt: Date;
        status: import("@prisma/client").$Enums.BookingStatus;
        passengerId: string;
        seats: number;
        tripId: string;
        totalOere: number;
    }) | null>;
    findByPassenger(passengerId: string): Promise<({
        trip: {
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
        };
        payment: {
            id: string;
            createdAt: Date;
            updatedAt: Date;
            status: import("@prisma/client").$Enums.PaymentStatus;
            bookingId: string;
            amountOere: number;
            provider: import("@prisma/client").$Enums.PaymentProvider;
            providerRef: string | null;
            webhookData: import("@prisma/client/runtime/client").JsonValue | null;
        } | null;
    } & {
        id: string;
        createdAt: Date;
        updatedAt: Date;
        status: import("@prisma/client").$Enums.BookingStatus;
        passengerId: string;
        seats: number;
        tripId: string;
        totalOere: number;
    })[]>;
    checkModelBThreshold(tripId: string): Promise<void>;
}
