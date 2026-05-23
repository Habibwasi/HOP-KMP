import {
  CanActivate,
  ExecutionContext,
  Inject,
  Injectable,
  Logger,
} from '@nestjs/common'
import { SupabaseClient } from '@supabase/supabase-js'
import { PrismaService } from '../prisma/prisma.service'
import { AppException } from '../common/errors/app-exception'
import { ApiErrorCode } from '../common/errors/api-error-codes'

@Injectable()
export class SupabaseGuard implements CanActivate {
  private readonly logger = new Logger(SupabaseGuard.name)

  constructor(
    @Inject('SUPABASE_CLIENT') private readonly supabase: SupabaseClient,
    private readonly prisma: PrismaService,
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest()
    const token = this.extractBearerToken(request)
    if (!token) throw new AppException(ApiErrorCode.TOKEN_MISSING)

    const {
      data: { user: supabaseUser },
      error,
    } = await this.supabase.auth.getUser(token)

    if (error || !supabaseUser) throw new AppException(ApiErrorCode.TOKEN_INVALID)

    let user = await this.prisma.user.findUnique({ where: { id: supabaseUser.id } })

    if (!user) {
      // ── Migration path: old-system users have a Prisma id that doesn't match
      // their Supabase UUID. Try to find them by email so existing data isn't lost.
      const email = supabaseUser.email
      if (email) {
        const existingByEmail = await this.prisma.user.findUnique({ where: { email } })
        if (existingByEmail) {
          // Reconcile: we can't change the PK, so we create a new row with the
          // Supabase UUID, copying profile data, and leave the old row in place.
          // Going forward, the Supabase UUID row is authoritative.
          // Use a transaction: clear phone/email on the old row first so the
          // unique constraints don't fire when creating the new row.
          this.logger.log(`Migrating legacy user ${existingByEmail.id} → ${supabaseUser.id}`)
          // All FKs on User have ON UPDATE CASCADE, so updating the PK in-place
          // re-parents every related row (Trip, Booking, Rating, etc.) atomically.
          await this.prisma.$executeRaw`UPDATE "User" SET "id" = ${supabaseUser.id} WHERE "id" = ${existingByEmail.id}`
          user = await this.prisma.user.findUniqueOrThrow({ where: { id: supabaseUser.id } })
        }
      }
    }

    if (!user) {
      // ── Auto-create path: new signup or first OAuth sign-in.
      // Email/password: mobile stores firstName/lastName/phone in user_metadata.
      // Google OAuth: provides given_name / family_name (or full_name).
      // Apple OAuth: may only provide full_name on first sign-in.
      const meta = (supabaseUser.user_metadata ?? {}) as Record<string, string>
      const fullNameParts = (meta['full_name'] ?? '').trim().split(/\s+/)
      const firstName =
        meta['firstName'] ??
        meta['first_name'] ??
        meta['given_name'] ??
        (fullNameParts.length >= 1 ? fullNameParts[0] : undefined)
      const lastName =
        meta['lastName'] ??
        meta['last_name'] ??
        meta['family_name'] ??
        (fullNameParts.length >= 2 ? fullNameParts.slice(1).join(' ') : undefined)
      const avatarUrl: string | null =
        meta['avatar_url'] ?? meta['picture'] ?? null

      if (firstName && lastName) {
        this.logger.log(`Auto-creating Prisma profile for Supabase user ${supabaseUser.id}`)
        user = await this.prisma.user.upsert({
          where: { id: supabaseUser.id },
          create: {
            id: supabaseUser.id,
            email: supabaseUser.email ?? null,
            phone: meta['phone'] ?? null,
            firstName,
            lastName,
            avatarUrl,
          },
          update: {},
        })
      } else {
        throw new AppException(ApiErrorCode.PROFILE_NOT_FOUND)
      }
    }

    if (user.isBanned) {
      if (user.banExpiresAt && user.banExpiresAt <= new Date()) {
        // Lift the ban inline so the user can proceed immediately
        await this.prisma.user.update({
          where: { id: user.id },
          data: { isBanned: false, banExpiresAt: null },
        })
      } else {
        throw new AppException(ApiErrorCode.ACCOUNT_BANNED)
      }
    }

    request['user'] = user
    return true
  }

  private extractBearerToken(request: any): string | null {
    const auth: string | undefined = request.headers?.authorization
    if (!auth?.startsWith('Bearer ')) return null
    return auth.slice(7)
  }
}
