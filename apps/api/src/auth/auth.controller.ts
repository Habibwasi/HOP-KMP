import { Controller, Post, Body, UseGuards, Req, HttpCode } from '@nestjs/common'
import { AuthService } from './auth.service'
import { SendOtpDto } from './dto/send-otp.dto'
import { VerifyOtpDto } from './dto/verify-otp.dto'
import { RegisterDto } from './dto/register.dto'
import { AuthGuard } from '@nestjs/passport'

@Controller('auth')
export class AuthController {
  constructor(private auth: AuthService) {}

  @Post('otp/send')
  sendOtp(@Body() dto: SendOtpDto) {
    return this.auth.sendOtp(dto.phone, dto.purpose)
  }

  @Post('otp/verify')
  @HttpCode(200)
  verifyOtp(@Body() dto: VerifyOtpDto) {
    return this.auth.verifyOtp(dto.phone, dto.code, dto.purpose)
  }

  @Post('register')
  register(@Body() dto: RegisterDto) {
    return this.auth.register(dto)
  }

  @Post('login/otp')
  @HttpCode(200)
  loginWithOtp(@Body() dto: VerifyOtpDto) {
    return this.auth.loginWithOtp(dto.phone, dto.code)
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
