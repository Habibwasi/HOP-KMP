import {
  Injectable,
  BadRequestException,
  UnauthorizedException,
  ConflictException,
  Logger,
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
  private readonly logger = new Logger(AuthService.name)

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

  /**
   * Ensures a phone number is in E.164 format so Twilio always gets a valid `to`.
   * If the number already starts with '+' it is returned as-is (trimmed).
   * Otherwise a leading '00' is converted to '+', and bare 8-digit Danish numbers
   * get '+45' prepended.
   */
  private toE164(phone: string): string {
    const trimmed = phone.trim().replace(/\s+/g, '')
    if (trimmed.startsWith('+')) return trimmed
    if (trimmed.startsWith('00')) return '+' + trimmed.slice(2)
    // Bare 8-digit number — assume Danish (+45)
    if (/^\d{8}$/.test(trimmed)) return '+45' + trimmed
    throw new BadRequestException(`Phone number "${phone}" is not in a recognised format. Use E.164 (e.g. +4520123456).`)
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

    const e164 = this.toE164(phone)
    const service = this.getTwilioClient()
      .verify.v2.services(this.config.getOrThrow('TWILIO_VERIFY_SERVICE_SID'))

    // Try SMS first; fall back to voice call if the prefix is SMS-blocked by Twilio.
    try {
      await service.verifications.create({ to: e164, channel: 'sms' })
    } catch (smsErr: any) {
      // Twilio error 60410 = prefix blocked for SMS channel → retry via call
      if (smsErr?.code === 60410 || smsErr?.message?.includes('prefix is blocked for the SMS channel')) {
        this.logger.warn(`SMS blocked for ${e164} — retrying via voice call`)
        try {
          await service.verifications.create({ to: e164, channel: 'call' })
        } catch (callErr: any) {
          this.logger.error(`Twilio sendOtp (call fallback) failed for ${e164}: ${callErr?.message}`)
          throw new BadRequestException(callErr?.message ?? 'Failed to send OTP')
        }
        return { message: 'OTP sent via voice call' }
      }
      this.logger.error(`Twilio sendOtp failed for ${e164}: ${smsErr?.message}`)
      throw new BadRequestException(smsErr?.message ?? 'Failed to send OTP')
    }

    return { message: 'OTP sent successfully' }
  }

  async verifyOtp(phone: string, code: string, purpose: OtpPurpose): Promise<{ verified: boolean }> {
    const user = await this.users.findByPhone(phone)
    if (!user) throw new BadRequestException('User not found')

    const e164 = this.toE164(phone)
    let check: { status: string }
    try {
      check = await this.getTwilioClient()
        .verify.v2.services(this.config.getOrThrow('TWILIO_VERIFY_SERVICE_SID'))
        .verificationChecks.create({ to: e164, code })
    } catch (err: any) {
      this.logger.error(`Twilio verifyOtp failed for ${e164}: ${err?.message}`)
      throw new BadRequestException(err?.message ?? 'Failed to verify OTP')
    }

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
          email: dto.email,   
          passwordHash,
        })

    const tokens = await this.generateTokens(user.id, user.phone)
    return { user: this.sanitize(user), ...tokens }
  }

  // ─── LOGIN (email + password) ─────────────────────────────────────────────

  async login(email: string, password: string) {
    const user = await this.users.findByEmail(email)
    if (!user || !user.passwordHash) throw new UnauthorizedException('Invalid credentials')
    if (user.isBanned) throw new UnauthorizedException('Account banned')

    const valid = await bcrypt.compare(password, user.passwordHash)
    if (!valid) throw new UnauthorizedException('Invalid credentials')

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
    const expiresAt = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000) // 1 year

    // Revoke all previous non-expired refresh tokens for this user before
    // issuing a new one. Prevents ghost sessions accumulating in the DB and
    // ensures a new login/register invalidates all prior sessions for the
    // same account (single-session-per-user policy).
    await this.prisma.refreshToken.updateMany({
      where: { userId, isRevoked: false },
      data: { isRevoked: true },
    })

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
