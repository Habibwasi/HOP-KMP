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
exports.SettlementsService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
const client_1 = require("@prisma/client");
const notifications_service_1 = require("../notifications/notifications.service");
let SettlementsService = class SettlementsService {
    prisma;
    notifications;
    constructor(prisma, notifications) {
        this.prisma = prisma;
        this.notifications = notifications;
    }
    async assertParty(bookingId, userId) {
        const booking = await this.prisma.booking.findUnique({
            where: { id: bookingId },
            include: { trip: { select: { driverId: true } } },
        });
        if (!booking)
            throw new common_1.NotFoundException('Booking not found');
        const isPassenger = booking.passengerId === userId;
        const isDriver = booking.trip.driverId === userId;
        if (!isPassenger && !isDriver)
            throw new common_1.ForbiddenException('Not a party to this booking');
        return { booking, isPassenger, isDriver };
    }
    async getSettlement(bookingId, userId) {
        await this.assertParty(bookingId, userId);
        const settlement = await this.prisma.rideSettlement.findUnique({ where: { bookingId } });
        if (!settlement)
            throw new common_1.NotFoundException('Settlement not available yet');
        const booking = await this.prisma.booking.findUnique({ where: { id: bookingId }, select: { tripId: true } });
        return { ...settlement, tripId: booking?.tripId ?? null };
    }
    async markPassengerPaid(bookingId, userId) {
        const { booking, isPassenger } = await this.assertParty(bookingId, userId);
        if (!isPassenger)
            throw new common_1.ForbiddenException('Only the passenger can mark as paid');
        if (booking.status !== client_1.BookingStatus.AWAITING_PAYMENT) {
            throw new common_1.BadRequestException('Booking is not awaiting payment');
        }
        const settlement = await this.prisma.rideSettlement.findUnique({ where: { bookingId } });
        if (!settlement)
            throw new common_1.NotFoundException('Settlement not found');
        if (settlement.passengerPaidAt)
            throw new common_1.BadRequestException('Already marked as paid');
        const updated = await this.prisma.rideSettlement.update({
            where: { bookingId },
            data: { passengerPaidAt: new Date() },
        });
        await this.notifications.sendToUser(booking.trip.driverId, 'Passenger marked payment sent', 'The passenger has indicated they sent payment via MobilePay. Please confirm receipt.', { type: 'PAYMENT_MARKED_PAID', bookingId }).catch(() => { });
        await this.prisma.notification.create({
            data: {
                userId: booking.trip.driverId,
                type: 'PAYMENT_MARKED_PAID',
                title: 'Passenger marked payment sent',
                body: 'The passenger has indicated they sent payment via MobilePay. Please confirm receipt.',
                deepLinkId: bookingId,
            },
        });
        return updated;
    }
    async markDriverConfirmed(bookingId, userId) {
        const { booking, isDriver } = await this.assertParty(bookingId, userId);
        if (!isDriver)
            throw new common_1.ForbiddenException('Only the driver can confirm receipt');
        if (booking.status !== client_1.BookingStatus.AWAITING_PAYMENT) {
            throw new common_1.BadRequestException('Booking is not awaiting payment');
        }
        const settlement = await this.prisma.rideSettlement.findUnique({ where: { bookingId } });
        if (!settlement)
            throw new common_1.NotFoundException('Settlement not found');
        if (!settlement.passengerPaidAt) {
            throw new common_1.BadRequestException('Passenger has not yet marked payment as sent');
        }
        if (settlement.driverConfirmedAt)
            throw new common_1.BadRequestException('Already confirmed');
        const [updatedSettlement] = await this.prisma.$transaction([
            this.prisma.rideSettlement.update({
                where: { bookingId },
                data: { driverConfirmedAt: new Date() },
            }),
            this.prisma.booking.update({
                where: { id: bookingId },
                data: { status: client_1.BookingStatus.COMPLETED },
            }),
        ]);
        await this.notifications.sendToUser(booking.passengerId, 'Payment confirmed', 'Your driver confirmed receipt of payment. Thanks for riding with Ridly!', { type: 'PAYMENT_CONFIRMED', bookingId }).catch(() => { });
        await this.prisma.notification.create({
            data: {
                userId: booking.passengerId,
                type: 'PAYMENT_CONFIRMED',
                title: 'Payment confirmed',
                body: 'Your driver confirmed receipt of payment. Thanks for riding with Ridly!',
                deepLinkId: bookingId,
            },
        });
        return updatedSettlement;
    }
    async unmarkPaid(bookingId, userId) {
        const { booking, isPassenger } = await this.assertParty(bookingId, userId);
        if (!isPassenger)
            throw new common_1.ForbiddenException('Only the passenger can unmark payment');
        const settlement = await this.prisma.rideSettlement.findUnique({ where: { bookingId } });
        if (!settlement)
            throw new common_1.NotFoundException('Settlement not found');
        if (!settlement.passengerPaidAt)
            throw new common_1.BadRequestException('Payment has not been marked');
        if (settlement.driverConfirmedAt)
            throw new common_1.BadRequestException('Driver has already confirmed — cannot undo');
        return this.prisma.rideSettlement.update({
            where: { bookingId },
            data: { passengerPaidAt: null },
        });
    }
    async getSettlementsForTrip(tripId, userId) {
        const trip = await this.prisma.trip.findUnique({
            where: { id: tripId },
            select: { driverId: true },
        });
        if (!trip)
            throw new common_1.NotFoundException('Trip not found');
        if (trip.driverId !== userId)
            throw new common_1.ForbiddenException('Only the driver can view trip settlements');
        const bookings = await this.prisma.booking.findMany({
            where: { tripId, status: { not: 'CANCELLED' } },
            include: {
                passenger: { select: { firstName: true, lastName: true } },
                settlement: {
                    select: {
                        passengerPaidAt: true,
                        driverConfirmedAt: true,
                        suggestedAmountOere: true,
                    },
                },
            },
            orderBy: { createdAt: 'asc' },
        });
        return bookings.map((b) => ({
            bookingId: b.id,
            passengerFirstName: b.passenger.firstName,
            passengerLastName: b.passenger.lastName,
            suggestedAmountOere: b.settlement?.suggestedAmountOere ?? 0,
            passengerPaidAt: b.settlement?.passengerPaidAt ?? null,
            driverConfirmedAt: b.settlement?.driverConfirmedAt ?? null,
            bookingStatus: b.status,
        }));
    }
    async dispute(bookingId, userId, reason) {
        const { booking, isPassenger } = await this.assertParty(bookingId, userId);
        if (booking.status !== client_1.BookingStatus.AWAITING_PAYMENT) {
            throw new common_1.BadRequestException('Booking is not awaiting payment');
        }
        const settlement = await this.prisma.rideSettlement.findUnique({ where: { bookingId } });
        if (!settlement)
            throw new common_1.NotFoundException('Settlement not found');
        if (settlement.disputedAt)
            throw new common_1.BadRequestException('Already disputed');
        const [updatedSettlement] = await this.prisma.$transaction([
            this.prisma.rideSettlement.update({
                where: { bookingId },
                data: { disputedAt: new Date(), disputeReason: reason },
            }),
            this.prisma.booking.update({
                where: { id: bookingId },
                data: { status: client_1.BookingStatus.DISPUTED },
            }),
        ]);
        const notifyUserId = isPassenger ? booking.trip.driverId : booking.passengerId;
        const title = 'Payment dispute raised';
        const body = isPassenger
            ? 'The passenger has raised a dispute for this trip payment.'
            : 'The driver has raised a dispute for this trip payment.';
        const recipientRole = isPassenger ? 'driver' : 'passenger';
        await this.notifications.sendToUser(notifyUserId, title, body, {
            type: 'PAYMENT_DISPUTED',
            bookingId,
            recipientRole,
        }).catch(() => { });
        await this.prisma.notification.create({
            data: {
                userId: notifyUserId,
                type: 'PAYMENT_DISPUTED',
                title,
                body,
                deepLinkId: bookingId,
            },
        });
        return updatedSettlement;
    }
};
exports.SettlementsService = SettlementsService;
exports.SettlementsService = SettlementsService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        notifications_service_1.NotificationsService])
], SettlementsService);
//# sourceMappingURL=settlements.service.js.map