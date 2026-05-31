import { Controller, Get } from '@nestjs/common'
import { HelpCenterService } from './help-center.service'

@Controller('help-center')
export class HelpCenterController {
  constructor(private readonly helpCenter: HelpCenterService) {}

  /** GET /help-center/faqs — public, no auth required */
  @Get('faqs')
  getFaqs() {
    return this.helpCenter.getFaqs()
  }
}
