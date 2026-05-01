import {
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common'
import { PrismaService } from '../prisma/prisma.service'
import { NotificationsService } from '../notifications/notifications.service'
import { CreateSearchAlertDto } from './dto/create-search-alert.dto'

@Injectable()
export class SearchAlertsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly notifications: NotificationsService,
  ) {}

  async create(userId: string, dto: CreateSearchAlertDto) {
    const existing = await this.prisma.searchAlert.findFirst({
      where: {
        userId,
        origin: { equals: dto.origin, mode: 'insensitive' },
        dest: { equals: dto.dest, mode: 'insensitive' },
        isActive: true,
      },
    })
    if (existing) throw new ConflictException('Alert already exists for this route')

    return this.prisma.searchAlert.create({
      data: {
        userId,
        origin: dto.origin.trim(),
        dest: dto.dest.trim(),
        seats: dto.seats ?? 1,
      },
    })
  }

  list(userId: string) {
    return this.prisma.searchAlert.findMany({
      where: { userId, isActive: true },
      orderBy: { createdAt: 'desc' },
    })
  }

  async remove(userId: string, id: string) {
    const alert = await this.prisma.searchAlert.findUnique({ where: { id } })
    if (!alert) throw new NotFoundException('Alert not found')
    if (alert.userId !== userId) throw new ForbiddenException()
    await this.prisma.searchAlert.update({ where: { id }, data: { isActive: false } })
    return { ok: true }
  }

  /**
   * Called by the BullMQ processor when a new trip is created.
   * Finds active alerts whose origin/dest text matches the new trip,
   * sends a push notification to each matched user, and persists a
   * Notification row so it appears in the in-app list.
   */
  async matchAndNotify(trip: {
    id: string
    originAddress: string
    destAddress: string
  }) {
    const alerts = await this.prisma.searchAlert.findMany({
      where: {
        isActive: true,
        origin: { contains: trip.originAddress.split(',')[0].trim(), mode: 'insensitive' },
        dest: { contains: trip.destAddress.split(',')[0].trim(), mode: 'insensitive' },
      },
    })

    if (!alerts.length) return

    const title = 'New ride available!'
    const body = `A ride from ${trip.originAddress} to ${trip.destAddress} just appeared.`

    await Promise.all(
      alerts.map(async (alert) => {
        await this.notifications.sendToUser(alert.userId, title, body, {
          type: 'SEARCH_ALERT',
          tripId: trip.id,
        })
        await this.prisma.notification.create({
          data: {
            userId: alert.userId,
            type: 'SEARCH_ALERT',
            title,
            body,
            deepLinkId: trip.id,
          },
        })
      }),
    )
  }
}
