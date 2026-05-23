import { Global, Module } from '@nestjs/common'
import { createClient } from '@supabase/supabase-js'
import { ConfigService } from '@nestjs/config'
import { JwtModule } from '@nestjs/jwt'
import { PrismaModule } from '../prisma/prisma.module'
import { MailModule } from '../mail/mail.module'
import { SupabaseGuard } from './supabase.guard'
import { EmailHookController } from './email-hook.controller'

@Global()
@Module({
  imports: [PrismaModule, MailModule, JwtModule.register({})],
  controllers: [EmailHookController],
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
