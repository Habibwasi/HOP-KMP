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
var UsersService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.UsersService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
const client_1 = require("@prisma/client");
const libphonenumber_js_1 = require("libphonenumber-js");
let UsersService = UsersService_1 = class UsersService {
    prisma;
    logger = new common_1.Logger(UsersService_1.name);
    constructor(prisma) {
        this.prisma = prisma;
    }
    async findByPhone(phone) {
        return this.prisma.user.findUnique({ where: { phone } });
    }
    async findByEmail(email) {
        return this.prisma.user.findUnique({ where: { email } });
    }
    async findById(id) {
        return this.prisma.user.findUnique({ where: { id } });
    }
    async createProfile(supabaseId, data) {
        if (data.phone) {
            if (!(0, libphonenumber_js_1.isValidPhoneNumber)(data.phone)) {
                throw new common_1.BadRequestException('Phone number must be in international format, e.g. +45 20 12 34 56');
            }
            data.phone = (0, libphonenumber_js_1.parsePhoneNumber)(data.phone).format('E.164');
        }
        if (data.phone) {
            const existing = await this.prisma.user.findUnique({ where: { phone: data.phone } });
            if (existing && existing.id !== supabaseId) {
                throw new common_1.ConflictException('Phone number already in use');
            }
        }
        if (data.email) {
            const existing = await this.prisma.user.findUnique({ where: { email: data.email } });
            if (existing && existing.id !== supabaseId) {
                throw new common_1.ConflictException('Email already in use');
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
            });
        }
        catch (e) {
            if (e instanceof client_1.Prisma.PrismaClientKnownRequestError && e.code === 'P2002') {
                const fields = e.meta?.target?.join(', ') ?? 'field';
                throw new common_1.ConflictException(`${fields} already in use`);
            }
            throw e;
        }
    }
    async markVerified(userId) {
        return this.prisma.user.update({
            where: { id: userId },
            data: { isVerified: true },
        });
    }
    async updateProfile(userId, data) {
        return this.prisma.user.update({
            where: { id: userId },
            data,
        });
    }
    async reportUser(reportedId, reporterId, reason) {
        this.logger.log(`User ${reporterId} reported ${reportedId}: ${reason}`);
    }
    async getCarDetails(userId) {
        return this.prisma.carDetails.findUnique({ where: { userId } });
    }
    async saveCarDetails(userId, data) {
        const [carDetails] = await this.prisma.$transaction([
            this.prisma.carDetails.upsert({
                where: { userId },
                create: { userId, ...data },
                update: data,
            }),
            this.prisma.user.update({
                where: { id: userId },
                data: {
                    role: { set: 'DRIVER' },
                },
            }),
        ]);
        return carDetails;
    }
    async completedTripCount(userId) {
        const [asDriver, asPassenger] = await Promise.all([
            this.prisma.trip.count({ where: { driverId: userId, status: 'COMPLETED' } }),
            this.prisma.booking.count({
                where: {
                    passengerId: userId,
                    status: 'CONFIRMED',
                    trip: { status: 'COMPLETED' },
                },
            }),
        ]);
        return asDriver + asPassenger;
    }
};
exports.UsersService = UsersService;
exports.UsersService = UsersService = UsersService_1 = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], UsersService);
//# sourceMappingURL=users.service.js.map