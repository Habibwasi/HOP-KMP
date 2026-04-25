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
Object.defineProperty(exports, "__esModule", { value: true });
exports.PlacesService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
let PlacesService = class PlacesService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    listForUser(userId) {
        return this.prisma.savedPlace.findMany({
            where: { userId },
            orderBy: [{ kind: 'asc' }, { createdAt: 'asc' }],
        });
    }
    create(userId, dto) {
        return this.prisma.savedPlace.upsert({
            where: { userId_label: { userId, label: dto.label } },
            create: {
                userId,
                label: dto.label,
                address: dto.address,
                lat: dto.lat,
                lng: dto.lng,
                kind: dto.kind ?? 'CUSTOM',
            },
            update: {
                address: dto.address,
                lat: dto.lat,
                lng: dto.lng,
                kind: dto.kind ?? 'CUSTOM',
            },
        });
    }
    async update(userId, id, dto) {
        const existing = await this.prisma.savedPlace.findUnique({ where: { id } });
        if (!existing)
            throw new common_1.NotFoundException('Place not found');
        if (existing.userId !== userId)
            throw new common_1.ForbiddenException();
        return this.prisma.savedPlace.update({
            where: { id },
            data: {
                label: dto.label,
                address: dto.address,
                lat: dto.lat,
                lng: dto.lng,
                kind: dto.kind ?? existing.kind,
            },
        });
    }
    async remove(userId, id) {
        const existing = await this.prisma.savedPlace.findUnique({ where: { id } });
        if (!existing)
            throw new common_1.NotFoundException('Place not found');
        if (existing.userId !== userId)
            throw new common_1.ForbiddenException();
        await this.prisma.savedPlace.delete({ where: { id } });
        return { ok: true };
    }
};
exports.PlacesService = PlacesService;
exports.PlacesService = PlacesService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], PlacesService);
//# sourceMappingURL=places.service.js.map