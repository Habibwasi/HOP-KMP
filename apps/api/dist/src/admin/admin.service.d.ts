import { PrismaService } from '../prisma/prisma.service';
export declare class AdminService {
    private prisma;
    constructor(prisma: PrismaService);
    listUsers(page?: number, limit?: number): Promise<{
        users: {
            id: string;
            phone: string | null;
            email: string | null;
            firstName: string;
            lastName: string;
            role: import("@prisma/client").$Enums.Role;
            isVerified: boolean;
            isBanned: boolean;
            banExpiresAt: Date | null;
            createdAt: Date;
            isAdmin: boolean;
        }[];
        total: number;
        page: number;
        pages: number;
    }>;
    banUser(userId: string, durationDays: number | 'permanent'): Promise<{
        id: string;
        phone: string | null;
        email: string | null;
        firstName: string;
        lastName: string;
        avatarUrl: string | null;
        role: import("@prisma/client").$Enums.Role;
        isVerified: boolean;
        isBanned: boolean;
        banExpiresAt: Date | null;
        createdAt: Date;
        updatedAt: Date;
        mobilepayNumber: string | null;
        isAdmin: boolean;
    }>;
    unbanUser(userId: string): Promise<{
        id: string;
        phone: string | null;
        email: string | null;
        firstName: string;
        lastName: string;
        avatarUrl: string | null;
        role: import("@prisma/client").$Enums.Role;
        isVerified: boolean;
        isBanned: boolean;
        banExpiresAt: Date | null;
        createdAt: Date;
        updatedAt: Date;
        mobilepayNumber: string | null;
        isAdmin: boolean;
    }>;
    makeAdmin(userId: string): Promise<{
        id: string;
        phone: string | null;
        email: string | null;
        firstName: string;
        lastName: string;
        avatarUrl: string | null;
        role: import("@prisma/client").$Enums.Role;
        isVerified: boolean;
        isBanned: boolean;
        banExpiresAt: Date | null;
        createdAt: Date;
        updatedAt: Date;
        mobilepayNumber: string | null;
        isAdmin: boolean;
    }>;
    getPendingLicences(): Promise<({
        user: {
            id: string;
            phone: string | null;
            firstName: string;
            lastName: string;
        };
    } & {
        id: string;
        createdAt: Date;
        userId: string;
        status: import("@prisma/client").$Enums.BookingStatus;
        frontUrl: string;
        backUrl: string;
        reviewedAt: Date | null;
        reviewedBy: string | null;
    })[]>;
    reviewLicence(licenceId: string, adminId: string, approved: boolean): Promise<{
        reviewed: boolean;
        approved: boolean;
    }>;
    listTrips(page?: number, limit?: number): Promise<{
        trips: ({
            _count: {
                bookings: number;
            };
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
        })[];
        total: number;
        page: number;
        pages: number;
    }>;
    getDashboardStats(): Promise<{
        users: {
            total: number;
            verified: number;
            banned: number;
        };
        trips: {
            total: number;
            active: number;
        };
        bookings: {
            total: number;
            confirmed: number;
        };
        pendingLicenceReviews: number;
    }>;
}
