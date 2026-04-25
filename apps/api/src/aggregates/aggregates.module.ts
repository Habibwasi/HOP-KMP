import { Module } from '@nestjs/common'
import { AggregatesController } from './aggregates.controller'
import { AggregatesService } from './aggregates.service'
import { PrismaModule } from '../prisma/prisma.module'
import { AuthModule } from '../auth/auth.module'

@Module({
  imports: [PrismaModule, AuthModule],
  controllers: [AggregatesController],
  providers: [AggregatesService],
})
export class AggregatesModule {}
