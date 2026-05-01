"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.TripsModule = void 0;
const common_1 = require("@nestjs/common");
const bullmq_1 = require("@nestjs/bullmq");
const trips_service_1 = require("./trips.service");
const trips_controller_1 = require("./trips.controller");
const pricing_service_1 = require("./pricing.service");
const search_alerts_processor_1 = require("../search-alerts/search-alerts.processor");
let TripsModule = class TripsModule {
};
exports.TripsModule = TripsModule;
exports.TripsModule = TripsModule = __decorate([
    (0, common_1.Module)({
        imports: [bullmq_1.BullModule.registerQueue({ name: search_alerts_processor_1.SEARCH_ALERTS_QUEUE })],
        providers: [trips_service_1.TripsService, pricing_service_1.PricingService],
        controllers: [trips_controller_1.TripsController],
        exports: [trips_service_1.TripsService],
    })
], TripsModule);
//# sourceMappingURL=trips.module.js.map