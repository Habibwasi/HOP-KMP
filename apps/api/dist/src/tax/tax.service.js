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
exports.TaxService = void 0;
const common_1 = require("@nestjs/common");
const prisma_service_1 = require("../prisma/prisma.service");
let TaxService = class TaxService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    async getMonthlyDashboard(driverId, year, month) {
        const start = new Date(year, month - 1, 1);
        const end = new Date(year, month, 0, 23, 59, 59);
        const records = await this.prisma.taxRecord.findMany({
            where: {
                driverId,
                recordedAt: { gte: start, lte: end },
            },
            include: {
                trip: {
                    select: {
                        originAddress: true,
                        destAddress: true,
                        departureAt: true,
                        distanceKm: true,
                    },
                },
            },
            orderBy: { recordedAt: 'asc' },
        });
        const totalOere = records.reduce((sum, r) => sum + r.amountOere, 0);
        const totalKm = records.reduce((sum, r) => sum + r.distanceKm, 0);
        const taxableAmountOere = records.reduce((sum, r) => sum + Math.round(r.distanceKm * r.ratePerKm * 100), 0);
        return {
            year,
            month,
            totalTrips: records.length,
            totalEarnedOere: totalOere,
            totalKm: Math.round(totalKm * 10) / 10,
            taxableAmountOere,
            taxableAmountDkk: taxableAmountOere / 100,
            records,
        };
    }
    async getAnnualSummary(driverId, year) {
        const months = await Promise.all(Array.from({ length: 12 }, (_, i) => this.getMonthlyDashboard(driverId, year, i + 1)));
        const totalOere = months.reduce((sum, m) => sum + m.totalEarnedOere, 0);
        const totalKm = months.reduce((sum, m) => sum + m.totalKm, 0);
        const taxableOere = months.reduce((sum, m) => sum + m.taxableAmountOere, 0);
        return {
            year,
            totalTrips: months.reduce((sum, m) => sum + m.totalTrips, 0),
            totalEarnedOere: totalOere,
            totalEarnedDkk: totalOere / 100,
            totalKm: Math.round(totalKm * 10) / 10,
            taxableAmountOere: taxableOere,
            taxableAmountDkk: taxableOere / 100,
            months,
        };
    }
    async getAllRecords(driverId) {
        return this.prisma.taxRecord.findMany({
            where: { driverId },
            orderBy: { recordedAt: 'desc' },
            include: {
                trip: {
                    select: { originAddress: true, destAddress: true, departureAt: true },
                },
            },
        });
    }
};
exports.TaxService = TaxService;
exports.TaxService = TaxService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], TaxService);
//# sourceMappingURL=tax.service.js.map