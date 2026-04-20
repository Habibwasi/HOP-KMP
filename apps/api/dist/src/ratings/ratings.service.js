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
exports.RatingsService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
const client_1 = require("@prisma/client");
const RATING_WINDOW_HOURS = 48;
let RatingsService = class RatingsService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    async create(raterId, dto) {
        if (raterId === dto.rateeId) {
            throw new common_1.BadRequestException('Cannot rate yourself');
        }
        const trip = await this.prisma.trip.findUnique({
            where: { id: dto.tripId },
            include: {
                bookings: {
                    where: {
                        status: client_1.BookingStatus.CONFIRMED,
                        passengerId: raterId,
                    },
                },
            },
        });
        if (!trip)
            throw new common_1.NotFoundException('Trip not found');
        const isDriver = trip.driverId === raterId;
        const isPassenger = trip.bookings.length > 0;
        if (!isDriver && !isPassenger) {
            throw new common_1.BadRequestException('You were not part of this trip');
        }
        const windowEnd = new Date(trip.departureAt);
        windowEnd.setHours(windowEnd.getHours() + RATING_WINDOW_HOURS);
        if (new Date() > windowEnd) {
            throw new common_1.BadRequestException('Rating window has closed (48 hours after departure)');
        }
        const existing = await this.prisma.rating.findFirst({
            where: { raterId, rateeId: dto.rateeId },
        });
        if (existing)
            throw new common_1.ConflictException('Already rated this user for this trip');
        return this.prisma.rating.create({
            data: {
                raterId,
                rateeId: dto.rateeId,
                score: dto.score,
                comment: dto.comment,
            },
        });
    }
    async getUserRatings(userId) {
        const ratings = await this.prisma.rating.findMany({
            where: { rateeId: userId },
            include: {
                rater: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
            },
            orderBy: { createdAt: 'desc' },
        });
        const avg = ratings.length > 0
            ? ratings.reduce((sum, r) => sum + r.score, 0) / ratings.length
            : null;
        return {
            userId,
            averageScore: avg ? Math.round(avg * 10) / 10 : null,
            totalRatings: ratings.length,
            ratings,
        };
    }
};
exports.RatingsService = RatingsService;
exports.RatingsService = RatingsService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], RatingsService);
//# sourceMappingURL=ratings.service.js.map