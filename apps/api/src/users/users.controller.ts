import { Controller, Get, UseGuards, Req, Param, NotFoundException } from '@nestjs/common'
import { AuthGuard } from '@nestjs/passport'
import { UsersService } from './users.service'
import { RatingsService } from '../ratings/ratings.service'

@Controller('users')
export class UsersController {
  constructor(
    private users: UsersService,
    private ratings: RatingsService,
  ) {}

  @Get('me')
  @UseGuards(AuthGuard('jwt'))
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
