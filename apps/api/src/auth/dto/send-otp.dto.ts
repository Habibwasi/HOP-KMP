import { IsPhoneNumber, IsEnum } from 'class-validator'
import { OtpPurpose } from '@prisma/client'

export class SendOtpDto {
  @IsPhoneNumber(undefined)
  phone: string

  @IsEnum(OtpPurpose)
  purpose: OtpPurpose
}
