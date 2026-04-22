import {
  CanActivate,
  ExecutionContext,
  Inject,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common'
import { SupabaseClient } from '@supabase/supabase-js'
import { PrismaService } from '../prisma/prisma.service'

@Injectable()
export class SupabaseGuard implements CanActivate {
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

    const user = await this.prisma.user.findUnique({ where: { id: supabaseUser.id } })
    if (!user) throw new UnauthorizedException('User profile not found')
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
