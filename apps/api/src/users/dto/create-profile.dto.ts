import { IsEmail, IsOptional, IsString, MinLength } from 'class-validator'

export class CreateProfileDto {
  @IsString()
  @MinLength(1)
  firstName: string

  @IsString()
  @MinLength(1)
  lastName: string

  // Phone format is validated inside UsersService.createProfile() so
  // the controller can clean up the Supabase auth user on failure.
  @IsOptional()
  @IsString()
  phone?: string

  @IsOptional()
  @IsEmail()
  email?: string
}
