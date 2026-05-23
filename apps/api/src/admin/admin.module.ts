import { Module } from '@nestjs/common'
import { AdminService } from './admin.service'
import { AdminController } from './admin.controller'
import { BanExpiryService } from './ban-expiry.service'

@Module({
  providers: [AdminService, BanExpiryService],
  controllers: [AdminController],
})
export class AdminModule {}
