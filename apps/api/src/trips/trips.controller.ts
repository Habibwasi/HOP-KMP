import {
  Controller,
  Post,
  Get,
  Patch,
  Body,
  Param,
  Query,
  UseGuards,
  Req,
} from '@nestjs/common'
import { SupabaseGuard } from '../auth/supabase.guard'
import { TripsService } from './trips.service'
import { CreateTripDto } from './dto/create-trip.dto'
import { SearchTripsDto } from './dto/search-trips.dto'

@Controller('trips')
export class TripsController {
  constructor(private trips: TripsService) {}

  @Post()
  @UseGuards(SupabaseGuard)
  create(@Req() req: any, @Body() dto: CreateTripDto) {
    return this.trips.create(req.user.id, dto)
  }

  @Get('search')
  search(@Query() dto: SearchTripsDto) {
    return this.trips.search(dto)
  }

  @Get('me/driver')
  @UseGuards(SupabaseGuard)
  myTripsAsDriver(@Req() req: any) {
    return this.trips.findByDriver(req.user.id)
  }

  @Get('me/passenger')
  @UseGuards(SupabaseGuard)
  myTripsAsPassenger(@Req() req: any) {
    return this.trips.findByPassenger(req.user.id)
  }

  @Get('my')
  @UseGuards(SupabaseGuard)
  myTrips(@Req() req: any) {
    return this.trips.findByDriver(req.user.id)
  }

  @Get(':id/bookings')
  @UseGuards(SupabaseGuard)
  getTripPassengers(@Param('id') id: string, @Req() req: any) {
    return this.trips.getTripPassengers(id, req.user.id)
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.trips.findById(id)
  }

  @Patch(':id/cancel')
  @UseGuards(SupabaseGuard)
  cancel(@Param('id') id: string, @Req() req: any) {
    return this.trips.cancel(id, req.user.id)
  }

  @Patch(':id/complete')
  @UseGuards(SupabaseGuard)
  complete(@Param('id') id: string, @Req() req: any) {
    return this.trips.complete(id, req.user.id)
  }
}
