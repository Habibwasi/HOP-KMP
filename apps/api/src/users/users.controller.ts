import {
  Controller, Get, Post, Patch, UseGuards, Req, Param,
  Body, NotFoundException, UnauthorizedException, ConflictException, Logger,
  HttpCode, HttpStatus,
  Inject,
} from '@nestjs/common'
import { SupabaseClient } from '@supabase/supabase-js'
import { SupabaseGuard } from '../auth/supabase.guard'
import { UsersService } from './users.service'
import { RatingsService } from '../ratings/ratings.service'
import { NotificationsService } from '../notifications/notifications.service'
import { CreateProfileDto } from './dto/create-profile.dto'
import { UpdateUserDto } from './dto/update-user.dto'
import { IsString, MinLength } from 'class-validator'

class ReportDto {
  @IsString()
  @MinLength(1)
  reason: string
}

class PushTokenDto {
  @IsString()
  token: string

  @IsString()
  platform: string
}

@Controller('users')
export class UsersController {
  private readonly logger = new Logger(UsersController.name)

  constructor(
    private users: UsersService,
    private ratings: RatingsService,
    private notifications: NotificationsService,
    @Inject('SUPABASE_CLIENT') private readonly supabase: SupabaseClient,
  ) {}

  /**
   * Creates (or updates) the Prisma profile for a Supabase-authenticated user.
   * Must be called once right after Supabase sign-up; this endpoint is intentionally
   * unguarded so it can run before the Prisma row exists.
   *
   * If profile creation fails with a conflict (phone/email already taken by a
   * different account), the dangling Supabase auth user is deleted so the caller
   * can sign up again with corrected details without hitting "user already exists".
   */
  @Post('profile')
  async createProfile(@Req() req: any, @Body() dto: CreateProfileDto) {
    const auth: string | undefined = req.headers?.authorization
    if (!auth?.startsWith('Bearer ')) throw new UnauthorizedException()
    const token = auth.slice(7)

    const { data: { user: supabaseUser }, error } = await this.supabase.auth.getUser(token)
    if (error || !supabaseUser) throw new UnauthorizedException()

    try {
      return await this.users.createProfile(supabaseUser.id, {
        firstName: dto.firstName,
        lastName: dto.lastName,
        phone: dto.phone,
        email: dto.email ?? supabaseUser.email,
      })
    } catch (err) {
      if (err instanceof ConflictException) {
        // Roll back: delete the Supabase auth user so the email is free again.
        // The service-role client has the rights to do this.
        const { error: deleteError } = await this.supabase.auth.admin.deleteUser(supabaseUser.id)
        if (deleteError) {
          this.logger.error(
            `Failed to delete dangling Supabase user ${supabaseUser.id}: ${deleteError.message}`,
          )
        }
      }
      throw err
    }
  }

  @Get('me')
  @UseGuards(SupabaseGuard)
  async getMe(@Req() req: any) {
    const user = await this.users.findById(req.user.id)
    if (!user) throw new NotFoundException('User not found')
    return user
  }

  @Patch('me')
  @UseGuards(SupabaseGuard)
  async updateMe(@Req() req: any, @Body() dto: UpdateUserDto) {
    const [firstName, ...rest] = dto.fullName.trim().split(' ')
    const lastName = rest.join(' ') || '.'
    return this.users.updateProfile(req.user.id, { firstName, lastName })
  }

  @Post('push-token')
  @UseGuards(SupabaseGuard)
  @HttpCode(HttpStatus.OK)
  async savePushToken(@Req() req: any, @Body() dto: PushTokenDto) {
    const platform = dto.platform === 'ios' ? 'ios' : 'android'
    await this.notifications.registerToken(req.user.id, dto.token, platform)
  }

  @Get(':id')
  @UseGuards(SupabaseGuard)
  async getUserById(@Param('id') id: string) {
    const user = await this.users.findById(id)
    if (!user) throw new NotFoundException('User not found')
    return user
  }

  @Get(':id/reviews')
  async getUserReviews(@Param('id') id: string) {
    const result = await this.ratings.getUserRatings(id)
    return result.ratings.map((r: any) => ({
      id: r.id,
      raterName: `${r.rater.firstName} ${r.rater.lastName}`.trim(),
      stars: r.score,
      comment: r.comment ?? null,
      roleRated: 'PASSENGER',
    }))
  }

  @Get(':id/car')
  async getCarDetails(@Param('id') id: string) {
    const car = await this.users.getCarDetails(id)
    if (!car) throw new NotFoundException('No car details found')
    return car
  }

  @Post(':id/report')
  @UseGuards(SupabaseGuard)
  @HttpCode(HttpStatus.OK)
  async reportUser(@Req() req: any, @Param('id') id: string, @Body() dto: ReportDto) {
    await this.users.reportUser(id, req.user.id, dto.reason)
  }
}
