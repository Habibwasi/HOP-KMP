import {
  Controller, Get, Post, Patch, UseGuards, Req, Param,
  Body, NotFoundException, UnauthorizedException, Logger,
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
import { CreateCarDetailsDto } from './dto/create-car-details.dto'
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
      // Roll back on ANY profile-creation error (conflict, bad request, unexpected
      // Prisma error, etc.) — delete the Supabase auth user so the email/phone is
      // free for a corrected re-registration attempt.
      //
      // Retry up to 3 times with exponential back-off (200ms, 400ms) because a
      // transient network blip to Supabase's admin API must not leave a dangling
      // auth user that permanently blocks re-registration with the same email.
      const MAX_DELETE_ATTEMPTS = 3
      let lastDeleteError: Error | null = null
      for (let attempt = 1; attempt <= MAX_DELETE_ATTEMPTS; attempt++) {
        const { error: deleteError } = await this.supabase.auth.admin.deleteUser(supabaseUser.id)
        if (!deleteError) { lastDeleteError = null; break }
        lastDeleteError = deleteError
        if (attempt < MAX_DELETE_ATTEMPTS) {
          await new Promise(r => setTimeout(r, 200 * attempt))
        }
      }

      if (lastDeleteError) {
        // All retries exhausted — the Supabase user is dangling. Log with full
        // context so an operator can delete it manually, then surface a specific
        // message to the client so the user knows to contact support rather than
        // retrying indefinitely.
        this.logger.error(
          `DANGLING_AUTH_USER supabaseId=${supabaseUser.id} email=${supabaseUser.email} ` +
          `deleteError="${lastDeleteError.message}" originalError="${(err as Error).message}" ` +
          `— manual cleanup required in Supabase dashboard`,
        )
        throw new Error(
          `Registration failed: ${(err as Error).message}. ` +
          `Your account is in a partial state — please contact support with your email address ` +
          `before attempting to register again.`,
        )
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

  @Get('me/stats')
  @UseGuards(SupabaseGuard)
  async getMyStats(@Req() req: any) {
    const userId = req.user.id
    const [ratingSummary, completedTrips] = await Promise.all([
      this.ratings.getUserRatings(userId),
      this.users.completedTripCount(userId),
    ])
    return {
      averageRating: ratingSummary.averageScore,
      totalRatings: ratingSummary.totalRatings,
      completedTrips,
    }
  }

  @Patch('me')
  @UseGuards(SupabaseGuard)
  async updateMe(@Req() req: any, @Body() dto: UpdateUserDto) {
    const data: { firstName?: string; lastName?: string; mobilepayNumber?: string } = {}

    if (dto.fullName?.trim()) {
      const [firstName, ...rest] = dto.fullName.trim().split(' ')
      data.firstName = firstName
      data.lastName = rest.join(' ') || '.'
    }

    if (dto.mobilepayNumber !== undefined) {
      data.mobilepayNumber = dto.mobilepayNumber
    }

    return this.users.updateProfile(req.user.id, data)
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

  @Post('me/car-details')
  @UseGuards(SupabaseGuard)
  @HttpCode(HttpStatus.OK)
  async saveMyCarDetails(@Req() req: any, @Body() dto: CreateCarDetailsDto) {
    return this.users.saveCarDetails(req.user.id, {
      make: dto.make,
      model: dto.model,
      year: dto.year,
      licensePlate: dto.license_plate,
      colour: dto.colour,
      seatsAvailable: dto.seats_available,
    })
  }

  @Post(':id/report')
  @UseGuards(SupabaseGuard)
  @HttpCode(HttpStatus.OK)
  async reportUser(@Req() req: any, @Param('id') id: string, @Body() dto: ReportDto) {
    await this.users.reportUser(id, req.user.id, dto.reason)
  }
}
