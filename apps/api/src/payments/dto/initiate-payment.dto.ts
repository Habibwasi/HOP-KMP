import { IsUUID, IsEnum } from 'class-validator'
import { PaymentProvider } from '@prisma/client'

export class InitiatePaymentDto {
  @IsUUID()
  bookingId: string

  @IsEnum(PaymentProvider)
  provider: PaymentProvider
}
