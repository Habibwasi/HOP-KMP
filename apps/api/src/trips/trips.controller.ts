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
import { AuthGuard } from '@nestjs/passport'
import { TripsService } from './trips.service'
import { CreateTripDto } from './dto/create-trip.dto'
import { SearchTripsDto } from './dto/search-trips.dto'

@Controller('trips')
export class TripsController {
  constructor(private trips: TripsService) {}

  @Post()
  @UseGuards(AuthGuard('jwt'))
  create(@Req() req: any, @Body() dto: CreateTripDto) {
    return this.trips.create(req.user.id, dto)
  }

  @Get('search')
  search(@Query() dto: SearchTripsDto) {
    return this.trips.search(dto)
  }

  @Get('my')
  @UseGuards(AuthGuard('jwt'))
  myTrips(@Req() req: any) {
    return this.trips.findByDriver(req.user.id)
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.trips.findById(id)
  }

  @Patch(':id/cancel')
  @UseGuards(AuthGuard('jwt'))
  cancel(@Param('id') id: string, @Req() req: any) {
    return this.trips.cancel(id, req.user.id)
  }
}
