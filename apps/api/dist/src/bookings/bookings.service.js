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
var BookingsService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.BookingsService = void 0;
const common_1 = require("@nestjs/common");
const bullmq_1 = require("@nestjs/bullmq");
const bullmq_2 = require("bullmq");
const prisma_service_1 = require("../prisma/prisma.service");
const client_1 = require("@prisma/client");
const notifications_service_1 = require("../notifications/notifications.service");
const app_exception_1 = require("../common/errors/app-exception");
const api_error_codes_1 = require("../common/errors/api-error-codes");
let BookingsService = BookingsService_1 = class BookingsService {
    prisma;
    bookingsQueue;
    notifications;
    logger = new common_1.Logger(BookingsService_1.name);
    constructor(prisma, bookingsQueue, notifications) {
        this.prisma = prisma;
        this.bookingsQueue = bookingsQueue;
        this.notifications = notifications;
    }
    async create(passengerId, dto) {
        const booking = await this.prisma.$transaction(async (tx) => {
            const trip = await tx.trip.findUnique({ where: { id: dto.tripId } });
            if (!trip)
                throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.TRIP_NOT_FOUND);
            if (trip.status !== client_1.TripStatus.ACTIVE) {
                throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.TRIP_NOT_ACTIVE);
            }
            if (trip.driverId === passengerId) {
                throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.CANNOT_BOOK_OWN_TRIP);
            }
            const activeBookings = await tx.booking.aggregate({
                where: {
                    tripId: dto.tripId,
                    status: { in: [client_1.BookingStatus.CONFIRMED, client_1.BookingStatus.PENDING] },
                },
                _sum: { seats: true },
            });
            const bookedSeats = activeBookings._sum.seats ?? 0;
            const available = trip.seats - bookedSeats;
            if (dto.seats > available) {
                throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.INSUFFICIENT_SEATS, `Only ${available} seat(s) available`);
            }
            const existing = await tx.booking.findFirst({
                where: {
                    tripId: dto.tripId,
                    passengerId,
                    status: { in: [client_1.BookingStatus.PENDING, client_1.BookingStatus.CONFIRMED] },
                },
            });
            if (existing)
                throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.ALREADY_BOOKED);
            const totalOere = trip.pricePerSeat * dto.seats;
            const booking = await tx.booking.create({
                data: {
                    tripId: dto.tripId,
                    passengerId,
                    seats: dto.seats,
                    totalOere,
                    status: trip.model === client_1.TripModel.A ? client_1.BookingStatus.CONFIRMED : client_1.BookingStatus.PENDING,
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
        const passengerName = booking.passenger.firstName;
        this.notifications
            .sendToUser(booking.trip.driverId, 'New booking 🎉', `${passengerName} booked ${dto.seats} seat(s) on your trip`, { type: 'NEW_BOOKING', tripId: dto.tripId, bookingId: booking.id })
            .catch(() => { });
        this.prisma.notification.create({
            data: {
                userId: booking.trip.driverId,
                type: 'NEW_BOOKING',
                title: 'New booking 🎉',
                body: `${passengerName} booked ${dto.seats} seat(s) on your trip`,
                deepLinkId: booking.id,
            },
        }).catch(() => { });
        return booking;
    }
    async confirm(bookingId, driverId) {
        const booking = await this.prisma.booking.findUnique({
            where: { id: bookingId },
            include: { trip: { select: { driverId: true } } },
        });
        if (!booking)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.BOOKING_NOT_FOUND);
        if (booking.trip.driverId !== driverId)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.DRIVER_ONLY);
        if (booking.status === client_1.BookingStatus.CONFIRMED)
            return booking;
        if (booking.status !== client_1.BookingStatus.PENDING) {
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.BOOKING_NOT_PENDING);
        }
        return this.prisma.booking.update({
            where: { id: bookingId },
            data: { status: client_1.BookingStatus.CONFIRMED },
        });
    }
    async findByIdAuthorized(id, requesterId) {
        const booking = await this.findById(id);
        const isParty = booking.passengerId === requesterId || booking.trip.driverId === requesterId;
        if (!isParty)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.NOT_A_PARTY);
        return booking;
    }
    async cancel(bookingId, userId) {
        const booking = await this.prisma.booking.findUnique({
            where: { id: bookingId },
            include: { trip: true },
        });
        if (!booking)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.BOOKING_NOT_FOUND);
        const isPassenger = booking.passengerId === userId;
        const isDriver = booking.trip.driverId === userId;
        if (!isPassenger && !isDriver)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.NOT_A_PARTY);
        if (booking.status === client_1.BookingStatus.CANCELLED) {
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.ALREADY_CANCELLED);
        }
        await this.prisma.booking.update({
            where: { id: bookingId },
            data: { status: client_1.BookingStatus.CANCELLED },
        });
        return { cancelled: true };
    }
    async cancelAllForTrip(tripId) {
        await this.prisma.booking.updateMany({
            where: {
                tripId,
                status: { in: [client_1.BookingStatus.PENDING, client_1.BookingStatus.CONFIRMED] },
            },
            data: { status: client_1.BookingStatus.CANCELLED },
        });
    }
    async findById(id) {
        const booking = await this.prisma.booking.findUnique({
            where: { id },
            include: {
                trip: true,
                passenger: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
            },
        });
        if (!booking)
            throw new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.BOOKING_NOT_FOUND);
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
            },
        });
    }
    async findMyChats(userId) {
        const [asPassenger, asDriver] = await Promise.all([
            this.prisma.booking.findMany({
                where: { passengerId: userId, status: { not: 'CANCELLED' } },
                orderBy: { createdAt: 'desc' },
                include: {
                    trip: {
                        include: { driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
                    },
                },
            }),
            this.prisma.booking.findMany({
                where: { trip: { driverId: userId }, status: { not: 'CANCELLED' } },
                orderBy: { createdAt: 'desc' },
                include: {
                    passenger: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
                    trip: true,
                },
            }),
        ]);
        const passengerChats = asPassenger.map((b) => ({
            bookingId: b.id,
            bookingStatus: b.status,
            tripOrigin: b.trip.originAddress,
            tripDest: b.trip.destAddress,
            departureAt: b.trip.departureAt.toISOString(),
            otherPartyId: b.trip.driver.id,
            otherPartyName: `${b.trip.driver.firstName} ${b.trip.driver.lastName}`.trim(),
            otherPartyAvatarUrl: b.trip.driver.avatarUrl ?? null,
            myRole: 'PASSENGER',
        }));
        const driverChats = asDriver.map((b) => ({
            bookingId: b.id,
            bookingStatus: b.status,
            tripOrigin: b.trip.originAddress,
            tripDest: b.trip.destAddress,
            departureAt: b.trip.departureAt.toISOString(),
            otherPartyId: b.passenger.id,
            otherPartyName: `${b.passenger.firstName} ${b.passenger.lastName}`.trim(),
            otherPartyAvatarUrl: b.passenger.avatarUrl ?? null,
            myRole: 'DRIVER',
        }));
        return [...passengerChats, ...driverChats].sort((a, b) => new Date(b.departureAt).getTime() - new Date(a.departureAt).getTime());
    }
    async checkModelBThreshold(tripId) {
        const trip = await this.prisma.trip.findUnique({
            where: { id: tripId },
            include: { driver: { select: { id: true, firstName: true, lastName: true } } },
        });
        if (!trip || trip.status !== client_1.TripStatus.ACTIVE)
            return;
        const pending = await this.prisma.booking.aggregate({
            where: { tripId, status: client_1.BookingStatus.PENDING },
            _sum: { seats: true },
        });
        const pendingSeats = pending._sum.seats ?? 0;
        if (pendingSeats >= (trip.minPassengers ?? 0)) {
            await this.prisma.booking.updateMany({
                where: { tripId, status: client_1.BookingStatus.PENDING },
                data: { status: client_1.BookingStatus.CONFIRMED },
            });
            const confirmedBookings = await this.prisma.booking.findMany({
                where: { tripId, status: client_1.BookingStatus.CONFIRMED },
                select: { passengerId: true },
            });
            const title = 'Trip is a go!';
            const body = `Your Model B trip from ${trip.originAddress} to ${trip.destAddress} has reached the minimum threshold. It\'s confirmed!`;
            await this.notifications.sendToUser(trip.driverId, title, body, { type: 'THRESHOLD_MET', tripId });
            await this.prisma.notification.create({
                data: { userId: trip.driverId, type: 'THRESHOLD_MET', title, body, deepLinkId: tripId },
            });
            for (const { passengerId } of confirmedBookings) {
                await this.notifications.sendToUser(passengerId, title, body, { type: 'THRESHOLD_MET', tripId });
                await this.prisma.notification.create({
                    data: { userId: passengerId, type: 'THRESHOLD_MET', title, body, deepLinkId: tripId },
                });
            }
            return;
        }
        const activeBookings = await this.prisma.booking.findMany({
            where: { tripId, status: { in: [client_1.BookingStatus.PENDING, client_1.BookingStatus.CONFIRMED] } },
            select: { passengerId: true },
        });
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
        const cancelTitle = 'Trip cancelled — threshold not reached';
        const cancelBody = `The Model B trip from ${trip.originAddress} to ${trip.destAddress} did not reach the minimum passenger count and has been cancelled.`;
        await this.notifications.sendToUser(trip.driverId, cancelTitle, cancelBody, { type: 'GENERAL', tripId });
        await this.prisma.notification.create({
            data: { userId: trip.driverId, type: 'GENERAL', title: cancelTitle, body: cancelBody, deepLinkId: tripId },
        });
        for (const { passengerId } of activeBookings) {
            await this.notifications.sendToUser(passengerId, cancelTitle, cancelBody, { type: 'BOOKING_CANCELLED', tripId });
            await this.prisma.notification.create({
                data: { userId: passengerId, type: 'BOOKING_CANCELLED', title: cancelTitle, body: cancelBody, deepLinkId: tripId },
            });
        }
    }
};
exports.BookingsService = BookingsService;
exports.BookingsService = BookingsService = BookingsService_1 = __decorate([
    (0, common_1.Injectable)(),
    __param(1, (0, bullmq_1.InjectQueue)('bookings')),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        bullmq_2.Queue,
        notifications_service_1.NotificationsService])
], BookingsService);
//# sourceMappingURL=bookings.service.js.map