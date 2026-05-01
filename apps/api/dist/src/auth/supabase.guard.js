"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
var SupabaseGuard_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.SupabaseGuard = void 0;
const common_1 = require("@nestjs/common");
const supabase_js_1 = require("@supabase/supabase-js");
const prisma_service_1 = require("../prisma/prisma.service");
let SupabaseGuard = SupabaseGuard_1 = class SupabaseGuard {
    supabase;
    prisma;
    logger = new common_1.Logger(SupabaseGuard_1.name);
    constructor(supabase, prisma) {
        this.supabase = supabase;
        this.prisma = prisma;
    }
    async canActivate(context) {
        const request = context.switchToHttp().getRequest();
        const token = this.extractBearerToken(request);
        if (!token)
            throw new common_1.UnauthorizedException();
        const { data: { user: supabaseUser }, error, } = await this.supabase.auth.getUser(token);
        if (error || !supabaseUser)
            throw new common_1.UnauthorizedException();
        let user = await this.prisma.user.findUnique({ where: { id: supabaseUser.id } });
        if (!user) {
            const email = supabaseUser.email;
            if (email) {
                const existingByEmail = await this.prisma.user.findUnique({ where: { email } });
                if (existingByEmail) {
                    this.logger.log(`Migrating legacy user ${existingByEmail.id} → ${supabaseUser.id}`);
                    user = await this.prisma.user.create({
                        data: {
                            id: supabaseUser.id,
                            email: existingByEmail.email,
                            phone: existingByEmail.phone,
                            firstName: existingByEmail.firstName,
                            lastName: existingByEmail.lastName,
                            avatarUrl: existingByEmail.avatarUrl,
                            role: existingByEmail.role,
                            isVerified: existingByEmail.isVerified,
                            isAdmin: existingByEmail.isAdmin,
                        },
                    });
                }
            }
        }
        if (!user) {
            const meta = (supabaseUser.user_metadata ?? {});
            const firstName = meta['firstName'] ?? meta['first_name'];
            const lastName = meta['lastName'] ?? meta['last_name'];
            if (firstName && lastName) {
                this.logger.log(`Auto-creating Prisma profile for Supabase user ${supabaseUser.id}`);
                user = await this.prisma.user.upsert({
                    where: { id: supabaseUser.id },
                    create: {
                        id: supabaseUser.id,
                        email: supabaseUser.email ?? null,
                        phone: meta['phone'] ?? null,
                        firstName,
                        lastName,
                    },
                    update: {},
                });
            }
            else {
                throw new common_1.UnauthorizedException('Profile not found. Please register again or contact support.');
            }
        }
        if (user.isBanned) {
            if (user.banExpiresAt && user.banExpiresAt <= new Date()) {
                await this.prisma.user.update({
                    where: { id: user.id },
                    data: { isBanned: false, banExpiresAt: null },
                });
            }
            else {
                throw new common_1.UnauthorizedException('Account banned');
            }
        }
        request['user'] = user;
        return true;
    }
    extractBearerToken(request) {
        const auth = request.headers?.authorization;
        if (!auth?.startsWith('Bearer '))
            return null;
        return auth.slice(7);
    }
};
exports.SupabaseGuard = SupabaseGuard;
exports.SupabaseGuard = SupabaseGuard = SupabaseGuard_1 = __decorate([
    (0, common_1.Injectable)(),
    __param(0, (0, common_1.Inject)('SUPABASE_CLIENT')),
    __metadata("design:paramtypes", [supabase_js_1.SupabaseClient,
        prisma_service_1.PrismaService])
], SupabaseGuard);
//# sourceMappingURL=supabase.guard.js.map