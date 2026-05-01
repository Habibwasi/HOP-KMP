import { Body, Controller, Delete, Get, Param, Patch, Post, Query, Req, UseGuards } from '@nestjs/common'
import { IsEnum, IsNumber, IsOptional, IsString, MaxLength, MinLength } from 'class-validator'
import { SupabaseGuard } from '../auth/supabase.guard'
import { PlacesService } from './places.service'

export class UpsertPlaceDto {
  @IsString()
  @MinLength(1)
  @MaxLength(40)
  label: string

  @IsString()
  @MinLength(1)
  @MaxLength(200)
  address: string

  @IsOptional()
  @IsNumber()
  lat?: number

  @IsOptional()
  @IsNumber()
  lng?: number

  @IsOptional()
  @IsEnum(['HOME', 'WORK', 'CUSTOM'])
  kind?: 'HOME' | 'WORK' | 'CUSTOM'
}

@Controller('places')
@UseGuards(SupabaseGuard)
export class PlacesController {
  constructor(private readonly places: PlacesService) {}

  @Get()
  list(@Req() req: any) {
    return this.places.listForUser(req.user.id)
  }

  /** GET /places/geocode?address=... — resolve a free-text address to lat/lng. */
  @Get('geocode')
  geocode(@Query('address') address: string) {
    return this.places.geocode(address)
  }

  @Post()
  create(@Req() req: any, @Body() dto: UpsertPlaceDto) {
    return this.places.create(req.user.id, dto)
  }

  @Patch(':id')
  update(@Req() req: any, @Param('id') id: string, @Body() dto: UpsertPlaceDto) {
    return this.places.update(req.user.id, id, dto)
  }

  @Delete(':id')
  remove(@Req() req: any, @Param('id') id: string) {
    return this.places.remove(req.user.id, id)
  }
}
