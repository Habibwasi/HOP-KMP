import { Controller, Post, Body, UseGuards, Req, HttpCode } from '@nestjs/common'
import { AuthService } from './auth.service'
import { RegisterDto } from './dto/register.dto'
import { LoginDto } from './dto/login.dto'
import { AuthGuard } from '@nestjs/passport'

@Controller('auth')
export class AuthController {
  constructor(private auth: AuthService) {}

  @Post('register')
  register(@Body() dto: RegisterDto) {
    return this.auth.register(dto)
  }

  @Post('login')
  @HttpCode(200)
  login(@Body() dto: LoginDto) {
    return this.auth.login(dto.email, dto.password)
  }

  @Post('refresh')
  @HttpCode(200)
  @UseGuards(AuthGuard('jwt-refresh'))
  refresh(@Req() req: any) {
    return this.auth.refreshTokens(req.user.sub, req.user.refreshToken)
  }

  @Post('logout')
  @HttpCode(200)
  logout(@Body('refreshToken') refreshToken: string) {
    return this.auth.logout(refreshToken)
  }
}
