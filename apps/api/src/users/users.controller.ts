import {
  Controller, Get, Post, UseGuards, Req, Param,
  Body, NotFoundException, UnauthorizedException,
  Inject,
} from '@nestjs/common'
import { SupabaseClient } from '@supabase/supabase-js'
import { SupabaseGuard } from '../auth/supabase.guard'
import { UsersService } from './users.service'
import { RatingsService } from '../ratings/ratings.service'
import { CreateProfileDto } from './dto/create-profile.dto'

@Controller('users')
export class UsersController {
  constructor(
    private users: UsersService,
    private ratings: RatingsService,
    @Inject('SUPABASE_CLIENT') private readonly supabase: SupabaseClient,
  ) {}

  /**
   * Creates (or updates) the Prisma profile for a Supabase-authenticated user.
   * Must be called once right after Supabase sign-up; this endpoint is intentionally
   * unguarded so it can run before the Prisma row exists.
   */
  @Post('profile')
  async createProfile(@Req() req: any, @Body() dto: CreateProfileDto) {
    const auth: string | undefined = req.headers?.authorization
    if (!auth?.startsWith('Bearer ')) throw new UnauthorizedException()
    const token = auth.slice(7)

    const { data: { user: supabaseUser }, error } = await this.supabase.auth.getUser(token)
    if (error || !supabaseUser) throw new UnauthorizedException()

    return this.users.createProfile(supabaseUser.id, {
      firstName: dto.firstName,
      lastName: dto.lastName,
      phone: dto.phone,
      email: dto.email ?? supabaseUser.email,
    })
  }

  @Get('me')
  @UseGuards(SupabaseGuard)
  async getMe(@Req() req: any) {
    const user = await this.users.findById(req.user.id)
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
}
