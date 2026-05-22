import { Controller, Post, Get, Patch, Body, Param, UseGuards, Req, HttpCode, ForbiddenException } from '@nestjs/common'
import { IsInt, IsOptional, IsString, Max, MaxLength, Min } from 'class-validator'
import { SupabaseGuard } from '../auth/supabase.guard'
import { BookingsService } from './bookings.service'
import { RatingsService } from '../ratings/ratings.service'
import { CreateBookingDto } from './dto/create-booking.dto'

class RateBookingDto {
  @IsInt()
  @Min(1)
  @Max(5)
  stars: number

  @IsOptional()
  @IsString()
  @MaxLength(500)
  comment?: string
}

@Controller('bookings')
@UseGuards(SupabaseGuard)
export class BookingsController {
  constructor(
    private bookings: BookingsService,
    private ratings: RatingsService,
  ) {}

  @Post()
  create(@Req() req: any, @Body() dto: CreateBookingDto) {
    return this.bookings.create(req.user.id, dto)
  }

  @Get('my')
  myBookings(@Req() req: any) {
    return this.bookings.findByPassenger(req.user.id)
  }

  @Get('me/active')
  activeForMe(@Req() req: any) {
    return this.bookings.findActiveForPassenger(req.user.id)
  }

  @Get('my-chats')
  myChats(@Req() req: any) {
    return this.bookings.findMyChats(req.user.id)
  }

  @Get(':id')
  findOne(@Param('id') id: string, @Req() req: any) {
    return this.bookings.findByIdAuthorized(id, req.user.id)
  }

  @Patch(':id/cancel')
  @HttpCode(200)
  cancel(@Param('id') id: string, @Req() req: any) {
    return this.bookings.cancel(id, req.user.id)
  }

  @Patch(':id/confirm')
  @HttpCode(200)
  confirm(@Param('id') id: string, @Req() req: any) {
    return this.bookings.confirm(id, req.user.id)
  }

  @Post(':id/rate')
  @HttpCode(201)
  async rate(
    @Param('id') bookingId: string,
    @Req() req: any,
    @Body() body: RateBookingDto,
  ) {
    const booking = await this.bookings.findById(bookingId)
    const raterId: string = req.user.id
    // Passenger rates the driver; driver rates the passenger
    const rateeId: string =
      raterId === booking.passengerId ? booking.trip.driverId : booking.passengerId
    return this.ratings.create(raterId, {
      rateeId,
      tripId: booking.tripId,
      score: body.stars,
      comment: body.comment,
    })
  }
}
