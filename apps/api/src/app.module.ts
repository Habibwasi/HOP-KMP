import { Module } from '@nestjs/common'
import { SentryModule } from '@sentry/nestjs/setup'
import { ConfigModule } from '@nestjs/config'
import { BullModule } from '@nestjs/bullmq'
import { ScheduleModule } from '@nestjs/schedule'
import Redis from 'ioredis'
import { PrismaModule } from './prisma/prisma.module'
import { AuthModule } from './auth/auth.module'
import { UsersModule } from './users/users.module'
import { TripsModule } from './trips/trips.module'
import { BookingsModule } from './bookings/bookings.module'
import { SettlementsModule } from './settlements/settlements.module'
import { RatingsModule } from './ratings/ratings.module'
import { NotificationsModule } from './notifications/notifications.module'
import { AdminModule } from './admin/admin.module'
import { PlacesModule } from './places/places.module'
import { SearchHistoryModule } from './search-history/search-history.module'
import { AggregatesModule } from './aggregates/aggregates.module'
import { ChatModule } from './chat/chat.module'
import { HealthController } from './health.controller'

@Module({
  controllers: [HealthController],
  imports: [
    SentryModule.forRoot(),
    ConfigModule.forRoot({ isGlobal: true }),
    ScheduleModule.forRoot(),
    BullModule.forRootAsync({
      useFactory: () => {
        const url = process.env.REDIS_URL
        const host = process.env.REDIS_HOST
        const port = process.env.REDIS_PORT
        const password = process.env.REDIS_PASSWORD
        if (!url && !host) {
          throw new Error('Neither REDIS_URL nor REDIS_HOST is set — check Railway Redis plugin is linked to this service')
        }
        const redisUrl = url ?? `redis://${host}:${port ?? '6379'}`
        return {
          connection: new Redis(redisUrl, {
            password: !url && password ? password : undefined,
            maxRetriesPerRequest: null,
            enableReadyCheck: false,
            retryStrategy: (times) => Math.min(times * 500, 5000),
          }),
        }
      },
    }),
    PrismaModule,
    AuthModule,
    UsersModule,
    TripsModule,
    BookingsModule,
    SettlementsModule,
    RatingsModule,
    NotificationsModule,
    AdminModule,
    PlacesModule,
    SearchHistoryModule,
    AggregatesModule,
    ChatModule,
  ],
})
export class AppModule {}
