import { Module } from '@nestjs/common'
import { PrismaModule } from '../prisma/prisma.module'
import { AuthModule } from '../auth/auth.module'
import { SearchHistoryController } from './search-history.controller'
import { SearchHistoryService } from './search-history.service'

@Module({
  imports: [PrismaModule, AuthModule],
  controllers: [SearchHistoryController],
  providers: [SearchHistoryService],
  exports: [SearchHistoryService],
})
export class SearchHistoryModule {}
