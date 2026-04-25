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
exports.TripsService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
const pricing_service_1 = require("./pricing.service");
const client_1 = require("@prisma/client");
let TripsService = class TripsService {
    prisma;
    pricing;
    constructor(prisma, pricing) {
        this.prisma = prisma;
        this.pricing = pricing;
    }
    async create(driverId, dto) {
        if (dto.model === client_1.TripModel.B) {
            if (!dto.minPassengers) {
                throw new common_1.BadRequestException('minPassengers is required for Model B trips');
            }
            if (!dto.thresholdDeadline) {
                throw new common_1.BadRequestException('thresholdDeadline is required for Model B trips');
            }
            const deadline = new Date(dto.thresholdDeadline);
            const departure = new Date(dto.departureAt);
            if (deadline >= departure) {
                throw new common_1.BadRequestException('thresholdDeadline must be before departureAt');
            }
        }
        const distanceKm = this.pricing.calculateDistance(dto.originLat, dto.originLng, dto.destLat, dto.destLng);
        const pricePerSeat = this.pricing.calculatePricePerSeat(distanceKm, dto.seats);
        const trip = await this.prisma.trip.create({
            data: {
                driverId,
                model: dto.model,
                originLat: dto.originLat,
                originLng: dto.originLng,
                originAddress: dto.originAddress,
                destLat: dto.destLat,
                destLng: dto.destLng,
                destAddress: dto.destAddress,
                departureAt: new Date(dto.departureAt),
                seats: dto.seats,
                pricePerSeat,
                distanceKm,
                isRecurring: dto.model === client_1.TripModel.A,
                recurringDays: dto.recurringDays ?? [],
                minPassengers: dto.minPassengers,
                thresholdDeadline: dto.thresholdDeadline ? new Date(dto.thresholdDeadline) : null,
            },
            include: { driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
        });
        return trip;
    }
    async search(dto) {
        const radiusKm = dto.radiusKm ?? 5;
        const seats = dto.seats ?? 1;
        const date = new Date(dto.date);
        const dayStart = new Date(date);
        dayStart.setHours(0, 0, 0, 0);
        const dayEnd = new Date(date);
        dayEnd.setHours(23, 59, 59, 999);
        const useCoordinates = dto.originLat != null &&
            dto.originLng != null &&
            dto.destLat != null &&
            dto.destLng != null;
        const where = {
            status: client_1.TripStatus.ACTIVE,
            departureAt: { gte: dayStart, lte: dayEnd },
            seats: { gte: seats },
        };
        if (!useCoordinates) {
            if (dto.origin) {
                where.originAddress = { contains: dto.origin, mode: 'insensitive' };
            }
            if (dto.dest) {
                where.destAddress = { contains: dto.dest, mode: 'insensitive' };
            }
        }
        const trips = await this.prisma.trip.findMany({
            where,
            include: {
                driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
                bookings: { where: { status: 'CONFIRMED' } },
            },
        });
        return trips
            .filter((trip) => {
            const bookedSeats = trip.bookings.reduce((sum, b) => sum + b.seats, 0);
            const availableSeats = trip.seats - bookedSeats;
            if (useCoordinates) {
                const originDist = this.pricing.calculateDistance(dto.originLat, dto.originLng, trip.originLat, trip.originLng);
                const destDist = this.pricing.calculateDistance(dto.destLat, dto.destLng, trip.destLat, trip.destLng);
                return originDist <= radiusKm && destDist <= radiusKm && availableSeats >= seats;
            }
            return availableSeats >= seats;
        })
            .map((trip) => {
            const bookedSeats = trip.bookings.reduce((sum, b) => sum + b.seats, 0);
            return {
                ...trip,
                availableSeats: trip.seats - bookedSeats,
                bookings: undefined,
            };
        });
    }
    async findById(id) {
        const trip = await this.prisma.trip.findUnique({
            where: { id },
            include: {
                driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
                bookings: {
                    where: { status: 'CONFIRMED' },
                    include: { passenger: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
                },
            },
        });
        if (!trip)
            throw new common_1.NotFoundException('Trip not found');
        return trip;
    }
    async cancel(tripId, userId) {
        const trip = await this.prisma.trip.findUnique({ where: { id: tripId } });
        if (!trip)
            throw new common_1.NotFoundException('Trip not found');
        if (trip.driverId !== userId)
            throw new common_1.ForbiddenException('Not your trip');
        if (trip.status === client_1.TripStatus.CANCELLED) {
            throw new common_1.BadRequestException('Trip already cancelled');
        }
        return this.prisma.trip.update({
            where: { id: tripId },
            data: { status: client_1.TripStatus.CANCELLED, isActive: false },
        });
    }
    async findByDriver(driverId) {
        return this.prisma.trip.findMany({
            where: { driverId },
            orderBy: { departureAt: 'desc' },
            include: {
                bookings: { where: { status: 'CONFIRMED' } },
            },
        });
    }
    async findByPassenger(passengerId) {
        const bookings = await this.prisma.booking.findMany({
            where: { passengerId, status: { not: 'CANCELLED' } },
            include: {
                trip: {
                    include: {
                        driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
                        bookings: { where: { status: 'CONFIRMED' } },
                    },
                },
            },
            orderBy: { createdAt: 'desc' },
        });
        return bookings.map((b) => b.trip);
    }
};
exports.TripsService = TripsService;
exports.TripsService = TripsService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        pricing_service_1.PricingService])
], TripsService);
//# sourceMappingURL=trips.service.js.map