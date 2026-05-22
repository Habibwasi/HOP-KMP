import { Injectable } from '@nestjs/common'
import { PrismaService } from '../prisma/prisma.service'
import { AppException } from '../common/errors/app-exception'
import { ApiErrorCode } from '../common/errors/api-error-codes'

@Injectable()
export class ChatService {
  constructor(private prisma: PrismaService) {}

  /** Validate that userId is either the passenger or the driver of bookingId. */
  async assertParticipant(bookingId: string, userId: string): Promise<void> {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      include: { trip: { select: { driverId: true } } },
    })
    if (!booking) throw new AppException(ApiErrorCode.BOOKING_NOT_FOUND)
    const isPassenger = booking.passengerId === userId
    const isDriver = booking.trip.driverId === userId
    if (!isPassenger && !isDriver) throw new AppException(ApiErrorCode.NOT_A_PARTICIPANT)
  }

  /** Persist a chat message and return it for broadcasting. */
  async createMessage(bookingId: string, senderId: string, senderName: string, body: string) {
    return this.prisma.chatMessage.create({
      data: { bookingId, senderId, body },
      select: {
        id: true,
        bookingId: true,
        senderId: true,
        body: true,
        createdAt: true,
      },
    }).then((msg) => ({ ...msg, senderName, timestampMs: msg.createdAt.getTime() }))
  }

  /** Return the last 50 messages for a booking, oldest first. */
  async getHistory(bookingId: string, requesterId: string) {
    await this.assertParticipant(bookingId, requesterId)
    const messages = await this.prisma.chatMessage.findMany({
      where: { bookingId },
      orderBy: { createdAt: 'asc' },
      take: 50,
      include: {
        sender: { select: { firstName: true, lastName: true } },
      },
    })
    return messages.map((m) => ({
      id: m.id,
      bookingId: m.bookingId,
      senderId: m.senderId,
      senderName: `${m.sender.firstName} ${m.sender.lastName}`.trim(),
      body: m.body,
      timestampMs: m.createdAt.getTime(),
    }))
  }
}
