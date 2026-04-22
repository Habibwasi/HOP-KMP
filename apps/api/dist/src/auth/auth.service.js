"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
var AuthService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthService = void 0;
const common_1 = require("@nestjs/common");
const jwt_1 = require("@nestjs/jwt");
const config_1 = require("@nestjs/config");
const prisma_service_1 = require("../prisma/prisma.service");
const users_service_1 = require("../users/users.service");
const client_1 = require("@prisma/client");
const bcrypt = __importStar(require("bcrypt"));
const crypto = __importStar(require("crypto"));
const twilio_1 = __importDefault(require("twilio"));
let AuthService = AuthService_1 = class AuthService {
    prisma;
    users;
    jwt;
    config;
    logger = new common_1.Logger(AuthService_1.name);
    constructor(prisma, users, jwt, config) {
        this.prisma = prisma;
        this.users = users;
        this.jwt = jwt;
        this.config = config;
    }
    getTwilioClient() {
        return (0, twilio_1.default)(this.config.getOrThrow('TWILIO_ACCOUNT_SID'), this.config.getOrThrow('TWILIO_AUTH_TOKEN'));
    }
    toE164(phone) {
        const trimmed = phone.trim().replace(/\s+/g, '');
        if (trimmed.startsWith('+'))
            return trimmed;
        if (trimmed.startsWith('00'))
            return '+' + trimmed.slice(2);
        if (/^\d{8}$/.test(trimmed))
            return '+45' + trimmed;
        throw new common_1.BadRequestException(`Phone number "${phone}" is not in a recognised format. Use E.164 (e.g. +4520123456).`);
    }
    async sendOtp(phone, purpose) {
        let user = await this.users.findByPhone(phone);
        if (!user) {
            if (purpose === client_1.OtpPurpose.LOGIN) {
                throw new common_1.BadRequestException('User not found. Please register first.');
            }
            user = await this.users.create({ phone, firstName: '', lastName: '' });
        }
        const e164 = this.toE164(phone);
        const service = this.getTwilioClient()
            .verify.v2.services(this.config.getOrThrow('TWILIO_VERIFY_SERVICE_SID'));
        try {
            await service.verifications.create({ to: e164, channel: 'sms' });
        }
        catch (smsErr) {
            if (smsErr?.code === 60410 || smsErr?.message?.includes('prefix is blocked for the SMS channel')) {
                this.logger.warn(`SMS blocked for ${e164} — retrying via voice call`);
                try {
                    await service.verifications.create({ to: e164, channel: 'call' });
                }
                catch (callErr) {
                    this.logger.error(`Twilio sendOtp (call fallback) failed for ${e164}: ${callErr?.message}`);
                    throw new common_1.BadRequestException(callErr?.message ?? 'Failed to send OTP');
                }
                return { message: 'OTP sent via voice call' };
            }
            this.logger.error(`Twilio sendOtp failed for ${e164}: ${smsErr?.message}`);
            throw new common_1.BadRequestException(smsErr?.message ?? 'Failed to send OTP');
        }
        return { message: 'OTP sent successfully' };
    }
    async verifyOtp(phone, code, purpose) {
        const user = await this.users.findByPhone(phone);
        if (!user)
            throw new common_1.BadRequestException('User not found');
        const e164 = this.toE164(phone);
        let check;
        try {
            check = await this.getTwilioClient()
                .verify.v2.services(this.config.getOrThrow('TWILIO_VERIFY_SERVICE_SID'))
                .verificationChecks.create({ to: e164, code });
        }
        catch (err) {
            this.logger.error(`Twilio verifyOtp failed for ${e164}: ${err?.message}`);
            throw new common_1.BadRequestException(err?.message ?? 'Failed to verify OTP');
        }
        if (check.status !== 'approved') {
            throw new common_1.BadRequestException('Invalid or expired OTP');
        }
        if (purpose === client_1.OtpPurpose.PHONE_VERIFY) {
            await this.users.markVerified(user.id);
        }
        return { verified: true };
    }
    async register(dto) {
        const existing = await this.users.findByPhone(dto.phone);
        if (existing && existing.isVerified) {
            throw new common_1.ConflictException('Phone already registered');
        }
        const passwordHash = dto.password ? await bcrypt.hash(dto.password, 12) : undefined;
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
            });
        const tokens = await this.generateTokens(user.id, user.phone);
        return { user: this.sanitize(user), ...tokens };
    }
    async login(email, password) {
        const user = await this.users.findByEmail(email);
        if (!user || !user.passwordHash)
            throw new common_1.UnauthorizedException('Invalid credentials');
        if (user.isBanned)
            throw new common_1.UnauthorizedException('Account banned');
        const valid = await bcrypt.compare(password, user.passwordHash);
        if (!valid)
            throw new common_1.UnauthorizedException('Invalid credentials');
        const tokens = await this.generateTokens(user.id, user.phone);
        return { user: this.sanitize(user), ...tokens };
    }
    async loginWithOtp(phone, code) {
        const user = await this.users.findByPhone(phone);
        if (!user)
            throw new common_1.UnauthorizedException('User not found');
        if (user.isBanned)
            throw new common_1.UnauthorizedException('Account banned');
        const check = await this.getTwilioClient()
            .verify.v2.services(this.config.getOrThrow('TWILIO_VERIFY_SERVICE_SID'))
            .verificationChecks.create({ to: phone, code });
        if (check.status !== 'approved') {
            throw new common_1.UnauthorizedException('Invalid or expired OTP');
        }
        const tokens = await this.generateTokens(user.id, user.phone);
        return { user: this.sanitize(user), ...tokens };
    }
    async refreshTokens(userId, refreshToken) {
        const stored = await this.prisma.refreshToken.findUnique({
            where: { token: refreshToken },
        });
        if (!stored || stored.userId !== userId || stored.isRevoked || stored.expiresAt < new Date()) {
            throw new common_1.UnauthorizedException('Invalid refresh token');
        }
        await this.prisma.refreshToken.update({ where: { id: stored.id }, data: { isRevoked: true } });
        const user = await this.users.findById(userId);
        if (!user || user.isBanned)
            throw new common_1.UnauthorizedException();
        return this.generateTokens(user.id, user.phone);
    }
    async logout(refreshToken) {
        await this.prisma.refreshToken.updateMany({
            where: { token: refreshToken },
            data: { isRevoked: true },
        });
        return { message: 'Logged out' };
    }
    async generateTokens(userId, phone) {
        const accessToken = this.jwt.sign({ sub: userId, phone }, {
            secret: this.config.get('JWT_ACCESS_SECRET'),
            expiresIn: this.config.get('JWT_ACCESS_EXPIRES_IN'),
        });
        const refreshToken = crypto.randomBytes(64).toString('hex');
        const expiresAt = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000);
        await this.prisma.refreshToken.updateMany({
            where: { userId, isRevoked: false },
            data: { isRevoked: true },
        });
        await this.prisma.refreshToken.create({
            data: { token: refreshToken, userId, expiresAt },
        });
        return { accessToken, refreshToken };
    }
    sanitize(user) {
        const { passwordHash, ...rest } = user;
        return rest;
    }
};
exports.AuthService = AuthService;
exports.AuthService = AuthService = AuthService_1 = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        users_service_1.UsersService,
        jwt_1.JwtService,
        config_1.ConfigService])
], AuthService);
//# sourceMappingURL=auth.service.js.map