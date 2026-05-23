import { PrismaService } from '../prisma/prisma.service';
export declare class ChatService {
    private prisma;
    constructor(prisma: PrismaService);
    assertParticipant(bookingId: string, userId: string): Promise<void>;
    createMessage(bookingId: string, senderId: string, senderName: string, body: string): Promise<{
        senderName: string;
        timestampMs: number;
        id: string;
        createdAt: Date;
        body: string;
        bookingId: string;
        senderId: string;
    }>;
    getHistory(bookingId: string, requesterId: string): Promise<{
        id: string;
        bookingId: string;
        senderId: string;
        senderName: string;
        body: string;
        timestampMs: number;
    }[]>;
}
