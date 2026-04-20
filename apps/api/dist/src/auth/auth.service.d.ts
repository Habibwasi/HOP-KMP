import { JwtService } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../prisma/prisma.service';
import { UsersService } from '../users/users.service';
import { RegisterDto } from './dto/register.dto';
import { OtpPurpose } from '@prisma/client';
export declare class AuthService {
    private prisma;
    private users;
    private jwt;
    private config;
    constructor(prisma: PrismaService, users: UsersService, jwt: JwtService, config: ConfigService);
    sendOtp(phone: string, purpose: OtpPurpose): Promise<{
        message: string;
    }>;
    verifyOtp(phone: string, code: string, purpose: OtpPurpose): Promise<{
        verified: boolean;
    }>;
    register(dto: RegisterDto): Promise<{
        accessToken: string;
        refreshToken: string;
        user: any;
    }>;
    loginWithOtp(phone: string, code: string): Promise<{
        accessToken: string;
        refreshToken: string;
        user: any;
    }>;
    refreshTokens(userId: string, refreshToken: string): Promise<{
        accessToken: string;
        refreshToken: string;
    }>;
    logout(refreshToken: string): Promise<{
        message: string;
    }>;
    private generateTokens;
    private sanitize;
}
