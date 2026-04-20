import { IsUUID, IsInt, Min, Max, IsEnum } from 'class-validator'
import { PaymentProvider } from '@prisma/client'

export class CreateBookingDto {
  @IsUUID()
  tripId: string

  @IsInt()
  @Min(1)
  @Max(8)
  seats: number

  @IsEnum(PaymentProvider)
  paymentProvider: PaymentProvider
}
