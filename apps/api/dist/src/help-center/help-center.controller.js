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
exports.HelpCenterController = void 0;
const common_1 = require("@nestjs/common");
const help_center_service_1 = require("./help-center.service");
let HelpCenterController = class HelpCenterController {
    helpCenter;
    constructor(helpCenter) {
        this.helpCenter = helpCenter;
    }
    getFaqs() {
        return this.helpCenter.getFaqs();
    }
};
exports.HelpCenterController = HelpCenterController;
__decorate([
    (0, common_1.Get)('faqs'),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], HelpCenterController.prototype, "getFaqs", null);
exports.HelpCenterController = HelpCenterController = __decorate([
    (0, common_1.Controller)('help-center'),
    __metadata("design:paramtypes", [help_center_service_1.HelpCenterService])
], HelpCenterController);
//# sourceMappingURL=help-center.controller.js.map