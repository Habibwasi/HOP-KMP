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
exports.SettlementsController = void 0;
const common_1 = require("@nestjs/common");
const supabase_guard_1 = require("../auth/supabase.guard");
const settlements_service_1 = require("./settlements.service");
const dispute_dto_1 = require("./dto/dispute.dto");
let SettlementsController = class SettlementsController {
    settlements;
    constructor(settlements) {
        this.settlements = settlements;
    }
    get(bookingId, req) {
        return this.settlements.getSettlement(bookingId, req.user.id);
    }
    markPaid(bookingId, req) {
        return this.settlements.markPassengerPaid(bookingId, req.user.id);
    }
    confirmReceived(bookingId, req) {
        return this.settlements.markDriverConfirmed(bookingId, req.user.id);
    }
    dispute(bookingId, dto, req) {
        return this.settlements.dispute(bookingId, req.user.id, dto.reason);
    }
};
exports.SettlementsController = SettlementsController;
__decorate([
    (0, common_1.Get)(':bookingId'),
    __param(0, (0, common_1.Param)('bookingId')),
    __param(1, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object]),
    __metadata("design:returntype", void 0)
], SettlementsController.prototype, "get", null);
__decorate([
    (0, common_1.Post)(':bookingId/mark-paid'),
    (0, common_1.HttpCode)(200),
    __param(0, (0, common_1.Param)('bookingId')),
    __param(1, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object]),
    __metadata("design:returntype", void 0)
], SettlementsController.prototype, "markPaid", null);
__decorate([
    (0, common_1.Post)(':bookingId/confirm-received'),
    (0, common_1.HttpCode)(200),
    __param(0, (0, common_1.Param)('bookingId')),
    __param(1, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object]),
    __metadata("design:returntype", void 0)
], SettlementsController.prototype, "confirmReceived", null);
__decorate([
    (0, common_1.Post)(':bookingId/dispute'),
    (0, common_1.HttpCode)(200),
    __param(0, (0, common_1.Param)('bookingId')),
    __param(1, (0, common_1.Body)()),
    __param(2, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, dispute_dto_1.DisputeDto, Object]),
    __metadata("design:returntype", void 0)
], SettlementsController.prototype, "dispute", null);
exports.SettlementsController = SettlementsController = __decorate([
    (0, common_1.Controller)('settlements'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    __metadata("design:paramtypes", [settlements_service_1.SettlementsService])
], SettlementsController);
//# sourceMappingURL=settlements.controller.js.map