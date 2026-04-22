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
exports.AdminController = void 0;
const common_1 = require("@nestjs/common");
const supabase_guard_1 = require("../auth/supabase.guard");
const admin_guard_1 = require("./guards/admin.guard");
const admin_service_1 = require("./admin.service");
const class_validator_1 = require("class-validator");
const class_transformer_1 = require("class-transformer");
const common_2 = require("@nestjs/common");
class BanUserDto {
    durationDays;
    permanent;
}
__decorate([
    (0, class_validator_1.IsOptional)(),
    (0, class_transformer_1.Type)(() => Number),
    (0, class_validator_1.IsInt)(),
    (0, class_validator_1.Min)(1),
    (0, class_validator_1.Max)(365),
    __metadata("design:type", Number)
], BanUserDto.prototype, "durationDays", void 0);
__decorate([
    (0, class_validator_1.IsOptional)(),
    (0, class_validator_1.IsIn)(['permanent']),
    __metadata("design:type", String)
], BanUserDto.prototype, "permanent", void 0);
let AdminController = class AdminController {
    admin;
    constructor(admin) {
        this.admin = admin;
    }
    stats() {
        return this.admin.getDashboardStats();
    }
    users(page = 1, limit = 20) {
        return this.admin.listUsers(+page, +limit);
    }
    ban(id, dto) {
        const duration = dto.permanent === 'permanent' ? 'permanent' : (dto.durationDays ?? 7);
        return this.admin.banUser(id, duration);
    }
    unban(id) {
        return this.admin.unbanUser(id);
    }
    makeAdmin(id) {
        return this.admin.makeAdmin(id);
    }
    pendingLicences() {
        return this.admin.getPendingLicences();
    }
    reviewLicence(req, id, approved) {
        return this.admin.reviewLicence(id, req.user.id, approved);
    }
    trips(page = 1, limit = 20) {
        return this.admin.listTrips(+page, +limit);
    }
};
exports.AdminController = AdminController;
__decorate([
    (0, common_1.Get)('stats'),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AdminController.prototype, "stats", null);
__decorate([
    (0, common_1.Get)('users'),
    __param(0, (0, common_1.Query)('page')),
    __param(1, (0, common_1.Query)('limit')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, Object]),
    __metadata("design:returntype", void 0)
], AdminController.prototype, "users", null);
__decorate([
    (0, common_1.Patch)('users/:id/ban'),
    __param(0, (0, common_1.Param)('id')),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, BanUserDto]),
    __metadata("design:returntype", void 0)
], AdminController.prototype, "ban", null);
__decorate([
    (0, common_1.Patch)('users/:id/unban'),
    __param(0, (0, common_1.Param)('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", void 0)
], AdminController.prototype, "unban", null);
__decorate([
    (0, common_1.Patch)('users/:id/make-admin'),
    __param(0, (0, common_1.Param)('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", void 0)
], AdminController.prototype, "makeAdmin", null);
__decorate([
    (0, common_1.Get)('licences/pending'),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AdminController.prototype, "pendingLicences", null);
__decorate([
    (0, common_1.Post)('licences/:id/review'),
    __param(0, (0, common_2.Req)()),
    __param(1, (0, common_1.Param)('id')),
    __param(2, (0, common_1.Body)('approved')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String, Boolean]),
    __metadata("design:returntype", void 0)
], AdminController.prototype, "reviewLicence", null);
__decorate([
    (0, common_1.Get)('trips'),
    __param(0, (0, common_1.Query)('page')),
    __param(1, (0, common_1.Query)('limit')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, Object]),
    __metadata("design:returntype", void 0)
], AdminController.prototype, "trips", null);
exports.AdminController = AdminController = __decorate([
    (0, common_1.Controller)('admin'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard, admin_guard_1.AdminGuard),
    __metadata("design:paramtypes", [admin_service_1.AdminService])
], AdminController);
//# sourceMappingURL=admin.controller.js.map