import { PrismaService } from '../prisma/prisma.service';
import { NotificationsService } from '../notifications/notifications.service';
import { CreateSearchAlertDto } from './dto/create-search-alert.dto';
export declare class SearchAlertsService {
    private readonly prisma;
    private readonly notifications;
    constructor(prisma: PrismaService, notifications: NotificationsService);
    create(userId: string, dto: CreateSearchAlertDto): Promise<{
        id: string;
        createdAt: Date;
        userId: string;
        seats: number;
        isActive: boolean;
        origin: string;
        dest: string;
    }>;
    list(userId: string): import("@prisma/client").Prisma.PrismaPromise<{
        id: string;
        createdAt: Date;
        userId: string;
        seats: number;
        isActive: boolean;
        origin: string;
        dest: string;
    }[]>;
    remove(userId: string, id: string): Promise<{
        ok: boolean;
    }>;
    matchAndNotify(trip: {
        id: string;
        originAddress: string;
        destAddress: string;
    }): Promise<void>;
}
