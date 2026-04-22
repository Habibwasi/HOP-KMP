import { Global, Module } from '@nestjs/common'
import { createClient } from '@supabase/supabase-js'
import { ConfigService } from '@nestjs/config'
import { PrismaModule } from '../prisma/prisma.module'
import { SupabaseGuard } from './supabase.guard'

@Global()
@Module({
  imports: [PrismaModule],
  providers: [
    {
      provide: 'SUPABASE_CLIENT',
      inject: [ConfigService],
      useFactory: (config: ConfigService) =>
        createClient(
          config.getOrThrow<string>('SUPABASE_URL'),
          config.getOrThrow<string>('SUPABASE_SERVICE_ROLE_KEY'),
        ),
    },
    SupabaseGuard,
  ],
  exports: ['SUPABASE_CLIENT', SupabaseGuard],
})
export class AuthModule {}
