"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.SearchAlertsModule = void 0;
const common_1 = require("@nestjs/common");
const bullmq_1 = require("@nestjs/bullmq");
const prisma_module_1 = require("../prisma/prisma.module");
const auth_module_1 = require("../auth/auth.module");
const notifications_module_1 = require("../notifications/notifications.module");
const search_alerts_controller_1 = require("./search-alerts.controller");
const search_alerts_service_1 = require("./search-alerts.service");
const search_alerts_processor_1 = require("./search-alerts.processor");
let SearchAlertsModule = class SearchAlertsModule {
};
exports.SearchAlertsModule = SearchAlertsModule;
exports.SearchAlertsModule = SearchAlertsModule = __decorate([
    (0, common_1.Module)({
        imports: [
            prisma_module_1.PrismaModule,
            auth_module_1.AuthModule,
            notifications_module_1.NotificationsModule,
            bullmq_1.BullModule.registerQueue({ name: search_alerts_processor_1.SEARCH_ALERTS_QUEUE }),
        ],
        controllers: [search_alerts_controller_1.SearchAlertsController],
        providers: [search_alerts_service_1.SearchAlertsService, search_alerts_processor_1.SearchAlertsProcessor],
        exports: [search_alerts_service_1.SearchAlertsService, bullmq_1.BullModule],
    })
], SearchAlertsModule);
//# sourceMappingURL=search-alerts.module.js.map