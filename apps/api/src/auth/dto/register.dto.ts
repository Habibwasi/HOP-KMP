import { IsPhoneNumber, IsString, MinLength, IsOptional, IsEmail } from 'class-validator'

export class RegisterDto {
  @IsPhoneNumber(undefined)
  phone: string

  @IsString()
  @MinLength(2)
  firstName: string

  @IsString()
  @MinLength(2)
  lastName: string

  @IsOptional()
  @IsEmail()
  email?: string

  @IsOptional()
  @IsString()
  @MinLength(8)
  password?: string
}
