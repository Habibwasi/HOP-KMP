import { IsUUID, IsInt, Min, Max } from 'class-validator'

export class CreateBookingDto {
  @IsUUID()
  tripId: string

  @IsInt()
  @Min(1)
  @Max(8)
  seats: number
}
