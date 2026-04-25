import { IsNumber, IsDateString, IsOptional, IsInt, Min, Max, IsString } from 'class-validator'
import { Type } from 'class-transformer'

export class SearchTripsDto {
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  originLat?: number

  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  originLng?: number

  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  destLat?: number

  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  destLng?: number

  /** Text-based origin city/address (used when lat/lng are absent). */
  @IsOptional()
  @IsString()
  origin?: string

  /** Text-based destination city/address (used when lat/lng are absent). */
  @IsOptional()
  @IsString()
  dest?: string

  @IsDateString()
  date: string

  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(8)
  seats?: number

  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  radiusKm?: number
}
