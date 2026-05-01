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
const config_1 = require("@nestjs/config");
const prisma_service_1 = require("../prisma/prisma.service");
let PlacesService = class PlacesService {
    prisma;
    config;
    constructor(prisma, config) {
        this.prisma = prisma;
        this.config = config;
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
    async geocode(address) {
        const key = this.config.get('GOOGLE_MAPS_API_KEY');
        if (!key)
            throw new common_1.BadRequestException('Geocoding is not configured on this server');
        const url = `https://maps.googleapis.com/maps/api/geocode/json?address=${encodeURIComponent(address)}&key=${key}`;
        const res = await fetch(url);
        if (!res.ok)
            throw new common_1.BadRequestException('Geocoding request failed');
        const json = await res.json();
        if (json.status !== 'OK' || !json.results.length) {
            throw new common_1.BadRequestException(`No geocoding result for: ${address}`);
        }
        const { lat, lng } = json.results[0].geometry.location;
        return { lat, lng, formattedAddress: json.results[0].formatted_address };
    }
};
exports.PlacesService = PlacesService;
exports.PlacesService = PlacesService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        config_1.ConfigService])
], PlacesService);
//# sourceMappingURL=places.service.js.map