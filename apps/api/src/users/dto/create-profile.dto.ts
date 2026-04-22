import { IsEmail, IsOptional, IsPhoneNumber, IsString, MinLength } from 'class-validator'

export class CreateProfileDto {
  @IsString()
  @MinLength(1)
  firstName: string

  @IsString()
  @MinLength(1)
  lastName: string

  @IsOptional()
  @IsPhoneNumber(undefined)
  phone?: string

  @IsOptional()
  @IsEmail()
  email?: string
}
