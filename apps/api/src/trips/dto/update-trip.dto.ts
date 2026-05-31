import {
  IsNumber,
  IsString,
  IsDateString,
  IsInt,
  Min,
  IsOptional,
} from 'class-validator'

export class UpdateTripDto {
  @IsOptional()
  @IsNumber()
  originLat?: number

  @IsOptional()
  @IsNumber()
  originLng?: number

  @IsOptional()
  @IsString()
  originAddress?: string

  @IsOptional()
  @IsNumber()
  destLat?: number

  @IsOptional()
  @IsNumber()
  destLng?: number

  @IsOptional()
  @IsString()
  destAddress?: string

  @IsOptional()
  @IsDateString()
  departureAt?: string

  @IsOptional()
  @IsInt()
  @Min(1)
  distanceMetres?: number
}
