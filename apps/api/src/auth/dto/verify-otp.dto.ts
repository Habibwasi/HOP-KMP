import { IsPhoneNumber, IsString, Length, IsEnum } from 'class-validator'
import { OtpPurpose } from '@prisma/client'

export class VerifyOtpDto {
  @IsPhoneNumber(undefined)
  phone: string

  @IsString()
  @Length(6, 6)
  code: string

  @IsEnum(OtpPurpose)
  purpose: OtpPurpose
}
