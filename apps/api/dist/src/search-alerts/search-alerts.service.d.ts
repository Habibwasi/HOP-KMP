import { PrismaService } from '../prisma/prisma.service';
import { NotificationsService } from '../notifications/notifications.service';
import { CreateSearchAlertDto } from './dto/create-search-alert.dto';
export declare class SearchAlertsService {
    private readonly prisma;
    private readonly notifications;
    constructor(prisma: PrismaService, notifications: NotificationsService);
    create(userId: string, dto: CreateSearchAlertDto): Promise<{
        id: string;
        userId: string;
        createdAt: Date;
        origin: string;
        dest: string;
        seats: number;
        isActive: boolean;
    }>;
    list(userId: string): import("@prisma/client").Prisma.PrismaPromise<{
        id: string;
        userId: string;
        createdAt: Date;
        origin: string;
        dest: string;
        seats: number;
        isActive: boolean;
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
