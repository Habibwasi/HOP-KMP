import { Module } from '@nestjs/common'
import { ConfigModule } from '@nestjs/config'
import { BullModule } from '@nestjs/bullmq'
import { PrismaModule } from './prisma/prisma.module'
import { AuthModule } from './auth/auth.module'
import { UsersModule } from './users/users.module'
import { TripsModule } from './trips/trips.module'
import { BookingsModule } from './bookings/bookings.module'
import { PaymentsModule } from './payments/payments.module'
import { RatingsModule } from './ratings/ratings.module'
import { NotificationsModule } from './notifications/notifications.module'
import { TaxModule } from './tax/tax.module'
import { AdminModule } from './admin/admin.module'
import { HealthController } from './health.controller'

@Module({
  controllers: [HealthController],
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    BullModule.forRoot({
      connection: {
        host: process.env.REDIS_HOST ?? 'localhost',
        port: parseInt(process.env.REDIS_PORT ?? '6379'),
      },
    }),
    PrismaModule,
    AuthModule,
    UsersModule,
    TripsModule,
    BookingsModule,
    PaymentsModule,
    RatingsModule,
    NotificationsModule,
    TaxModule,
    AdminModule,
  ],
})
export class AppModule {}
