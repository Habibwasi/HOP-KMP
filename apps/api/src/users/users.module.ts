import { Module } from '@nestjs/common'
import { UsersService } from './users.service'
import { UsersController } from './users.controller'
import { RatingsModule } from '../ratings/ratings.module'
import { NotificationsModule } from '../notifications/notifications.module'

@Module({
  imports: [RatingsModule, NotificationsModule],
  controllers: [UsersController],
  providers: [UsersService],
  exports: [UsersService],
})
export class UsersModule {}
