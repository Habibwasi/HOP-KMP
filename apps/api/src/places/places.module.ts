import { Module } from '@nestjs/common'
import { PrismaModule } from '../prisma/prisma.module'
import { AuthModule } from '../auth/auth.module'
import { PlacesController } from './places.controller'
import { PlacesService } from './places.service'

@Module({
  imports: [PrismaModule, AuthModule],
  controllers: [PlacesController],
  providers: [PlacesService],
  exports: [PlacesService],
})
export class PlacesModule {}
