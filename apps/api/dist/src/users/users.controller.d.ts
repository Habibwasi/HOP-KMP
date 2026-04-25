import { SupabaseClient } from '@supabase/supabase-js';
import { UsersService } from './users.service';
import { RatingsService } from '../ratings/ratings.service';
import { NotificationsService } from '../notifications/notifications.service';
import { CreateProfileDto } from './dto/create-profile.dto';
import { UpdateUserDto } from './dto/update-user.dto';
declare class ReportDto {
    reason: string;
}
declare class PushTokenDto {
    token: string;
    platform: string;
}
export declare class UsersController {
    private users;
    private ratings;
    private notifications;
    private readonly supabase;
    private readonly logger;
    constructor(users: UsersService, ratings: RatingsService, notifications: NotificationsService, supabase: SupabaseClient);
    createProfile(req: any, dto: CreateProfileDto): Promise<{
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
        isAdmin: boolean;
    }>;
    getMe(req: any): Promise<{
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
        isAdmin: boolean;
    }>;
    getMyStats(req: any): Promise<{
        averageRating: number | null;
        totalRatings: number;
        completedTrips: number;
    }>;
    updateMe(req: any, dto: UpdateUserDto): Promise<{
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
        isAdmin: boolean;
    }>;
    savePushToken(req: any, dto: PushTokenDto): Promise<void>;
    getUserById(id: string): Promise<{
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
        isAdmin: boolean;
    }>;
    getUserReviews(id: string): Promise<{
        id: any;
        raterName: string;
        stars: any;
        comment: any;
        roleRated: string;
    }[]>;
    getCarDetails(id: string): Promise<{
        id: string;
        createdAt: Date;
        updatedAt: Date;
        userId: string;
        make: string;
        model: string;
        year: number;
        licensePlate: string;
        colour: string;
        seatsAvailable: number;
    }>;
    reportUser(req: any, id: string, dto: ReportDto): Promise<void>;
}
export {};
