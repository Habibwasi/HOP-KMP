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
exports.AdminService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
const app_exception_1 = require("../common/errors/app-exception");
const api_error_codes_1 = require("../common/errors/api-error-codes");
let AdminService = class AdminService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    async listUsers(page = 1, limit = 20) {
        const skip = (page - 1) * limit;
        const [users, total] = await Promise.all([
            this.prisma.user.findMany({
                skip,
                take: limit,
                orderBy: { createdAt: 'desc' },
                select: {
                    id: true, phone: true, email: true,
                    firstName: true, lastName: true,
                    role: true, isVerified: true, isBanned: true,
                    banExpiresAt: true, isAdmin: true, createdAt: true,
                },
            }),
            this.prisma.user.count(),
        ]);
        return { users, total, page, pages: Math.ceil(total / limit) };
    }
    async banUser(userId, durationDays) {
        const user = await this.prisma.user.findUnique({ where: { id: userId } });
        if (!user)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.USER_NOT_FOUND);
        const banExpiresAt = durationDays === 'permanent'
            ? new Date('9999-12-31')
            : new Date(Date.now() + durationDays * 24 * 60 * 60 * 1000);
        return this.prisma.user.update({
            where: { id: userId },
            data: { isBanned: true, banExpiresAt },
        });
    }
    async unbanUser(userId) {
        return this.prisma.user.update({
            where: { id: userId },
            data: { isBanned: false, banExpiresAt: null },
        });
    }
    async makeAdmin(userId) {
        return this.prisma.user.update({
            where: { id: userId },
            data: { isAdmin: true },
        });
    }
    async getPendingLicences() {
        return this.prisma.driverLicence.findMany({
            where: { status: 'PENDING' },
            include: {
                user: { select: { id: true, firstName: true, lastName: true, phone: true } },
            },
            orderBy: { createdAt: 'asc' },
        });
    }
    async reviewLicence(licenceId, adminId, approved) {
        const licence = await this.prisma.driverLicence.findUnique({ where: { id: licenceId } });
        if (!licence)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.LICENCE_NOT_FOUND);
        await this.prisma.driverLicence.update({
            where: { id: licenceId },
            data: {
                status: approved ? 'CONFIRMED' : 'CANCELLED',
                reviewedAt: new Date(),
                reviewedBy: adminId,
            },
        });
        if (approved) {
            await this.prisma.user.update({
                where: { id: licence.userId },
                data: { role: 'DRIVER' },
            });
        }
        return { reviewed: true, approved };
    }
    async listTrips(page = 1, limit = 20) {
        const skip = (page - 1) * limit;
        const [trips, total] = await Promise.all([
            this.prisma.trip.findMany({
                skip,
                take: limit,
                orderBy: { createdAt: 'desc' },
                include: {
                    driver: { select: { id: true, firstName: true, lastName: true } },
                    _count: { select: { bookings: true } },
                },
            }),
            this.prisma.trip.count(),
        ]);
        return { trips, total, page, pages: Math.ceil(total / limit) };
    }
    async getDashboardStats() {
        const [totalUsers, verifiedUsers, bannedUsers, totalTrips, activeTrips, totalBookings, confirmedBookings, pendingLicences,] = await Promise.all([
            this.prisma.user.count(),
            this.prisma.user.count({ where: { isVerified: true } }),
            this.prisma.user.count({ where: { isBanned: true } }),
            this.prisma.trip.count(),
            this.prisma.trip.count({ where: { status: 'ACTIVE' } }),
            this.prisma.booking.count(),
            this.prisma.booking.count({ where: { status: 'CONFIRMED' } }),
            this.prisma.driverLicence.count({ where: { status: 'PENDING' } }),
        ]);
        return {
            users: { total: totalUsers, verified: verifiedUsers, banned: bannedUsers },
            trips: { total: totalTrips, active: activeTrips },
            bookings: { total: totalBookings, confirmed: confirmedBookings },
            pendingLicenceReviews: pendingLicences,
        };
    }
};
exports.AdminService = AdminService;
exports.AdminService = AdminService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], AdminService);
//# sourceMappingURL=admin.service.js.map