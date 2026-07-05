"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
var ChatGateway_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.ChatGateway = void 0;
const websockets_1 = require("@nestjs/websockets");
const common_1 = require("@nestjs/common");
const socket_io_1 = require("socket.io");
const supabase_js_1 = require("@supabase/supabase-js");
const prisma_service_1 = require("../prisma/prisma.service");
const chat_service_1 = require("./chat.service");
let ChatGateway = ChatGateway_1 = class ChatGateway {
    supabase;
    prisma;
    chatService;
    server;
    logger = new common_1.Logger(ChatGateway_1.name);
    constructor(supabase, prisma, chatService) {
        this.supabase = supabase;
        this.prisma = prisma;
        this.chatService = chatService;
    }
    afterInit(server) {
        server.use(async (socket, next) => {
            const token = socket.handshake.auth?.token ??
                socket.handshake.headers?.authorization?.replace(/^Bearer\s+/i, '');
            if (!token) {
                this.logger.warn(`[Chat] Rejected unauthenticated connection ${socket.id}`);
                return next(new Error('Unauthorized'));
            }
            const { data: { user }, error } = await this.supabase.auth.getUser(token);
            if (error || !user) {
                this.logger.warn(`[Chat] Auth failed for connection ${socket.id}: ${error?.message}`);
                return next(new Error('Unauthorized'));
            }
            const prismaUser = await this.prisma.user.findUnique({ where: { id: user.id } });
            if (!prismaUser) {
                return next(new Error('Unauthorized'));
            }
            const sock = socket;
            sock.userId = prismaUser.id;
            sock.userFullName = `${prismaUser.firstName} ${prismaUser.lastName}`.trim();
            next();
        });
    }
    handleConnection(client) {
        const sock = client;
        this.logger.log(`[Chat] Connected: ${client.id} user=${sock.userId}`);
    }
    handleDisconnect(client) {
        this.logger.log(`[Chat] Disconnected: ${client.id}`);
    }
    async handleJoin(client, payload) {
        const sock = client;
        if (!sock.userId)
            throw new websockets_1.WsException('Unauthorized');
        try {
            await this.chatService.assertParticipant(payload.bookingId, sock.userId);
        }
        catch {
            throw new websockets_1.WsException('Not a participant of this booking');
        }
        await client.join(`booking:${payload.bookingId}`);
        this.logger.log(`[Chat] ${sock.userId} joined booking:${payload.bookingId}`);
        const history = await this.chatService.getHistory(payload.bookingId, sock.userId);
        client.emit('history', history);
        return { joined: payload.bookingId };
    }
    async handleMessage(client, payload) {
        const sock = client;
        if (!sock.userId)
            throw new websockets_1.WsException('Unauthorized');
        if (!payload.body?.trim())
            throw new websockets_1.WsException('Empty message');
        if (!client.rooms.has(`booking:${payload.bookingId}`)) {
            throw new websockets_1.WsException('Not a participant of this booking');
        }
        const message = await this.chatService.createMessage(payload.bookingId, sock.userId, sock.userFullName, payload.body.trim());
        client.to(`booking:${payload.bookingId}`).emit('message', message);
        client.emit('message', message);
        return { sent: true };
    }
    async handleHistory(client, payload) {
        const sock = client;
        if (!sock.userId)
            throw new websockets_1.WsException('Unauthorized');
        const messages = await this.chatService.getHistory(payload.bookingId, sock.userId);
        return { messages };
    }
};
exports.ChatGateway = ChatGateway;
__decorate([
    (0, websockets_1.WebSocketServer)(),
    __metadata("design:type", socket_io_1.Server)
], ChatGateway.prototype, "server", void 0);
__decorate([
    (0, websockets_1.SubscribeMessage)('join'),
    __param(0, (0, websockets_1.ConnectedSocket)()),
    __param(1, (0, websockets_1.MessageBody)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [socket_io_1.Socket, Object]),
    __metadata("design:returntype", Promise)
], ChatGateway.prototype, "handleJoin", null);
__decorate([
    (0, websockets_1.SubscribeMessage)('message'),
    __param(0, (0, websockets_1.ConnectedSocket)()),
    __param(1, (0, websockets_1.MessageBody)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [socket_io_1.Socket, Object]),
    __metadata("design:returntype", Promise)
], ChatGateway.prototype, "handleMessage", null);
__decorate([
    (0, websockets_1.SubscribeMessage)('history'),
    __param(0, (0, websockets_1.ConnectedSocket)()),
    __param(1, (0, websockets_1.MessageBody)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [socket_io_1.Socket, Object]),
    __metadata("design:returntype", Promise)
], ChatGateway.prototype, "handleHistory", null);
exports.ChatGateway = ChatGateway = ChatGateway_1 = __decorate([
    (0, websockets_1.WebSocketGateway)({
        namespace: '/chat',
        transports: ['websocket'],
        cors: { origin: process.env.ALLOWED_ORIGINS?.split(',').map((o) => o.trim()) ?? '*' },
    }),
    __param(0, (0, common_1.Inject)('SUPABASE_CLIENT')),
    __metadata("design:paramtypes", [supabase_js_1.SupabaseClient,
        prisma_service_1.PrismaService,
        chat_service_1.ChatService])
], ChatGateway);
//# sourceMappingURL=chat.gateway.js.map