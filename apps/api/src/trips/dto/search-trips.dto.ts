import { IsNumber, IsDateString, IsOptional, IsInt, Min, Max } from 'class-validator'
import { Type } from 'class-transformer'

export class SearchTripsDto {
  @Type(() => Number)
  @IsNumber()
  originLat: number

  @Type(() => Number)
  @IsNumber()
  originLng: number

  @Type(() => Number)
  @IsNumber()
  destLat: number

  @Type(() => Number)
  @IsNumber()
  destLng: number

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
