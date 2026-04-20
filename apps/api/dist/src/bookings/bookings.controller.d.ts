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
        seats: number;
        totalOere: number;
        status: import("@prisma/client").$Enums.BookingStatus;
        createdAt: Date;
        updatedAt: Date;
        tripId: string;
        passengerId: string;
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
            recurringDays: number[];
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
        seats: number;
        totalOere: number;
        status: import("@prisma/client").$Enums.BookingStatus;
        createdAt: Date;
        updatedAt: Date;
        tripId: string;
        passengerId: string;
    })[]>;
    findOne(id: string): Promise<{
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
            recurringDays: number[];
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
        passenger: {
            id: string;
            firstName: string;
            lastName: string;
            avatarUrl: string | null;
        };
    } & {
        id: string;
        seats: number;
        totalOere: number;
        status: import("@prisma/client").$Enums.BookingStatus;
        createdAt: Date;
        updatedAt: Date;
        tripId: string;
        passengerId: string;
    }>;
    cancel(id: string, req: any): Promise<{
        id: string;
        seats: number;
        totalOere: number;
        status: import("@prisma/client").$Enums.BookingStatus;
        createdAt: Date;
        updatedAt: Date;
        tripId: string;
        passengerId: string;
    }>;
}
