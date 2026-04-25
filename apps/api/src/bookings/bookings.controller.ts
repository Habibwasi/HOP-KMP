import { Controller, Post, Get, Patch, Body, Param, UseGuards, Req, HttpCode } from '@nestjs/common'
import { SupabaseGuard } from '../auth/supabase.guard'
import { BookingsService } from './bookings.service'
import { CreateBookingDto } from './dto/create-booking.dto'

@Controller('bookings')
@UseGuards(SupabaseGuard)
export class BookingsController {
  constructor(private bookings: BookingsService) {}

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

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.bookings.findById(id)
  }

  @Patch(':id/cancel')
  @HttpCode(200)
  cancel(@Param('id') id: string, @Req() req: any) {
    return this.bookings.cancel(id, req.user.id)
  }
}
