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

@Injectable()
export class AuthService {
  constructor(
    private prisma: PrismaService,
    private users: UsersService,
    private jwt: JwtService,
    private config: ConfigService,
  ) {}

  // ─── OTP ────────────────────────────────────────────────────────────────────

  async sendOtp(phone: string, purpose: OtpPurpose): Promise<{ message: string }> {
    const code = Math.floor(100000 + Math.random() * 900000).toString()
    const expiresAt = new Date(Date.now() + 10 * 60 * 1000) // 10 minutes

    let user = await this.users.findByPhone(phone)

    if (!user) {
      if (purpose === OtpPurpose.LOGIN) {
        throw new BadRequestException('User not found. Please register first.')
      }
      // For PHONE_VERIFY during registration we create a placeholder
      user = await this.users.create({ phone, firstName: '', lastName: '' })
    }

    // Invalidate previous OTPs for same purpose
    await this.prisma.otpCode.updateMany({
      where: { userId: user.id, purpose, used: false },
      data: { used: true },
    })

    await this.prisma.otpCode.create({
      data: { userId: user.id, code, purpose, expiresAt },
    })

    // TODO: send via Twilio Verify in production
    console.log(`OTP for ${phone}: ${code}`)

    return { message: 'OTP sent successfully' }
  }

  async verifyOtp(phone: string, code: string, purpose: OtpPurpose): Promise<{ verified: boolean }> {
    const user = await this.users.findByPhone(phone)
    if (!user) throw new BadRequestException('User not found')

    const otp = await this.prisma.otpCode.findFirst({
      where: {
        userId: user.id,
        code,
        purpose,
        used: false,
        expiresAt: { gt: new Date() },
      },
    })

    if (!otp) throw new BadRequestException('Invalid or expired OTP')

    await this.prisma.otpCode.update({ where: { id: otp.id }, data: { used: true } })

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

    const otp = await this.prisma.otpCode.findFirst({
      where: {
        userId: user.id,
        code,
        purpose: OtpPurpose.LOGIN,
        used: false,
        expiresAt: { gt: new Date() },
      },
    })

    if (!otp) throw new UnauthorizedException('Invalid or expired OTP')

    await this.prisma.otpCode.update({ where: { id: otp.id }, data: { used: true } })

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

    // Rotate — revoke old, issue new
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
    const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) // 30 days

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
