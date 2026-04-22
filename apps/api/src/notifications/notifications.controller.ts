import { Controller, Post, Delete, Body, UseGuards, Req } from '@nestjs/common'
import { SupabaseGuard } from '../auth/supabase.guard'
import { NotificationsService } from './notifications.service'
import { IsString, IsEnum } from 'class-validator'

class RegisterTokenDto {
  @IsString()
  token: string

  @IsEnum(['ios', 'android'])
  platform: 'ios' | 'android'
}

@Controller('notifications')
@UseGuards(SupabaseGuard)
export class NotificationsController {
  constructor(private notifications: NotificationsService) {}

  @Post('token')
  register(@Req() req: any, @Body() dto: RegisterTokenDto) {
    return this.notifications.registerToken(req.user.id, dto.token, dto.platform)
  }

  @Delete('token')
  remove(@Body('token') token: string) {
    return this.notifications.removeToken(token)
  }
}
