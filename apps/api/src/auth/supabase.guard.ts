import {
  CanActivate,
  ExecutionContext,
  Inject,
  Injectable,
  Logger,
  UnauthorizedException,
} from '@nestjs/common'
import { SupabaseClient } from '@supabase/supabase-js'
import { PrismaService } from '../prisma/prisma.service'

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
    if (!token) throw new UnauthorizedException()

    const {
      data: { user: supabaseUser },
      error,
    } = await this.supabase.auth.getUser(token)

    if (error || !supabaseUser) throw new UnauthorizedException()

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
          this.logger.log(`Migrating legacy user ${existingByEmail.id} → ${supabaseUser.id}`)
          user = await this.prisma.user.create({
            data: {
              id: supabaseUser.id,
              email: existingByEmail.email,
              phone: existingByEmail.phone,
              firstName: existingByEmail.firstName,
              lastName: existingByEmail.lastName,
              avatarUrl: existingByEmail.avatarUrl,
              role: existingByEmail.role,
              isVerified: existingByEmail.isVerified,
              isAdmin: existingByEmail.isAdmin,
            },
          })
        }
      }
    }

    if (!user) {
      // ── Auto-create path: new signup with email confirmation ON.
      // The mobile stored firstName/lastName/phone in user_metadata during signUpWith.
      const meta = (supabaseUser.user_metadata ?? {}) as Record<string, string>
      const firstName = meta['firstName'] ?? meta['first_name']
      const lastName = meta['lastName'] ?? meta['last_name']

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
          },
          update: {},
        })
      } else {
        throw new UnauthorizedException(
          'Profile not found. Please register again or contact support.',
        )
      }
    }

    if (user.isBanned) throw new UnauthorizedException('Account banned')

    request['user'] = user
    return true
  }

  private extractBearerToken(request: any): string | null {
    const auth: string | undefined = request.headers?.authorization
    if (!auth?.startsWith('Bearer ')) return null
    return auth.slice(7)
  }
}
