import { Module } from '@nestjs/common'
import { HelpCenterService } from './help-center.service'
import { HelpCenterController } from './help-center.controller'

@Module({
  providers: [HelpCenterService],
  controllers: [HelpCenterController],
})
export class HelpCenterModule {}
