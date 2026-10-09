import { Module } from '@nestjs/common'
import { PrismaModule } from '../prisma/prisma.module'
import { MailModule } from '../mail/mail.module'
import { WaitlistService } from './waitlist.service'
import { WaitlistController } from './waitlist.controller'

@Module({
  imports: [PrismaModule, MailModule],
  providers: [WaitlistService],
  controllers: [WaitlistController],
})
export class WaitlistModule {}
