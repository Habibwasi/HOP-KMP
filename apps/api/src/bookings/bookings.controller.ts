import { Controller, Post, Get, Patch, Body, Param, UseGuards, Req, HttpCode } from '@nestjs/common'
import { AuthGuard } from '@nestjs/passport'
import { BookingsService } from './bookings.service'
import { CreateBookingDto } from './dto/create-booking.dto'

@Controller('bookings')
@UseGuards(AuthGuard('jwt'))
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
