import {
  Controller, Get, Post, Patch, Body, Param,
  UseGuards, Query, ParseIntPipe,
} from '@nestjs/common'
import { SupabaseGuard } from '../auth/supabase.guard'
import { AdminGuard } from './guards/admin.guard'
import { AdminService } from './admin.service'
import { IsInt, Min, Max, IsIn, IsOptional } from 'class-validator'
import { Type } from 'class-transformer'
import { Req } from '@nestjs/common'

class BanUserDto {
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(365)
  durationDays?: number

  @IsOptional()
  @IsIn(['permanent'])
  permanent?: 'permanent'
}

@Controller('admin')
@UseGuards(SupabaseGuard, AdminGuard)
export class AdminController {
  constructor(private admin: AdminService) {}

  @Get('stats')
  stats() {
    return this.admin.getDashboardStats()
  }

  @Get('users')
  users(@Query('page') page = 1, @Query('limit') limit = 20) {
    return this.admin.listUsers(+page, +limit)
  }

  @Patch('users/:id/ban')
  ban(@Param('id') id: string, @Body() dto: BanUserDto) {
    const duration = dto.permanent === 'permanent' ? 'permanent' : (dto.durationDays ?? 7)
    return this.admin.banUser(id, duration)
  }

  @Patch('users/:id/unban')
  unban(@Param('id') id: string) {
    return this.admin.unbanUser(id)
  }

  @Patch('users/:id/make-admin')
  makeAdmin(@Param('id') id: string) {
    return this.admin.makeAdmin(id)
  }

  @Get('licences/pending')
  pendingLicences() {
    return this.admin.getPendingLicences()
  }

  @Post('licences/:id/review')
  reviewLicence(
    @Req() req: any,
    @Param('id') id: string,
    @Body('approved') approved: boolean,
  ) {
    return this.admin.reviewLicence(id, req.user.id, approved)
  }

  @Get('trips')
  trips(@Query('page') page = 1, @Query('limit') limit = 20) {
    return this.admin.listTrips(+page, +limit)
  }
}
