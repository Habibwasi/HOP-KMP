import { Injectable } from '@nestjs/common'
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

  async create(data: {
    phone: string
    firstName: string
    lastName: string
    passwordHash?: string
  }): Promise<User> {
    return this.prisma.user.create({ data })
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
