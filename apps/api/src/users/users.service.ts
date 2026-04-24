import { BadRequestException, ConflictException, Injectable, Logger } from '@nestjs/common'
import { PrismaService } from '../prisma/prisma.service'
import { Prisma, User } from '@prisma/client'
import { parsePhoneNumber, isValidPhoneNumber } from 'libphonenumber-js'

@Injectable()
export class UsersService {
  private readonly logger = new Logger(UsersService.name)

  constructor(private prisma: PrismaService) {}

  async findByPhone(phone: string): Promise<User | null> {
    return this.prisma.user.findUnique({ where: { phone } })
  }

  async findByEmail(email: string): Promise<User | null> {
    return this.prisma.user.findUnique({ where: { email } })
  }

  async findById(id: string): Promise<User | null> {
    return this.prisma.user.findUnique({ where: { id } })
  }

  /**
   * Creates or updates the Prisma profile for a Supabase-authenticated user.
   * Called once by the client immediately after Supabase sign-up.
   *
   * @param supabaseId  The Supabase auth UUID — used as the Prisma User.id.
   * @param data        Profile fields supplied by the client.
   */
  async createProfile(
    supabaseId: string,
    data: { firstName: string; lastName: string; phone?: string; email?: string },
  ): Promise<User> {
    // Validate phone format before any DB work so errors are thrown inside
    // the controller's try/catch (enabling Supabase user cleanup on failure).
    if (data.phone) {
      if (!isValidPhoneNumber(data.phone)) {
        throw new BadRequestException(
          'Phone number must be in international format, e.g. +45 20 12 34 56',
        )
      }
      // Normalise to E.164 so storage is consistent regardless of spacing
      data.phone = parsePhoneNumber(data.phone).format('E.164')
    }

    // Check for phone taken by a *different* account
    if (data.phone) {
      const existing = await this.prisma.user.findUnique({ where: { phone: data.phone } })
      if (existing && existing.id !== supabaseId) {
        throw new ConflictException('Phone number already in use')
      }
    }

    // Check for email taken by a *different* account
    if (data.email) {
      const existing = await this.prisma.user.findUnique({ where: { email: data.email } })
      if (existing && existing.id !== supabaseId) {
        throw new ConflictException('Email already in use')
      }
    }

    try {
      return await this.prisma.user.upsert({
        where: { id: supabaseId },
        create: {
          id: supabaseId,
          firstName: data.firstName,
          lastName: data.lastName,
          phone: data.phone ?? null,
          email: data.email ?? null,
        },
        update: {
          firstName: data.firstName,
          lastName: data.lastName,
          phone: data.phone ?? null,
          email: data.email ?? null,
        },
      })
    } catch (e) {
      if (e instanceof Prisma.PrismaClientKnownRequestError && e.code === 'P2002') {
        const fields = (e.meta?.target as string[])?.join(', ') ?? 'field'
        throw new ConflictException(`${fields} already in use`)
      }
      throw e
    }
  }

  async markVerified(userId: string): Promise<User> {
    return this.prisma.user.update({
      where: { id: userId },
      data: { isVerified: true },
    })
  }

  async updateProfile(userId: string, data: { firstName: string; lastName: string }): Promise<User> {
    return this.prisma.user.update({
      where: { id: userId },
      data,
    })
  }

  async reportUser(reportedId: string, reporterId: string, reason: string): Promise<void> {
    this.logger.log(`User ${reporterId} reported ${reportedId}: ${reason}`)
  }

  async getCarDetails(userId: string) {
    return this.prisma.carDetails.findUnique({ where: { userId } })
  }
}