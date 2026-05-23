import { Body, Controller, Delete, Get, Param, Post, Query, Req, UseGuards } from '@nestjs/common'
import { IsInt, IsOptional, IsString, Max, MaxLength, Min, MinLength } from 'class-validator'
import { Type } from 'class-transformer'
import { SupabaseGuard } from '../auth/supabase.guard'
import { SearchHistoryService } from './search-history.service'

export class RecordSearchDto {
  @IsString() @MinLength(1) @MaxLength(120)
  originLabel: string

  @IsString() @MinLength(1) @MaxLength(120)
  destLabel: string
}

class ListQueryDto {
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(20)
  limit?: number
}

@Controller('search/recent')
@UseGuards(SupabaseGuard)
export class SearchHistoryController {
  constructor(private readonly history: SearchHistoryService) {}

  @Get()
  list(@Req() req: any, @Query() q: ListQueryDto) {
    return this.history.list(req.user.id, q.limit ?? 5)
  }

  @Post()
  record(@Req() req: any, @Body() dto: RecordSearchDto) {
    return this.history.record(req.user.id, dto.originLabel, dto.destLabel)
  }

  @Delete(':id')
  remove(@Req() req: any, @Param('id') id: string) {
    return this.history.remove(req.user.id, id)
  }
}
