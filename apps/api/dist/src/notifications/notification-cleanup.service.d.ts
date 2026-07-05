import { PrismaService } from '../prisma/prisma.service';
export declare class NotificationCleanupService {
    private readonly prisma;
    private readonly logger;
    constructor(prisma: PrismaService);
    deleteOldNotifications(): Promise<void>;
}
