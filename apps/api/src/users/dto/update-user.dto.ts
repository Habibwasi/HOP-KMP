import { IsOptional, IsString, Matches, MinLength } from 'class-validator'

export class UpdateUserDto {
  @IsOptional()
  @IsString()
  @MinLength(1)
  fullName?: string

  @IsOptional()
  @Matches(/^\d{8}$/, { message: 'mobilepayNumber must be an 8-digit Danish phone number' })
  mobilepayNumber?: string
}
