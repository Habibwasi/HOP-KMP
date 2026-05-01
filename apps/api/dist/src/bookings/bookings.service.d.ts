import { Queue } from 'bullmq';
import { PrismaService } from '../prisma/prisma.service';
import { CreateBookingDto } from './dto/create-booking.dto';
import { NotificationsService } from '../notifications/notifications.service';
import { PaymentsService } from '../payments/payments.service';
export declare class BookingsService {
    private prisma;
    private bookingsQueue;
    private notifications;
    private payments;
    private readonly logger;
    constructor(prisma: PrismaService, bookingsQueue: Queue, notifications: NotificationsService, payments: PaymentsService);
    create(passengerId: string, dto: CreateBookingDto): Promise<{
        trip: {
            driver: {
                id: string;
                firstName: string;
                lastName: string;
            };
        } & {
            id: string;
            seats: number;
            status: import("@prisma/client").$Enums.TripStatus;
            createdAt: Date;
            updatedAt: Date;
            driverId: string;
            originLat: number;
            originLng: number;
            originAddress: string;
            destLat: number;
            destLng: number;
            destAddress: string;
            departureAt: Date;
            pricePerSeat: number;
            model: import("@prisma/client").$Enums.TripModel;
            minPassengers: number | null;
            thresholdDeadline: Date | null;
            distanceKm: number | null;
            recurringDays: string[];
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
        tripId: string;
        passengerId: string;
        seats: number;
        totalOere: number;
        status: import("@prisma/client").$Enums.BookingStatus;
        createdAt: Date;
        updatedAt: Date;
    }>;
    confirm(bookingId: string): Promise<{
        id: string;
        tripId: string;
        passengerId: string;
        seats: number;
        totalOere: number;
        status: import("@prisma/client").$Enums.BookingStatus;
        createdAt: Date;
        updatedAt: Date;
    }>;
    cancel(bookingId: string, userId: string): Promise<{
        id: string;
        tripId: string;
        passengerId: string;
        seats: number;
        totalOere: number;
        status: import("@prisma/client").$Enums.BookingStatus;
        createdAt: Date;
        updatedAt: Date;
    }>;
    findById(id: string): Promise<{
        trip: {
            id: string;
            seats: number;
            status: import("@prisma/client").$Enums.TripStatus;
            createdAt: Date;
            updatedAt: Date;
            driverId: string;
            originLat: number;
            originLng: number;
            originAddress: string;
            destLat: number;
            destLng: number;
            destAddress: string;
            departureAt: Date;
            pricePerSeat: number;
            model: import("@prisma/client").$Enums.TripModel;
            minPassengers: number | null;
            thresholdDeadline: Date | null;
            distanceKm: number | null;
            recurringDays: string[];
            isRecurring: boolean;
            isActive: boolean;
        };
        passenger: {
            id: string;
            firstName: string;
            lastName: string;
            avatarUrl: string | null;
        };
        payment: {
            id: string;
            status: import("@prisma/client").$Enums.PaymentStatus;
            createdAt: Date;
            updatedAt: Date;
            bookingId: string;
            amountOere: number;
            provider: import("@prisma/client").$Enums.PaymentProvider;
            providerRef: string | null;
            webhookData: import("@prisma/client/runtime/client").JsonValue | null;
        } | null;
    } & {
        id: string;
        tripId: string;
        passengerId: string;
        seats: number;
        totalOere: number;
        status: import("@prisma/client").$Enums.BookingStatus;
        createdAt: Date;
        updatedAt: Date;
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
            seats: number;
            status: import("@prisma/client").$Enums.TripStatus;
            createdAt: Date;
            updatedAt: Date;
            driverId: string;
            originLat: number;
            originLng: number;
            originAddress: string;
            destLat: number;
            destLng: number;
            destAddress: string;
            departureAt: Date;
            pricePerSeat: number;
            model: import("@prisma/client").$Enums.TripModel;
            minPassengers: number | null;
            thresholdDeadline: Date | null;
            distanceKm: number | null;
            recurringDays: string[];
            isRecurring: boolean;
            isActive: boolean;
        };
        payment: {
            id: string;
            status: import("@prisma/client").$Enums.PaymentStatus;
            createdAt: Date;
            updatedAt: Date;
            bookingId: string;
            amountOere: number;
            provider: import("@prisma/client").$Enums.PaymentProvider;
            providerRef: string | null;
            webhookData: import("@prisma/client/runtime/client").JsonValue | null;
        } | null;
    } & {
        id: string;
        tripId: string;
        passengerId: string;
        seats: number;
        totalOere: number;
        status: import("@prisma/client").$Enums.BookingStatus;
        createdAt: Date;
        updatedAt: Date;
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
            seats: number;
            status: import("@prisma/client").$Enums.TripStatus;
            createdAt: Date;
            updatedAt: Date;
            driverId: string;
            originLat: number;
            originLng: number;
            originAddress: string;
            destLat: number;
            destLng: number;
            destAddress: string;
            departureAt: Date;
            pricePerSeat: number;
            model: import("@prisma/client").$Enums.TripModel;
            minPassengers: number | null;
            thresholdDeadline: Date | null;
            distanceKm: number | null;
            recurringDays: string[];
            isRecurring: boolean;
            isActive: boolean;
        };
        payment: {
            id: string;
            status: import("@prisma/client").$Enums.PaymentStatus;
            createdAt: Date;
            updatedAt: Date;
            bookingId: string;
            amountOere: number;
            provider: import("@prisma/client").$Enums.PaymentProvider;
            providerRef: string | null;
            webhookData: import("@prisma/client/runtime/client").JsonValue | null;
        } | null;
    } & {
        id: string;
        tripId: string;
        passengerId: string;
        seats: number;
        totalOere: number;
        status: import("@prisma/client").$Enums.BookingStatus;
        createdAt: Date;
        updatedAt: Date;
    })[]>;
    checkModelBThreshold(tripId: string): Promise<void>;
}
