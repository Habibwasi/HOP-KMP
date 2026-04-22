import { Controller, Post, Get, Body, Param, UseGuards, Req } from '@nestjs/common'
import { SupabaseGuard } from '../auth/supabase.guard'
import { RatingsService } from './ratings.service'
import { CreateRatingDto } from './dto/create-rating.dto'

@Controller('ratings')
export class RatingsController {
  constructor(private ratings: RatingsService) {}

  @Post()
  @UseGuards(SupabaseGuard)
  create(@Req() req: any, @Body() dto: CreateRatingDto) {
    return this.ratings.create(req.user.id, dto)
  }

  @Get('user/:userId')
  getUserRatings(@Param('userId') userId: string) {
    return this.ratings.getUserRatings(userId)
  }
}
