import {
  Injectable,
  NotFoundException,
  ForbiddenException,
  BadRequestException,
} from '@nestjs/common'
import { PrismaService } from '../prisma/prisma.service'
import { BookingStatus } from '@prisma/client'
import { NotificationsService } from '../notifications/notifications.service'

@Injectable()
export class SettlementsService {
  constructor(
    private prisma: PrismaService,
    private notifications: NotificationsService,
  ) {}

  private async assertParty(bookingId: string, userId: string) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      include: { trip: { select: { driverId: true } } },
    })
    if (!booking) throw new NotFoundException('Booking not found')
    const isPassenger = booking.passengerId === userId
    const isDriver = booking.trip.driverId === userId
    if (!isPassenger && !isDriver) throw new ForbiddenException('Not a party to this booking')
    return { booking, isPassenger, isDriver }
  }

  async getSettlement(bookingId: string, userId: string) {
    await this.assertParty(bookingId, userId)
    const settlement = await this.prisma.rideSettlement.findUnique({ where: { bookingId } })
    if (!settlement) throw new NotFoundException('Settlement not available yet')
    const booking = await this.prisma.booking.findUnique({ where: { id: bookingId }, select: { tripId: true } })
    return { ...settlement, tripId: booking?.tripId ?? null }
  }

  async markPassengerPaid(bookingId: string, userId: string) {
    const { booking, isPassenger } = await this.assertParty(bookingId, userId)
    if (!isPassenger) throw new ForbiddenException('Only the passenger can mark as paid')
    if (booking.status !== BookingStatus.AWAITING_PAYMENT) {
      throw new BadRequestException('Booking is not awaiting payment')
    }

    const settlement = await this.prisma.rideSettlement.findUnique({ where: { bookingId } })
    if (!settlement) throw new NotFoundException('Settlement not found')
    if (settlement.passengerPaidAt) throw new BadRequestException('Already marked as paid')

    const updated = await this.prisma.rideSettlement.update({
      where: { bookingId },
      data: { passengerPaidAt: new Date() },
    })

    // Notify driver that passenger has marked as paid
    await this.notifications.sendToUser(
      booking.trip.driverId,
      'Passenger marked payment sent',
      'The passenger has indicated they sent payment via MobilePay. Please confirm receipt.',
      { type: 'PAYMENT_MARKED_PAID', bookingId },
    ).catch(() => {/* non-fatal */})
    await this.prisma.notification.create({
      data: {
        userId: booking.trip.driverId,
        type: 'PAYMENT_MARKED_PAID',
        title: 'Passenger marked payment sent',
        body: 'The passenger has indicated they sent payment via MobilePay. Please confirm receipt.',
        deepLinkId: bookingId,
      },
    })

    return updated
  }

  async markDriverConfirmed(bookingId: string, userId: string) {
    const { booking, isDriver } = await this.assertParty(bookingId, userId)
    if (!isDriver) throw new ForbiddenException('Only the driver can confirm receipt')
    if (booking.status !== BookingStatus.AWAITING_PAYMENT) {
      throw new BadRequestException('Booking is not awaiting payment')
    }

    const settlement = await this.prisma.rideSettlement.findUnique({ where: { bookingId } })
    if (!settlement) throw new NotFoundException('Settlement not found')
    if (!settlement.passengerPaidAt) {
      throw new BadRequestException('Passenger has not yet marked payment as sent')
    }
    if (settlement.driverConfirmedAt) throw new BadRequestException('Already confirmed')

    const [updatedSettlement] = await this.prisma.$transaction([
      this.prisma.rideSettlement.update({
        where: { bookingId },
        data: { driverConfirmedAt: new Date() },
      }),
      this.prisma.booking.update({
        where: { id: bookingId },
        data: { status: BookingStatus.COMPLETED },
      }),
    ])

    // Notify passenger
    await this.notifications.sendToUser(
      booking.passengerId,
      'Payment confirmed',
      'Your driver confirmed receipt of payment. Thanks for riding with Ridly!',
      { type: 'PAYMENT_CONFIRMED', bookingId },
    ).catch(() => {/* non-fatal */})
    await this.prisma.notification.create({
      data: {
        userId: booking.passengerId,
        type: 'PAYMENT_CONFIRMED',
        title: 'Payment confirmed',
        body: 'Your driver confirmed receipt of payment. Thanks for riding with Ridly!',
        deepLinkId: bookingId,
      },
    })

    return updatedSettlement
  }

  async unmarkPaid(bookingId: string, userId: string) {
    const { booking, isPassenger } = await this.assertParty(bookingId, userId)
    if (!isPassenger) throw new ForbiddenException('Only the passenger can unmark payment')

    const settlement = await this.prisma.rideSettlement.findUnique({ where: { bookingId } })
    if (!settlement) throw new NotFoundException('Settlement not found')
    if (!settlement.passengerPaidAt) throw new BadRequestException('Payment has not been marked')
    if (settlement.driverConfirmedAt) throw new BadRequestException('Driver has already confirmed — cannot undo')

    return this.prisma.rideSettlement.update({
      where: { bookingId },
      data: { passengerPaidAt: null },
    })
  }

  async getSettlementsForTrip(tripId: string, userId: string) {
    const trip = await this.prisma.trip.findUnique({
      where: { id: tripId },
      select: { driverId: true },
    })
    if (!trip) throw new NotFoundException('Trip not found')
    if (trip.driverId !== userId) throw new ForbiddenException('Only the driver can view trip settlements')

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
    })

    return bookings.map((b) => ({
      bookingId: b.id,
      passengerFirstName: b.passenger.firstName,
      passengerLastName: b.passenger.lastName,
      suggestedAmountOere: b.settlement?.suggestedAmountOere ?? 0,
      passengerPaidAt: b.settlement?.passengerPaidAt ?? null,
      driverConfirmedAt: b.settlement?.driverConfirmedAt ?? null,
      bookingStatus: b.status,
    }))
  }

  async dispute(bookingId: string, userId: string, reason: string) {
    const { booking } = await this.assertParty(bookingId, userId)
    if (booking.status !== BookingStatus.AWAITING_PAYMENT) {
      throw new BadRequestException('Booking is not awaiting payment')
    }

    const settlement = await this.prisma.rideSettlement.findUnique({ where: { bookingId } })
    if (!settlement) throw new NotFoundException('Settlement not found')
    if (settlement.disputedAt) throw new BadRequestException('Already disputed')

    const [updatedSettlement] = await this.prisma.$transaction([
      this.prisma.rideSettlement.update({
        where: { bookingId },
        data: { disputedAt: new Date(), disputeReason: reason },
      }),
      this.prisma.booking.update({
        where: { id: bookingId },
        data: { status: BookingStatus.DISPUTED },
      }),
    ])

    return updatedSettlement
  }
}
