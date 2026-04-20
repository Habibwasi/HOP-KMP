import { PrismaService } from '../prisma/prisma.service';
import { User } from '@prisma/client';
export declare class UsersService {
    private prisma;
    constructor(prisma: PrismaService);
    findByPhone(phone: string): Promise<User | null>;
    findById(id: string): Promise<User | null>;
    create(data: {
        phone: string;
        firstName: string;
        lastName: string;
        passwordHash?: string;
    }): Promise<User>;
    markVerified(userId: string): Promise<User>;
}
