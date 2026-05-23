import { IsInt, IsNotEmpty, IsOptional, IsString, Min } from 'class-validator'
import { Type } from 'class-transformer'

export class CreateSearchAlertDto {
  @IsString()
  @IsNotEmpty()
  origin: string

  @IsString()
  @IsNotEmpty()
  dest: string

  @IsOptional()
  @IsInt()
  @Min(1)
  @Type(() => Number)
  seats?: number
}
