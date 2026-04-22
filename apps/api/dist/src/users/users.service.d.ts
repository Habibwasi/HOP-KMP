import { PrismaService } from '../prisma/prisma.service';
import { User } from '@prisma/client';
export declare class UsersService {
    private prisma;
    private readonly logger;
    constructor(prisma: PrismaService);
    findByPhone(phone: string): Promise<User | null>;
    findByEmail(email: string): Promise<User | null>;
    findById(id: string): Promise<User | null>;
    createProfile(supabaseId: string, data: {
        firstName: string;
        lastName: string;
        phone?: string;
        email?: string;
    }): Promise<User>;
    markVerified(userId: string): Promise<User>;
    updateProfile(userId: string, data: {
        firstName: string;
        lastName: string;
    }): Promise<User>;
    reportUser(reportedId: string, reporterId: string, reason: string): Promise<void>;
    getCarDetails(userId: string): Promise<{
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
    } | null>;
}
