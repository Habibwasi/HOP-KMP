import { BookingsService } from './bookings.service';
import { CreateBookingDto } from './dto/create-booking.dto';
export declare class BookingsController {
    private bookings;
    constructor(bookings: BookingsService);
    create(req: any, dto: CreateBookingDto): Promise<{
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
    myBookings(req: any): Promise<({
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
    activeForMe(req: any): Promise<({
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
    findOne(id: string): Promise<{
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
    cancel(id: string, req: any): Promise<{
        cancelled: boolean;
    }>;
}
