import { IsInt, IsString, Max, MaxLength, Min, MinLength } from 'class-validator'

export class CreateCarDetailsDto {
  @IsString()
  @MinLength(1)
  @MaxLength(50)
  make: string

  @IsString()
  @MinLength(1)
  @MaxLength(50)
  model: string

  @IsInt()
  @Min(1900)
  @Max(new Date().getFullYear() + 1)
  year: number

  @IsString()
  @MinLength(1)
  @MaxLength(20)
  license_plate: string

  @IsString()
  @MinLength(1)
  @MaxLength(30)
  colour: string

  @IsInt()
  @Min(1)
  @Max(8)
  seats_available: number
}
