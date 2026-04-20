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
        token: string;
        userId: string;
        platform: string;
        createdAt: Date;
    }>;
    remove(token: string): Promise<import("@prisma/client").Prisma.BatchPayload>;
}
export {};
