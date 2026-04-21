import { UsersService } from './users.service';
import { RatingsService } from '../ratings/ratings.service';
export declare class UsersController {
    private users;
    private ratings;
    constructor(users: UsersService, ratings: RatingsService);
    getMe(req: any): Promise<{
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
}
