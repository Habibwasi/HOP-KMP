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
Object.defineProperty(exports, "__esModule", { value: true });
exports.ChatService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
let ChatService = class ChatService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    async assertParticipant(bookingId, userId) {
        const booking = await this.prisma.booking.findUnique({
            where: { id: bookingId },
            include: { trip: { select: { driverId: true } } },
        });
        if (!booking)
            throw new common_1.NotFoundException('Booking not found');
        const isPassenger = booking.passengerId === userId;
        const isDriver = booking.trip.driverId === userId;
        if (!isPassenger && !isDriver)
            throw new common_1.ForbiddenException('Not a participant');
    }
    async createMessage(bookingId, senderId, senderName, body) {
        return this.prisma.chatMessage.create({
            data: { bookingId, senderId, body },
            select: {
                id: true,
                bookingId: true,
                senderId: true,
                body: true,
                createdAt: true,
            },
        }).then((msg) => ({ ...msg, senderName, timestampMs: msg.createdAt.getTime() }));
    }
    async getHistory(bookingId, requesterId) {
        await this.assertParticipant(bookingId, requesterId);
        const messages = await this.prisma.chatMessage.findMany({
            where: { bookingId },
            orderBy: { createdAt: 'asc' },
            take: 50,
            include: {
                sender: { select: { firstName: true, lastName: true } },
            },
        });
        return messages.map((m) => ({
            id: m.id,
            bookingId: m.bookingId,
            senderId: m.senderId,
            senderName: `${m.sender.firstName} ${m.sender.lastName}`.trim(),
            body: m.body,
            timestampMs: m.createdAt.getTime(),
        }));
    }
};
exports.ChatService = ChatService;
exports.ChatService = ChatService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], ChatService);
//# sourceMappingURL=chat.service.js.map