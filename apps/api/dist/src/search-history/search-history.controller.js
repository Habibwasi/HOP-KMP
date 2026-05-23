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
exports.SearchHistoryController = exports.RecordSearchDto = void 0;
const common_1 = require("@nestjs/common");
const class_validator_1 = require("class-validator");
const class_transformer_1 = require("class-transformer");
const supabase_guard_1 = require("../auth/supabase.guard");
const search_history_service_1 = require("./search-history.service");
class RecordSearchDto {
    originLabel;
    destLabel;
}
exports.RecordSearchDto = RecordSearchDto;
__decorate([
    (0, class_validator_1.IsString)(),
    (0, class_validator_1.MinLength)(1),
    (0, class_validator_1.MaxLength)(120),
    __metadata("design:type", String)
], RecordSearchDto.prototype, "originLabel", void 0);
__decorate([
    (0, class_validator_1.IsString)(),
    (0, class_validator_1.MinLength)(1),
    (0, class_validator_1.MaxLength)(120),
    __metadata("design:type", String)
], RecordSearchDto.prototype, "destLabel", void 0);
class ListQueryDto {
    limit;
}
__decorate([
    (0, class_validator_1.IsOptional)(),
    (0, class_transformer_1.Type)(() => Number),
    (0, class_validator_1.IsInt)(),
    (0, class_validator_1.Min)(1),
    (0, class_validator_1.Max)(20),
    __metadata("design:type", Number)
], ListQueryDto.prototype, "limit", void 0);
let SearchHistoryController = class SearchHistoryController {
    history;
    constructor(history) {
        this.history = history;
    }
    list(req, q) {
        return this.history.list(req.user.id, q.limit ?? 5);
    }
    record(req, dto) {
        return this.history.record(req.user.id, dto.originLabel, dto.destLabel);
    }
    remove(req, id) {
        return this.history.remove(req.user.id, id);
    }
};
exports.SearchHistoryController = SearchHistoryController;
__decorate([
    (0, common_1.Get)(),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Query)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, ListQueryDto]),
    __metadata("design:returntype", void 0)
], SearchHistoryController.prototype, "list", null);
__decorate([
    (0, common_1.Post)(),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, RecordSearchDto]),
    __metadata("design:returntype", void 0)
], SearchHistoryController.prototype, "record", null);
__decorate([
    (0, common_1.Delete)(':id'),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Param)('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String]),
    __metadata("design:returntype", void 0)
], SearchHistoryController.prototype, "remove", null);
exports.SearchHistoryController = SearchHistoryController = __decorate([
    (0, common_1.Controller)('search/recent'),
    (0, common_1.UseGuards)(supabase_guard_1.SupabaseGuard),
    __metadata("design:paramtypes", [search_history_service_1.SearchHistoryService])
], SearchHistoryController);
//# sourceMappingURL=search-history.controller.js.map