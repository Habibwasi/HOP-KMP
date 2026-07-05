import { BookingsService } from './bookings.service';
import { RatingsService } from '../ratings/ratings.service';
import { CreateBookingDto } from './dto/create-booking.dto';
declare class RateBookingDto {
    stars: number;
    comment?: string;
}
export declare class BookingsController {
    private bookings;
    private ratings;
    constructor(bookings: BookingsService, ratings: RatingsService);
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
    myChats(req: any): Promise<{
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
    findOne(id: string, req: any): Promise<{
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
    cancel(id: string, req: any): Promise<{
        cancelled: boolean;
    }>;
    confirm(id: string, req: any): Promise<{
        id: string;
        createdAt: Date;
        updatedAt: Date;
        status: import("@prisma/client").$Enums.BookingStatus;
        passengerId: string;
        seats: number;
        tripId: string;
        totalOere: number;
    }>;
    rate(bookingId: string, req: any, body: RateBookingDto): Promise<{
        id: string;
        createdAt: Date;
        rateeId: string;
        score: number;
        comment: string | null;
        raterId: string;
    }>;
}
export {};
