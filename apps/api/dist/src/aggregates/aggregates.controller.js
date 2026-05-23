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
Object.defineProperty(exports, "__esModule", { value: true });
exports.AggregatesController = void 0;
const common_1 = require("@nestjs/common");
const supabase_guard_1 = require("../auth/supabase.guard");
const aggregates_service_1 = require("./aggregates.service");
let AggregatesController = class AggregatesController {
    aggregates;
    constructor(aggregates) {
        this.aggregates = aggregates;
    }
    async driverEarningsSeries(req, daysStr) {
        const days = daysStr ? parseInt(daysStr, 10) : 7;
        if (isNaN(days) || days < 1 || days > 30) {
            throw new common_1.BadRequestException('days must be between 1 and 30');
        }
        return this.aggregates.getDriverEarningsSeries(req.user.id, days);
    }
    async popularRoutes(limitStr) {
        const limit = limitStr ? parseInt(limitStr, 10) : 5;
        if (isNaN(limit) || limit < 1 || limit > 50) {
            throw new common_1.BadRequestException('limit must be between 1 and 50');
        }
        return this.aggregates.getPopularRoutes(limit);
    }
    async demandHotspots() {
        return this.aggregates.getDemandHotspots();
    }
    async co2Saved(req) {
        return this.aggregates.getUserCo2Saved(req.user.id);
    }
};
exports.AggregatesController = AggregatesController;
__decorate([
    (0, common_1.Get)('driver/earnings-series'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Query)('days')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String]),
    __metadata("design:returntype", Promise)
], AggregatesController.prototype, "driverEarningsSeries", null);
__decorate([
    (0, common_1.Get)('popular-routes'),
    __param(0, (0, common_1.Query)('limit')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", Promise)
], AggregatesController.prototype, "popularRoutes", null);
__decorate([
    (0, common_1.Get)('demand-hotspots'),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", Promise)
], AggregatesController.prototype, "demandHotspots", null);
__decorate([
    (0, common_1.Get)('co2-saved'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    __param(0, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], AggregatesController.prototype, "co2Saved", null);
exports.AggregatesController = AggregatesController = __decorate([
    (0, common_1.Controller)('aggregates'),
    __metadata("design:paramtypes", [aggregates_service_1.AggregatesService])
], AggregatesController);
//# sourceMappingURL=aggregates.controller.js.map