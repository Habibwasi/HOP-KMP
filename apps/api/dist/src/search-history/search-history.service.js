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
var SearchHistoryService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.SearchHistoryService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
let SearchHistoryService = class SearchHistoryService {
    static { SearchHistoryService_1 = this; }
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    list(userId, limit) {
        return this.prisma.recentSearch.findMany({
            where: { userId },
            orderBy: { lastUsedAt: 'desc' },
            take: limit,
        });
    }
    static MAX_RECENT_SEARCHES = 10;
    async record(userId, originLabel, destLabel) {
        const row = await this.prisma.recentSearch.upsert({
            where: {
                userId_originLabel_destLabel: {
                    userId,
                    originLabel,
                    destLabel,
                },
            },
            create: { userId, originLabel, destLabel },
            update: {
                lastUsedAt: new Date(),
                useCount: { increment: 1 },
            },
        });
        const overflow = await this.prisma.recentSearch.findMany({
            where: { userId },
            orderBy: { lastUsedAt: 'desc' },
            skip: SearchHistoryService_1.MAX_RECENT_SEARCHES,
            select: { id: true },
        });
        if (overflow.length > 0) {
            await this.prisma.recentSearch.deleteMany({
                where: { id: { in: overflow.map((r) => r.id) } },
            });
        }
        return row;
    }
    async remove(userId, id) {
        const row = await this.prisma.recentSearch.findUnique({ where: { id } });
        if (!row)
            throw new common_1.NotFoundException('Search not found');
        if (row.userId !== userId)
            throw new common_1.ForbiddenException();
        await this.prisma.recentSearch.delete({ where: { id } });
        return { ok: true };
    }
};
exports.SearchHistoryService = SearchHistoryService;
exports.SearchHistoryService = SearchHistoryService = SearchHistoryService_1 = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], SearchHistoryService);
//# sourceMappingURL=search-history.service.js.map