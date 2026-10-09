import { Transform } from 'class-transformer'
import { Equals, IsEmail, IsIn, IsOptional, IsString, MaxLength } from 'class-validator'

const trim = ({ value }: { value: unknown }) =>
  typeof value === 'string' ? value.trim().replace(/\s+/g, ' ') : value

export class JoinWaitlistDto {
  @Transform(({ value }) => (typeof value === 'string' ? value.trim().toLowerCase() : value))
  @IsEmail()
  @MaxLength(254)
  email: string

  @IsIn(['PASSENGER', 'DRIVER', 'BOTH'])
  role: 'PASSENGER' | 'DRIVER' | 'BOTH'

  @IsOptional()
  @Transform(trim)
  @IsString()
  @MaxLength(80)
  origin?: string

  @IsOptional()
  @Transform(trim)
  @IsString()
  @MaxLength(80)
  dest?: string

  @IsOptional()
  @IsIn(['da', 'en'])
  locale?: 'da' | 'en'

  @IsOptional()
  @IsString()
  @MaxLength(40)
  source?: string

  /** GDPR consent checkbox — must be explicitly true. */
  @Equals(true)
  consent: boolean

  /** Honeypot. Hidden in the form; bots fill it, people don't. */
  @IsOptional()
  @IsString()
  website?: string
}
