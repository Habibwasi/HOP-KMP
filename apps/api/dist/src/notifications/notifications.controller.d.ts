import { NotificationsService } from './notifications.service';
declare class RegisterTokenDto {
    token: string;
    platform: 'ios' | 'android';
}
export declare class NotificationsController {
    private notifications;
    constructor(notifications: NotificationsService);
    register(req: any, dto: RegisterTokenDto): Promise<{
        id: string;
        createdAt: Date;
        token: string;
        platform: string;
        userId: string;
    }>;
    remove(token: string): Promise<import("@prisma/client").Prisma.BatchPayload>;
    getAll(req: any): Promise<{
        id: string;
        createdAt: Date;
        userId: string;
        type: import("@prisma/client").$Enums.NotificationType;
        title: string;
        body: string;
        isRead: boolean;
        deepLinkId: string | null;
    }[]>;
    markRead(req: any, id: string): Promise<import("@prisma/client").Prisma.BatchPayload>;
}
export {};
