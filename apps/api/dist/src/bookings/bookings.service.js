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
Object.defineProperty(exports, "__esModule", { value: true });
exports.BookingsService = void 0;
const common_1 = require("@nestjs/common");
const bullmq_1 = require("@nestjs/bullmq");
const bullmq_2 = require("bullmq");
const prisma_service_1 = require("../prisma/prisma.service");
const client_1 = require("@prisma/client");
let BookingsService = class BookingsService {
    prisma;
    bookingsQueue;
    constructor(prisma, bookingsQueue) {
        this.prisma = prisma;
        this.bookingsQueue = bookingsQueue;
    }
    async create(passengerId, dto) {
        return this.prisma.$transaction(async (tx) => {
            const trip = await tx.trip.findUnique({ where: { id: dto.tripId } });
            if (!trip)
                throw new common_1.NotFoundException('Trip not found');
            if (trip.status !== client_1.TripStatus.ACTIVE) {
                throw new common_1.BadRequestException('Trip is not active');
            }
            if (trip.driverId === passengerId) {
                throw new common_1.BadRequestException('Cannot book your own trip');
            }
            const confirmedBookings = await tx.booking.aggregate({
                where: { tripId: dto.tripId, status: client_1.BookingStatus.CONFIRMED },
                _sum: { seats: true },
            });
            const bookedSeats = confirmedBookings._sum.seats ?? 0;
            const available = trip.seats - bookedSeats;
            if (dto.seats > available) {
                throw new common_1.ConflictException(`Only ${available} seat(s) available`);
            }
            const existing = await tx.booking.findFirst({
                where: {
                    tripId: dto.tripId,
                    passengerId,
                    status: { in: [client_1.BookingStatus.PENDING, client_1.BookingStatus.CONFIRMED] },
                },
            });
            if (existing)
                throw new common_1.ConflictException('Already booked this trip');
            const totalOere = trip.pricePerSeat * dto.seats;
            const booking = await tx.booking.create({
                data: {
                    tripId: dto.tripId,
                    passengerId,
                    seats: dto.seats,
                    totalOere,
                    status: client_1.BookingStatus.PENDING,
                },
                include: {
                    trip: { include: { driver: { select: { id: true, firstName: true, lastName: true } } } },
                    passenger: { select: { id: true, firstName: true, lastName: true } },
                },
            });
            if (trip.model === client_1.TripModel.B && trip.thresholdDeadline) {
                const delay = new Date(trip.thresholdDeadline).getTime() - Date.now();
                if (delay > 0) {
                    await this.bookingsQueue.add('check-threshold', { tripId: dto.tripId }, { delay, jobId: `threshold-${dto.tripId}`, removeOnComplete: true });
                }
            }
            return booking;
        });
    }
    async confirm(bookingId) {
        const booking = await this.prisma.booking.findUnique({ where: { id: bookingId } });
        if (!booking)
            throw new common_1.NotFoundException('Booking not found');
        if (booking.status !== client_1.BookingStatus.PENDING) {
            throw new common_1.BadRequestException('Booking is not pending');
        }
        return this.prisma.booking.update({
            where: { id: bookingId },
            data: { status: client_1.BookingStatus.CONFIRMED },
        });
    }
    async cancel(bookingId, userId) {
        const booking = await this.prisma.booking.findUnique({
            where: { id: bookingId },
            include: { trip: true },
        });
        if (!booking)
            throw new common_1.NotFoundException('Booking not found');
        const isPassenger = booking.passengerId === userId;
        const isDriver = booking.trip.driverId === userId;
        if (!isPassenger && !isDriver)
            throw new common_1.ForbiddenException('Not authorised');
        if (booking.status === client_1.BookingStatus.CANCELLED) {
            throw new common_1.BadRequestException('Already cancelled');
        }
        return this.prisma.booking.update({
            where: { id: bookingId },
            data: { status: client_1.BookingStatus.CANCELLED },
        });
    }
    async findById(id) {
        const booking = await this.prisma.booking.findUnique({
            where: { id },
            include: {
                trip: true,
                passenger: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
                payment: true,
            },
        });
        if (!booking)
            throw new common_1.NotFoundException('Booking not found');
        return booking;
    }
    async findActiveForPassenger(passengerId) {
        return this.prisma.booking.findFirst({
            where: {
                passengerId,
                status: client_1.BookingStatus.CONFIRMED,
                trip: { departureAt: { gte: new Date() }, status: client_1.TripStatus.ACTIVE },
            },
            orderBy: { trip: { departureAt: 'asc' } },
            include: {
                trip: {
                    include: {
                        driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
                    },
                },
                payment: true,
            },
        });
    }
    async findByPassenger(passengerId) {
        return this.prisma.booking.findMany({
            where: { passengerId },
            orderBy: { createdAt: 'desc' },
            include: {
                trip: {
                    include: { driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
                },
                payment: true,
            },
        });
    }
    async checkModelBThreshold(tripId) {
        const trip = await this.prisma.trip.findUnique({ where: { id: tripId } });
        if (!trip || trip.status !== client_1.TripStatus.ACTIVE)
            return;
        const confirmed = await this.prisma.booking.aggregate({
            where: { tripId, status: client_1.BookingStatus.CONFIRMED },
            _sum: { seats: true },
        });
        const bookedSeats = confirmed._sum.seats ?? 0;
        if (bookedSeats < (trip.minPassengers ?? 0)) {
            await this.prisma.$transaction([
                this.prisma.trip.update({
                    where: { id: tripId },
                    data: { status: client_1.TripStatus.THRESHOLD_NOT_MET, isActive: false },
                }),
                this.prisma.booking.updateMany({
                    where: {
                        tripId,
                        status: { in: [client_1.BookingStatus.PENDING, client_1.BookingStatus.CONFIRMED] },
                    },
                    data: { status: client_1.BookingStatus.CANCELLED },
                }),
            ]);
        }
    }
};
exports.BookingsService = BookingsService;
exports.BookingsService = BookingsService = __decorate([
    (0, common_1.Injectable)(),
    __param(1, (0, bullmq_1.InjectQueue)('bookings')),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        bullmq_2.Queue])
], BookingsService);
//# sourceMappingURL=bookings.service.js.map