import { Module, forwardRef } from '@nestjs/common'
import { ConfigModule } from '@nestjs/config'
import { PaymentsService } from './payments.service'
import { PaymentsController } from './payments.controller'
import { BookingsModule } from '../bookings/bookings.module'
import { PrismaModule } from '../prisma/prisma.module'

@Module({
  imports: [ConfigModule, PrismaModule, forwardRef(() => BookingsModule)],
  providers: [PaymentsService],
  controllers: [PaymentsController],
  exports: [PaymentsService],
})
export class PaymentsModule {}
