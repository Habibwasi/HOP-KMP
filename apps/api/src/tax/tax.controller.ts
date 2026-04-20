import { Controller, Get, Param, ParseIntPipe, UseGuards, Req } from '@nestjs/common'
import { AuthGuard } from '@nestjs/passport'
import { TaxService } from './tax.service'

@Controller('tax')
@UseGuards(AuthGuard('jwt'))
export class TaxController {
  constructor(private tax: TaxService) {}

  @Get('records')
  allRecords(@Req() req: any) {
    return this.tax.getAllRecords(req.user.id)
  }

  @Get(':year/:month')
  monthly(
    @Req() req: any,
    @Param('year', ParseIntPipe) year: number,
    @Param('month', ParseIntPipe) month: number,
  ) {
    return this.tax.getMonthlyDashboard(req.user.id, year, month)
  }

  @Get(':year')
  annual(@Req() req: any, @Param('year', ParseIntPipe) year: number) {
    return this.tax.getAnnualSummary(req.user.id, year)
  }
}
