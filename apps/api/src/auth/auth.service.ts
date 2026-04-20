import {
  Injectable,
  BadRequestException,
  UnauthorizedException,
  ConflictException,
} from '@nestjs/common'
import { JwtService } from '@nestjs/jwt'
import { ConfigService } from '@nestjs/config'
import { PrismaService } from '../prisma/prisma.service'
import { UsersService } from '../users/users.service'
import { RegisterDto } from './dto/register.dto'
import { OtpPurpose } from '@prisma/client'
import * as bcrypt from 'bcrypt'
import * as crypto from 'crypto'
import twilio from 'twilio'

@Injectable()
export class AuthService {
  constructor(
    private prisma: PrismaService,
    private users: UsersService,
    private jwt: JwtService,
    private config: ConfigService,
  ) {}

  private getTwilioClient() {
    return twilio(
      this.config.getOrThrow('TWILIO_ACCOUNT_SID'),
      this.config.getOrThrow('TWILIO_AUTH_TOKEN'),
    )
  }

  // ─── OTP ────────────────────────────────────────────────────────────────────

  async sendOtp(phone: string, purpose: OtpPurpose): Promise<{ message: string }> {
    let user = await this.users.findByPhone(phone)

    if (!user) {
      if (purpose === OtpPurpose.LOGIN) {
        throw new BadRequestException('User not found. Please register first.')
      }
      user = await this.users.create({ phone, firstName: '', lastName: '' })
    }

    await this.getTwilioClient()
      .verify.v2.services(this.config.getOrThrow('TWILIO_VERIFY_SERVICE_SID'))
      .verifications.create({ to: phone, channel: 'sms' })

    return { message: 'OTP sent successfully' }
  }

  async verifyOtp(phone: string, code: string, purpose: OtpPurpose): Promise<{ verified: boolean }> {
    const user = await this.users.findByPhone(phone)
    if (!user) throw new BadRequestException('User not found')

    const check = await this.getTwilioClient()
      .verify.v2.services(this.config.getOrThrow('TWILIO_VERIFY_SERVICE_SID'))
      .verificationChecks.create({ to: phone, code })

    if (check.status !== 'approved') {
      throw new BadRequestException('Invalid or expired OTP')
    }

    if (purpose === OtpPurpose.PHONE_VERIFY) {
      await this.users.markVerified(user.id)
    }

    return { verified: true }
  }

  // ─── REGISTER ────────────────────────────────────────────────────────────────

  async register(dto: RegisterDto) {
    const existing = await this.users.findByPhone(dto.phone)

    if (existing && existing.isVerified) {
      throw new ConflictException('Phone already registered')
    }

    const passwordHash = dto.password ? await bcrypt.hash(dto.password, 12) : undefined

    const user = existing
      ? await this.prisma.user.update({
          where: { id: existing.id },
          data: {
            firstName: dto.firstName,
            lastName: dto.lastName,
            email: dto.email,
            passwordHash,
          },
        })
      : await this.users.create({
          phone: dto.phone,
          firstName: dto.firstName,
          lastName: dto.lastName,
          passwordHash,
        })

    const tokens = await this.generateTokens(user.id, user.phone)
    return { user: this.sanitize(user), ...tokens }
  }

  // ─── LOGIN WITH OTP ──────────────────────────────────────────────────────────

  async loginWithOtp(phone: string, code: string) {
    const user = await this.users.findByPhone(phone)
    if (!user) throw new UnauthorizedException('User not found')
    if (user.isBanned) throw new UnauthorizedException('Account banned')

    const check = await this.getTwilioClient()
      .verify.v2.services(this.config.getOrThrow('TWILIO_VERIFY_SERVICE_SID'))
      .verificationChecks.create({ to: phone, code })

    if (check.status !== 'approved') {
      throw new UnauthorizedException('Invalid or expired OTP')
    }

    const tokens = await this.generateTokens(user.id, user.phone)
    return { user: this.sanitize(user), ...tokens }
  }

  // ─── REFRESH ─────────────────────────────────────────────────────────────────

  async refreshTokens(userId: string, refreshToken: string) {
    const stored = await this.prisma.refreshToken.findUnique({
      where: { token: refreshToken },
    })

    if (!stored || stored.userId !== userId || stored.isRevoked || stored.expiresAt < new Date()) {
      throw new UnauthorizedException('Invalid refresh token')
    }

    await this.prisma.refreshToken.update({ where: { id: stored.id }, data: { isRevoked: true } })

    const user = await this.users.findById(userId)
    if (!user || user.isBanned) throw new UnauthorizedException()

    return this.generateTokens(user.id, user.phone)
  }

  // ─── LOGOUT ──────────────────────────────────────────────────────────────────

  async logout(refreshToken: string): Promise<{ message: string }> {
    await this.prisma.refreshToken.updateMany({
      where: { token: refreshToken },
      data: { isRevoked: true },
    })
    return { message: 'Logged out' }
  }

  // ─── HELPERS ─────────────────────────────────────────────────────────────────

  private async generateTokens(userId: string, phone: string) {
    const accessToken = this.jwt.sign(
      { sub: userId, phone },
      {
        secret: this.config.get('JWT_ACCESS_SECRET'),
        expiresIn: this.config.get('JWT_ACCESS_EXPIRES_IN'),
      },
    )

    const refreshToken = crypto.randomBytes(64).toString('hex')
    const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)

    await this.prisma.refreshToken.create({
      data: { token: refreshToken, userId, expiresAt },
    })

    return { accessToken, refreshToken }
  }

  private sanitize(user: any) {
    const { passwordHash, ...rest } = user
    return rest
  }
}
