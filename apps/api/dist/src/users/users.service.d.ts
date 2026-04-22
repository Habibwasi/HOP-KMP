import { PrismaService } from '../prisma/prisma.service';
import { User } from '@prisma/client';
export declare class UsersService {
    private prisma;
    constructor(prisma: PrismaService);
    findByPhone(phone: string): Promise<User | null>;
    findByEmail(email: string): Promise<User | null>;
    findById(id: string): Promise<User | null>;
    create(data: {
        phone: string;
        firstName: string;
        lastName: string;
        email?: string;
        passwordHash?: string;
    }): Promise<User>;
    markVerified(userId: string): Promise<User>;
    getCarDetails(userId: string): Promise<{
        id: string;
        userId: string;
        make: string;
        model: string;
        year: number;
        licensePlate: string;
        colour: string;
        seatsAvailable: number;
        createdAt: Date;
        updatedAt: Date;
    } | null>;
}
