import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../prisma/prisma.service';
export declare class NotificationsService {
    private prisma;
    private config;
    private readonly logger;
    private apnProvider;
    private fcmInitialised;
    constructor(prisma: PrismaService, config: ConfigService);
    private initApn;
    private initFcm;
    sendToUser(userId: string, title: string, body: string, data?: Record<string, string>): Promise<void>;
    private sendApns;
    private sendFcm;
    registerToken(userId: string, token: string, platform: 'ios' | 'android'): Promise<{
        id: string;
        createdAt: Date;
        userId: string;
        token: string;
        platform: string;
    }>;
    removeToken(token: string): Promise<import("@prisma/client").Prisma.BatchPayload>;
    getForUser(userId: string): Promise<{
        id: string;
        createdAt: Date;
        userId: string;
        title: string;
        body: string;
        type: import("@prisma/client").$Enums.NotificationType;
        isRead: boolean;
        deepLinkId: string | null;
    }[]>;
    markRead(notificationId: string, userId: string): Promise<import("@prisma/client").Prisma.BatchPayload>;
}
