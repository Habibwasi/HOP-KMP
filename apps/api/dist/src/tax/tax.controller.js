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
exports.TaxController = void 0;
const common_1 = require("@nestjs/common");
const supabase_guard_1 = require("../auth/supabase.guard");
const tax_service_1 = require("./tax.service");
let TaxController = class TaxController {
    tax;
    constructor(tax) {
        this.tax = tax;
    }
    allRecords(req) {
        return this.tax.getAllRecords(req.user.id);
    }
    monthly(req, year, month) {
        return this.tax.getMonthlyDashboard(req.user.id, year, month);
    }
    annual(req, year) {
        return this.tax.getAnnualSummary(req.user.id, year);
    }
};
exports.TaxController = TaxController;
__decorate([
    (0, common_1.Get)('records'),
    __param(0, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", void 0)
], TaxController.prototype, "allRecords", null);
__decorate([
    (0, common_1.Get)(':year/:month'),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Param)('year', common_1.ParseIntPipe)),
    __param(2, (0, common_1.Param)('month', common_1.ParseIntPipe)),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, Number, Number]),
    __metadata("design:returntype", void 0)
], TaxController.prototype, "monthly", null);
__decorate([
    (0, common_1.Get)(':year'),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Param)('year', common_1.ParseIntPipe)),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, Number]),
    __metadata("design:returntype", void 0)
], TaxController.prototype, "annual", null);
exports.TaxController = TaxController = __decorate([
    (0, common_1.Controller)('tax'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    __metadata("design:paramtypes", [tax_service_1.TaxService])
], TaxController);
//# sourceMappingURL=tax.controller.js.map