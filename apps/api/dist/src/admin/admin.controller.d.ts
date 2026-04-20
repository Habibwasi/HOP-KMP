import { AdminService } from './admin.service';
declare class BanUserDto {
    durationDays?: number;
    permanent?: 'permanent';
}
export declare class AdminController {
    private admin;
    constructor(admin: AdminService);
    stats(): Promise<{
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
    users(page?: number, limit?: number): Promise<{
        users: {
            id: string;
            phone: string;
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
    ban(id: string, dto: BanUserDto): Promise<{
        id: string;
        phone: string;
        email: string | null;
        passwordHash: string | null;
        firstName: string;
        lastName: string;
        avatarUrl: string | null;
        role: import("@prisma/client").$Enums.Role;
        isVerified: boolean;
        isBanned: boolean;
        banExpiresAt: Date | null;
        createdAt: Date;
        updatedAt: Date;
        isAdmin: boolean;
    }>;
    unban(id: string): Promise<{
        id: string;
        phone: string;
        email: string | null;
        passwordHash: string | null;
        firstName: string;
        lastName: string;
        avatarUrl: string | null;
        role: import("@prisma/client").$Enums.Role;
        isVerified: boolean;
        isBanned: boolean;
        banExpiresAt: Date | null;
        createdAt: Date;
        updatedAt: Date;
        isAdmin: boolean;
    }>;
    makeAdmin(id: string): Promise<{
        id: string;
        phone: string;
        email: string | null;
        passwordHash: string | null;
        firstName: string;
        lastName: string;
        avatarUrl: string | null;
        role: import("@prisma/client").$Enums.Role;
        isVerified: boolean;
        isBanned: boolean;
        banExpiresAt: Date | null;
        createdAt: Date;
        updatedAt: Date;
        isAdmin: boolean;
    }>;
    pendingLicences(): Promise<({
        user: {
            id: string;
            phone: string;
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
    reviewLicence(req: any, id: string, approved: boolean): Promise<{
        reviewed: boolean;
        approved: boolean;
    }>;
    trips(page?: number, limit?: number): Promise<{
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
            originLat: number;
            originLng: number;
            originAddress: string;
            destLat: number;
            destLng: number;
            destAddress: string;
            departureAt: Date;
            seats: number;
            recurringDays: number[];
            minPassengers: number | null;
            thresholdDeadline: Date | null;
            pricePerSeat: number;
            status: import("@prisma/client").$Enums.TripStatus;
            distanceKm: number | null;
            isRecurring: boolean;
            isActive: boolean;
            driverId: string;
        })[];
        total: number;
        page: number;
        pages: number;
    }>;
}
export {};
