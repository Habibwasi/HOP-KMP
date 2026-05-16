import { Injectable, Logger } from '@nestjs/common'
import { ConfigService } from '@nestjs/config'
import { PrismaService } from '../prisma/prisma.service'
import { existsSync } from 'node:fs'
import * as apn from 'apn'
import * as admin from 'firebase-admin'

@Injectable()
export class NotificationsService {
  private readonly logger = new Logger(NotificationsService.name)
  private apnProvider: apn.Provider | null = null
  private fcmInitialised = false

  constructor(
    private prisma: PrismaService,
    private config: ConfigService,
  ) {
    this.initApn()
    this.initFcm()
  }

  private initApn() {
    const keyPath = this.config.get<string>('APNS_KEY_PATH')
    const keyId = this.config.get<string>('APNS_KEY_ID')
    const teamId = this.config.get<string>('APNS_TEAM_ID')

    if (!keyPath || !keyId || !teamId || !existsSync(keyPath)) {
      this.logger.warn('APNs not initialised - credentials are missing in this environment')
      return
    }

    try {
      this.apnProvider = new apn.Provider({
        token: {
          key: keyPath,
          keyId,
          teamId,
        },
        production: this.config.get('NODE_ENV') === 'production',
      })
    } catch (e) {
      this.logger.error(`APNs not initialised - ${(e as Error).message}`)
    }
  }

  private initFcm() {
    const projectId = this.config.get<string>('FIREBASE_PROJECT_ID')
    const clientEmail = this.config.get<string>('FIREBASE_CLIENT_EMAIL')
    const privateKey = this.config.get<string>('FIREBASE_PRIVATE_KEY')

    if (!projectId || !clientEmail || !privateKey || privateKey === 'your_private_key') {
      this.logger.warn('FCM not initialised - credentials are missing in this environment')
      return
    }

    try {
      if (!admin.apps.length) {
        admin.initializeApp({
          credential: admin.credential.cert({
            projectId,
            clientEmail,
            privateKey: privateKey.replace(/\\n/g, '\n'),
          }),
        })
      }
      this.fcmInitialised = true
    } catch (e) {
      this.logger.error(`FCM not initialised - ${(e as Error).message}`)
    }
  }

  async sendToUser(
    userId: string,
    title: string,
    body: string,
    data?: Record<string, string>,
  ) {
    const tokens = await this.prisma.pushToken.findMany({ where: { userId } })
    if (!tokens.length) return

    const ios = tokens.filter((t) => t.platform === 'ios')
    const android = tokens.filter((t) => t.platform === 'android')

    await Promise.allSettled([
      ...ios.map((t) => this.sendApns(t.token, title, body, data)),
      ...android.map((t) => this.sendFcm(t.token, title, body, data)),
    ])
  }

  private async sendApns(
    token: string,
    title: string,
    body: string,
    data?: Record<string, string>,
  ) {
    if (!this.apnProvider) return
    const note = new apn.Notification()
    note.alert = { title, body }
    note.topic = this.config.getOrThrow('APNS_BUNDLE_ID')
    note.sound = 'default'
    note.payload = data ?? {}
    const result = await this.apnProvider.send(note, token)
    if (result.failed.length) {
      this.logger.error(`APNs failed: ${JSON.stringify(result.failed)}`)
      // Prune unregistered device tokens
      const unregistered = result.failed
        .filter((f) => f.response?.reason === 'Unregistered' || f.response?.reason === 'BadDeviceToken')
        .map((f) => f.device)
      if (unregistered.length) {
        await this.prisma.pushToken.deleteMany({ where: { token: { in: unregistered } } })
      }
    }
  }

  private async sendFcm(
    token: string,
    title: string,
    body: string,
    data?: Record<string, string>,
  ) {
    if (!this.fcmInitialised) return
    try {
      await admin.messaging().send({
        token,
        notification: { title, body },
        data: data ?? {},
        android: { priority: 'high' },
      })
    } catch (e: any) {
      this.logger.error(`FCM failed for token ${token}: ${e?.message ?? e}`)
      // Prune unregistered tokens
      if (e?.code === 'messaging/registration-token-not-registered' ||
          e?.code === 'messaging/invalid-registration-token') {
        await this.prisma.pushToken.deleteMany({ where: { token } })
      }
    }
  }

  async registerToken(userId: string, token: string, platform: 'ios' | 'android') {
    return this.prisma.pushToken.upsert({
      where: { token },
      create: { userId, token, platform },
      update: { userId },
    })
  }

  async removeToken(token: string) {
    return this.prisma.pushToken.deleteMany({ where: { token } })
  }

  async getForUser(userId: string) {
    return this.prisma.notification.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      take: 50,
    })
  }

  async unreadCount(userId: string): Promise<{ count: number }> {
    const count = await this.prisma.notification.count({
      where: { userId, isRead: false },
    })
    return { count }
  }

  async markRead(notificationId: string, userId: string) {
    return this.prisma.notification.updateMany({
      where: { id: notificationId, userId },
      data: { isRead: true },
    })
  }
}
