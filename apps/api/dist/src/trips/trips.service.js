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
var TripsService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.TripsService = void 0;
const common_1 = require("@nestjs/common");
const bullmq_1 = require("@nestjs/bullmq");
const bullmq_2 = require("bullmq");
const prisma_service_1 = require("../prisma/prisma.service");
const pricing_service_1 = require("./pricing.service");
const client_1 = require("@prisma/client");
const search_alerts_processor_1 = require("../search-alerts/search-alerts.processor");
const trips_constants_1 = require("./trips.constants");
const bookings_service_1 = require("../bookings/bookings.service");
const notifications_service_1 = require("../notifications/notifications.service");
const DAY_CODE_TO_UTC_DOW = {
    SUN: 0,
    MON: 1,
    TUE: 2,
    WED: 3,
    THU: 4,
    FRI: 5,
    SAT: 6,
};
const expandAddressToken = (token) => {
    const aliases = {
        copenhagen: ['kobenhavn', 'kbh'],
        kobenhavn: ['copenhagen', 'kbh'],
        kbh: ['kobenhavn', 'copenhagen'],
        aarhus: ['arhus'],
        arhus: ['aarhus'],
    };
    return [token, ...(aliases[token] ?? [])];
};
const normalizeSearchText = (value) => {
    const tokens = (value ?? '')
        .toLowerCase()
        .replace(/æ/g, 'ae')
        .replace(/ø/g, 'o')
        .replace(/å/g, 'a')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .split(/[^a-z0-9]+/)
        .filter((part) => part.length >= 2);
    return [...new Set(tokens.flatMap(expandAddressToken))];
};
const addressMatches = (address, query) => {
    const queryTokens = normalizeSearchText(query);
    if (queryTokens.length === 0)
        return true;
    const addressTokens = normalizeSearchText(address);
    const addressText = addressTokens.join(' ');
    return queryTokens.some((token) => addressText.includes(token) ||
        addressTokens.some((addressToken) => addressToken.includes(token) || token.includes(addressToken)));
};
function buildRecurringDates(anchorDate, dayCodes, windowDays) {
    const targetDows = new Set(dayCodes.map((d) => DAY_CODE_TO_UTC_DOW[d.toUpperCase()]).filter((n) => n !== undefined));
    const hours = anchorDate.getUTCHours();
    const minutes = anchorDate.getUTCMinutes();
    const dates = [];
    const cursor = new Date(Date.UTC(anchorDate.getUTCFullYear(), anchorDate.getUTCMonth(), anchorDate.getUTCDate(), hours, minutes, 0, 0));
    for (let i = 0; i <= windowDays; i++) {
        if (targetDows.has(cursor.getUTCDay())) {
            dates.push(new Date(cursor));
        }
        cursor.setUTCDate(cursor.getUTCDate() + 1);
    }
    return dates;
}
let TripsService = TripsService_1 = class TripsService {
    prisma;
    pricing;
    alertsQueue;
    bookingsQueue;
    bookings;
    notifications;
    logger = new common_1.Logger(TripsService_1.name);
    constructor(prisma, pricing, alertsQueue, bookingsQueue, bookings, notifications) {
        this.prisma = prisma;
        this.pricing = pricing;
        this.alertsQueue = alertsQueue;
        this.bookingsQueue = bookingsQueue;
        this.bookings = bookings;
        this.notifications = notifications;
    }
    async create(driverId, dto) {
        const driver = await this.prisma.user.findUnique({ where: { id: driverId } });
        if (!driver?.mobilepayNumber) {
            throw new common_1.BadRequestException('Please add your MobilePay number in your profile before creating a trip');
        }
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
        if (dto.model === client_1.TripModel.A) {
            if (!dto.recurringDays || dto.recurringDays.length === 0) {
                throw new common_1.BadRequestException('recurringDays is required for Model A trips');
            }
        }
        const distanceKm = dto.distanceMetres
            ? dto.distanceMetres / 1000
            : this.pricing.calculateDistance(dto.originLat, dto.originLng, dto.destLat, dto.destLng);
        const pricePerSeat = this.pricing.calculatePricePerSeat(distanceKm, dto.seats);
        const baseData = {
            driverId,
            model: dto.model,
            originLat: dto.originLat,
            originLng: dto.originLng,
            originAddress: dto.originAddress,
            destLat: dto.destLat,
            destLng: dto.destLng,
            destAddress: dto.destAddress,
            seats: dto.seats,
            pricePerSeat,
            distanceKm,
            isRecurring: dto.model === client_1.TripModel.A,
            recurringDays: dto.recurringDays ?? [],
            minPassengers: dto.minPassengers,
            thresholdDeadline: dto.thresholdDeadline ? new Date(dto.thresholdDeadline) : null,
        };
        if (dto.model === client_1.TripModel.A) {
            const anchor = new Date(dto.departureAt);
            const dates = buildRecurringDates(anchor, dto.recurringDays, 30);
            if (dates.length === 0) {
                throw new common_1.BadRequestException('No occurrences found in the next 30 days for the selected days');
            }
            const instances = dates.map((d) => ({ ...baseData, departureAt: d }));
            await this.prisma.trip.createMany({ data: instances });
            const firstTrip = await this.prisma.trip.findFirst({
                where: {
                    driverId,
                    originAddress: dto.originAddress,
                    destAddress: dto.destAddress,
                    isRecurring: true,
                    departureAt: dates[0],
                },
                include: {
                    driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } },
                },
            });
            if (firstTrip) {
                await this.alertsQueue.add(search_alerts_processor_1.MATCH_ALERTS_JOB, {
                    tripId: firstTrip.id,
                    originAddress: firstTrip.originAddress,
                    destAddress: firstTrip.destAddress,
                });
            }
            return firstTrip;
        }
        const trip = await this.prisma.trip.create({
            data: {
                ...baseData,
                departureAt: new Date(dto.departureAt),
            },
            include: { driver: { select: { id: true, firstName: true, lastName: true, avatarUrl: true } } },
        });
        await this.alertsQueue.add(search_alerts_processor_1.MATCH_ALERTS_JOB, {
            tripId: trip.id,
            originAddress: trip.originAddress,
            destAddress: trip.destAddress,
        });
        if (trip.thresholdDeadline) {
            const delay = new Date(trip.thresholdDeadline).getTime() - Date.now();
            if (delay > 0) {
                await this.bookingsQueue.add(trips_constants_1.CHECK_THRESHOLD_JOB, { tripId: trip.id }, { delay, jobId: `threshold-${trip.id}`, removeOnComplete: true });
            }
        }
        return trip;
    }
    async extendRecurringWindow() {
        const now = new Date();
        const horizon = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);
        const futureTrips = await this.prisma.trip.findMany({
            where: {
                isRecurring: true,
                departureAt: { gte: now },
                status: { not: client_1.TripStatus.CANCELLED },
            },
        });
        const groups = new Map();
        for (const trip of futureTrips) {
            const key = `${trip.driverId}||${trip.originAddress}||${trip.destAddress}`;
            if (!groups.has(key))
                groups.set(key, []);
            groups.get(key).push(trip);
        }
        for (const [, trips] of groups) {
            const template = trips[0];
            const maxDate = trips.reduce((mx, t) => (t.departureAt > mx ? t.departureAt : mx), trips[0].departureAt);
            const daysRemaining = (maxDate.getTime() - now.getTime()) / (24 * 60 * 60 * 1000);
            if (daysRemaining >= 7)
                continue;
            const extendAnchor = new Date(maxDate.getTime() + 24 * 60 * 60 * 1000);
            const newDates = buildRecurringDates(extendAnchor, template.recurringDays, 30);
            if (newDates.length === 0)
                continue;
            const newInstances = newDates
                .filter((d) => d <= horizon)
                .map((d) => ({
                driverId: template.driverId,
                model: template.model,
                originLat: template.originLat,
                originLng: template.originLng,
                originAddress: template.originAddress,
                destLat: template.destLat,
                destLng: template.destLng,
                destAddress: template.destAddress,
                departureAt: d,
                seats: template.seats,
                pricePerSeat: template.pricePerSeat,
                distanceKm: template.distanceKm,
                isRecurring: true,
                recurringDays: template.recurringDays,
            }));
            if (newInstances.length > 0) {
                await this.prisma.trip.createMany({ data: newInstances, skipDuplicates: true });
            }
        }
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
            return availableSeats >= seats &&
                addressMatches(trip.originAddress, dto.origin) &&
                addressMatches(trip.destAddress, dto.dest);
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
        await this.prisma.trip.update({
            where: { id: tripId },
            data: { status: client_1.TripStatus.CANCELLED, isActive: false },
        });
        await this.bookings.cancelAllForTrip(tripId).catch((err) => {
            this.logger.error(`[Trips] cancelAllForTrip failed for trip ${tripId}: ${err?.message}`);
        });
        return { cancelled: true };
    }
    async complete(tripId, userId) {
        const trip = await this.prisma.trip.findUnique({
            where: { id: tripId },
            include: {
                driver: { select: { mobilepayNumber: true } },
                bookings: { where: { status: client_1.BookingStatus.CONFIRMED } },
            },
        });
        if (!trip)
            throw new common_1.NotFoundException('Trip not found');
        if (trip.driverId !== userId)
            throw new common_1.ForbiddenException('Not your trip');
        if (!trip.driver.mobilepayNumber) {
            throw new common_1.BadRequestException('Add your MobilePay number before completing a trip');
        }
        const driver = trip.driver;
        if (trip.status === client_1.TripStatus.COMPLETED)
            return { completed: true };
        if (trip.status !== client_1.TripStatus.ACTIVE) {
            throw new common_1.BadRequestException('Trip must be ACTIVE to complete');
        }
        await this.prisma.trip.update({
            where: { id: tripId },
            data: { status: client_1.TripStatus.COMPLETED, isActive: false },
        });
        for (const booking of trip.bookings) {
            try {
                await this.prisma.$transaction([
                    this.prisma.rideSettlement.create({
                        data: {
                            bookingId: booking.id,
                            suggestedAmountOere: booking.totalOere,
                            mobilepayNumber: driver.mobilepayNumber,
                        },
                    }),
                    this.prisma.booking.update({
                        where: { id: booking.id },
                        data: { status: client_1.BookingStatus.AWAITING_PAYMENT },
                    }),
                ]);
                await this.notifications.sendToUser(booking.passengerId, 'Time to pay your driver', `Please send DKK ${Math.round(booking.totalOere / 100)} to your driver via MobilePay.`, { type: 'RIDE_AWAITING_PAYMENT', bookingId: booking.id }).catch(() => { });
                await this.prisma.notification.create({
                    data: {
                        userId: booking.passengerId,
                        type: 'RIDE_AWAITING_PAYMENT',
                        title: 'Time to pay your driver',
                        body: `Please send DKK ${Math.round(booking.totalOere / 100)} to your driver via MobilePay.`,
                        deepLinkId: booking.id,
                    },
                });
            }
            catch (err) {
                this.logger.error(`[Trips] Failed to create settlement for booking ${booking.id}: ${err?.message}`);
            }
        }
        return { completed: true };
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
exports.TripsService = TripsService = TripsService_1 = __decorate([
    (0, common_1.Injectable)(),
    __param(2, (0, bullmq_1.InjectQueue)(search_alerts_processor_1.SEARCH_ALERTS_QUEUE)),
    __param(3, (0, bullmq_1.InjectQueue)(trips_constants_1.BOOKINGS_QUEUE)),
    __param(4, (0, common_1.Inject)((0, common_1.forwardRef)(() => bookings_service_1.BookingsService))),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        pricing_service_1.PricingService,
        bullmq_2.Queue,
        bullmq_2.Queue,
        bookings_service_1.BookingsService,
        notifications_service_1.NotificationsService])
], TripsService);
//# sourceMappingURL=trips.service.js.map