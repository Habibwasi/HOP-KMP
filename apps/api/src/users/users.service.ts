import { ConflictException, Injectable } from '@nestjs/common'
import { PrismaService } from '../prisma/prisma.service'
import { User } from '@prisma/client'

@Injectable()
export class UsersService {
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
    if (data.phone) {
      const existing = await this.prisma.user.findUnique({ where: { phone: data.phone } })
      if (existing && existing.id !== supabaseId) {
        throw new ConflictException('Phone number already in use')
      }
    }

    return this.prisma.user.upsert({
      where: { id: supabaseId },
      create: {
        id: supabaseId,
        firstName: data.firstName,
        lastName: data.lastName,
        phone: data.phone,
        email: data.email,
      },
      update: {
        firstName: data.firstName,
        lastName: data.lastName,
        phone: data.phone,
        email: data.email,
      },
    })
  }

  async markVerified(userId: string): Promise<User> {
    return this.prisma.user.update({
      where: { id: userId },
      data: { isVerified: true },
    })
  }

  async getCarDetails(userId: string) {
    return this.prisma.carDetails.findUnique({ where: { userId } })
  }
}