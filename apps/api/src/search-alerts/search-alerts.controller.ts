import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Post,
  Req,
  UseGuards,
} from '@nestjs/common'
import { SupabaseGuard } from '../auth/supabase.guard'
import { SearchAlertsService } from './search-alerts.service'
import { CreateSearchAlertDto } from './dto/create-search-alert.dto'

@Controller('search-alerts')
@UseGuards(SupabaseGuard)
export class SearchAlertsController {
  constructor(private readonly service: SearchAlertsService) {}

  @Post()
  create(@Req() req: any, @Body() dto: CreateSearchAlertDto) {
    return this.service.create(req.user.id, dto)
  }

  @Get()
  list(@Req() req: any) {
    return this.service.list(req.user.id)
  }

  @Delete(':id')
  remove(@Req() req: any, @Param('id') id: string) {
    return this.service.remove(req.user.id, id)
  }
}
