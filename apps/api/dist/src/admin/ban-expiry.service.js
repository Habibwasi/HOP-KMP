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
var BanExpiryService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.BanExpiryService = void 0;
const common_1 = require("@nestjs/common");
const schedule_1 = require("@nestjs/schedule");
const prisma_service_1 = require("../prisma/prisma.service");
let BanExpiryService = BanExpiryService_1 = class BanExpiryService {
    prisma;
    logger = new common_1.Logger(BanExpiryService_1.name);
    constructor(prisma) {
        this.prisma = prisma;
    }
    async liftExpiredBans() {
        const result = await this.prisma.user.updateMany({
            where: {
                isBanned: true,
                banExpiresAt: { lte: new Date() },
            },
            data: {
                isBanned: false,
                banExpiresAt: null,
            },
        });
        if (result.count > 0) {
            this.logger.log(`Lifted ${result.count} expired ban(s)`);
        }
    }
};
exports.BanExpiryService = BanExpiryService;
__decorate([
    (0, schedule_1.Cron)(schedule_1.CronExpression.EVERY_10_MINUTES),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", Promise)
], BanExpiryService.prototype, "liftExpiredBans", null);
exports.BanExpiryService = BanExpiryService = BanExpiryService_1 = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], BanExpiryService);
//# sourceMappingURL=ban-expiry.service.js.map