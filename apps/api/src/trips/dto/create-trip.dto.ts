import {
  IsEnum,
  IsNumber,
  IsString,
  IsDateString,
  IsInt,
  Min,
  Max,
  IsOptional,
  IsArray,
  ArrayMinSize,
} from 'class-validator'
import { TripModel } from '@prisma/client'

export class CreateTripDto {
  @IsEnum(TripModel)
  model: TripModel

  @IsNumber()
  originLat: number

  @IsNumber()
  originLng: number

  @IsString()
  originAddress: string

  @IsNumber()
  destLat: number

  @IsNumber()
  destLng: number

  @IsString()
  destAddress: string

  @IsOptional()
  @IsInt()
  @Min(1)
  distanceMetres?: number

  @IsDateString()
  departureAt: string

  @IsInt()
  @Min(1)
  @Max(8)
  seats: number

  // Model A only
  @IsOptional()
  @IsArray()
  @ArrayMinSize(1)
  @IsString({ each: true })
  recurringDays?: string[]

  // Model A only — rolling window in days; defaults to 30 if omitted
  @IsOptional()
  @IsInt()
  @Min(7)
  @Max(90)
  windowDays?: number

  // Model B only
  @IsOptional()
  @IsInt()
  @Min(1)
  minPassengers?: number

  @IsOptional()
  @IsDateString()
  thresholdDeadline?: string
}
