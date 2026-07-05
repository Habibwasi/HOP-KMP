import { OnGatewayConnection, OnGatewayDisconnect, OnGatewayInit } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { SupabaseClient } from '@supabase/supabase-js';
import { PrismaService } from '../prisma/prisma.service';
import { ChatService } from './chat.service';
export declare class ChatGateway implements OnGatewayInit, OnGatewayConnection, OnGatewayDisconnect {
    private readonly supabase;
    private readonly prisma;
    private readonly chatService;
    server: Server;
    private readonly logger;
    constructor(supabase: SupabaseClient, prisma: PrismaService, chatService: ChatService);
    afterInit(server: Server): void;
    handleConnection(client: Socket): void;
    handleDisconnect(client: Socket): void;
    handleJoin(client: Socket, payload: {
        bookingId: string;
    }): Promise<{
        joined: string;
    }>;
    handleMessage(client: Socket, payload: {
        bookingId: string;
        body: string;
    }): Promise<{
        sent: boolean;
    }>;
    handleHistory(client: Socket, payload: {
        bookingId: string;
    }): Promise<{
        messages: {
            id: string;
            bookingId: string;
            senderId: string;
            senderName: string;
            body: string;
            timestampMs: number;
        }[];
    }>;
}
