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
exports.SearchAlertsService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
const notifications_service_1 = require("../notifications/notifications.service");
let SearchAlertsService = class SearchAlertsService {
    prisma;
    notifications;
    constructor(prisma, notifications) {
        this.prisma = prisma;
        this.notifications = notifications;
    }
    async create(userId, dto) {
        const existing = await this.prisma.searchAlert.findFirst({
            where: {
                userId,
                origin: { equals: dto.origin, mode: 'insensitive' },
                dest: { equals: dto.dest, mode: 'insensitive' },
                isActive: true,
            },
        });
        if (existing)
            throw new common_1.ConflictException('Alert already exists for this route');
        return this.prisma.searchAlert.create({
            data: {
                userId,
                origin: dto.origin.trim(),
                dest: dto.dest.trim(),
                seats: dto.seats ?? 1,
            },
        });
    }
    list(userId) {
        return this.prisma.searchAlert.findMany({
            where: { userId, isActive: true },
            orderBy: { createdAt: 'desc' },
        });
    }
    async remove(userId, id) {
        const alert = await this.prisma.searchAlert.findUnique({ where: { id } });
        if (!alert)
            throw new common_1.NotFoundException('Alert not found');
        if (alert.userId !== userId)
            throw new common_1.ForbiddenException();
        await this.prisma.searchAlert.update({ where: { id }, data: { isActive: false } });
        return { ok: true };
    }
    async matchAndNotify(trip) {
        const alerts = await this.prisma.searchAlert.findMany({
            where: {
                isActive: true,
                origin: { contains: trip.originAddress.split(',')[0].trim(), mode: 'insensitive' },
                dest: { contains: trip.destAddress.split(',')[0].trim(), mode: 'insensitive' },
            },
        });
        if (!alerts.length)
            return;
        const title = 'New ride available!';
        const body = `A ride from ${trip.originAddress} to ${trip.destAddress} just appeared.`;
        await Promise.all(alerts.map(async (alert) => {
            await this.notifications.sendToUser(alert.userId, title, body, {
                type: 'SEARCH_ALERT',
                tripId: trip.id,
            });
            await this.prisma.notification.create({
                data: {
                    userId: alert.userId,
                    type: 'SEARCH_ALERT',
                    title,
                    body,
                    deepLinkId: trip.id,
                },
            });
        }));
    }
};
exports.SearchAlertsService = SearchAlertsService;
exports.SearchAlertsService = SearchAlertsService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        notifications_service_1.NotificationsService])
], SearchAlertsService);
//# sourceMappingURL=search-alerts.service.js.map