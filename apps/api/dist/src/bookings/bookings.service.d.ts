import { Queue } from 'bullmq';
import { PrismaService } from '../prisma/prisma.service';
import { CreateBookingDto } from './dto/create-booking.dto';
import { NotificationsService } from '../notifications/notifications.service';
export declare class BookingsService {
    private prisma;
    private bookingsQueue;
    private notifications;
    private readonly logger;
    constructor(prisma: PrismaService, bookingsQueue: Queue, notifications: NotificationsService);
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
        createdAt: Date;
        updatedAt: Date;
        status: import("@prisma/client").$Enums.BookingStatus;
        passengerId: string;
        seats: number;
        tripId: string;
        totalOere: number;
    }>;
    confirm(bookingId: string, driverId: string): Promise<{
        id: string;
        createdAt: Date;
        updatedAt: Date;
        status: import("@prisma/client").$Enums.BookingStatus;
        passengerId: string;
        seats: number;
        tripId: string;
        totalOere: number;
    }>;
    findByIdAuthorized(id: string, requesterId: string): Promise<{
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
    cancel(bookingId: string, userId: string): Promise<{
        cancelled: boolean;
    }>;
    cancelAllForTrip(tripId: string): Promise<void>;
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
            recurringDays: string[];
            isRecurring: boolean;
            isActive: boolean;
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
            recurringDays: string[];
            isRecurring: boolean;
            isActive: boolean;
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
    })[]>;
    findMyChats(userId: string): Promise<{
        bookingId: string;
        bookingStatus: import("@prisma/client").$Enums.BookingStatus;
        tripOrigin: string;
        tripDest: string;
        departureAt: string;
        otherPartyId: string;
        otherPartyName: string;
        otherPartyAvatarUrl: string | null;
        myRole: string;
    }[]>;
    checkModelBThreshold(tripId: string): Promise<void>;
}
