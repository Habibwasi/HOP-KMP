import { Controller, Post, Delete, Get, Param, Body, UseGuards, Req, HttpCode, HttpStatus } from '@nestjs/common'
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
  remove(@Req() req: any, @Body('token') token: string) {
    return this.notifications.removeToken(token, req.user.id)
  }

  @Get()
  getAll(@Req() req: any) {
    return this.notifications.getForUser(req.user.id)
  }

  @Get('unread-count')
  unreadCount(@Req() req: any) {
    return this.notifications.unreadCount(req.user.id)
  }

  @Post(':id/read')
  @HttpCode(HttpStatus.OK)
  markRead(@Req() req: any, @Param('id') id: string) {
    return this.notifications.markRead(id, req.user.id)
  }
}
