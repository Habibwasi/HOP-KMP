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
exports.TripsModule = exports.CHECK_THRESHOLD_JOB = exports.BOOKINGS_QUEUE = void 0;
const common_1 = require("@nestjs/common");
const bullmq_1 = require("@nestjs/bullmq");
const bullmq_2 = require("@nestjs/bullmq");
const bullmq_3 = require("bullmq");
const trips_service_1 = require("./trips.service");
const trips_controller_1 = require("./trips.controller");
const pricing_service_1 = require("./pricing.service");
const trips_processor_1 = require("./trips.processor");
const search_alerts_processor_1 = require("../search-alerts/search-alerts.processor");
const trips_constants_1 = require("./trips.constants");
const bookings_module_1 = require("../bookings/bookings.module");
const payments_module_1 = require("../payments/payments.module");
var trips_constants_2 = require("./trips.constants");
Object.defineProperty(exports, "BOOKINGS_QUEUE", { enumerable: true, get: function () { return trips_constants_2.BOOKINGS_QUEUE; } });
Object.defineProperty(exports, "CHECK_THRESHOLD_JOB", { enumerable: true, get: function () { return trips_constants_2.CHECK_THRESHOLD_JOB; } });
let TripsModule = class TripsModule {
    tripsQueue;
    constructor(tripsQueue) {
        this.tripsQueue = tripsQueue;
    }
    async onModuleInit() {
        await this.tripsQueue.add(trips_processor_1.EXTEND_RECURRING_JOB, {}, {
            repeat: { pattern: '0 3 * * *' },
            jobId: 'extend-recurring-daily',
            removeOnComplete: true,
        });
    }
};
exports.TripsModule = TripsModule;
exports.TripsModule = TripsModule = __decorate([
    (0, common_1.Module)({
        imports: [
            bullmq_2.BullModule.registerQueue({ name: search_alerts_processor_1.SEARCH_ALERTS_QUEUE }),
            bullmq_2.BullModule.registerQueue({ name: trips_constants_1.BOOKINGS_QUEUE }),
            bullmq_2.BullModule.registerQueue({ name: trips_processor_1.TRIPS_QUEUE }),
            (0, common_1.forwardRef)(() => bookings_module_1.BookingsModule),
            (0, common_1.forwardRef)(() => payments_module_1.PaymentsModule),
        ],
        providers: [trips_service_1.TripsService, pricing_service_1.PricingService, trips_processor_1.TripsProcessor],
        controllers: [trips_controller_1.TripsController],
        exports: [trips_service_1.TripsService],
    }),
    __param(0, (0, bullmq_1.InjectQueue)(trips_processor_1.TRIPS_QUEUE)),
    __metadata("design:paramtypes", [bullmq_3.Queue])
], TripsModule);
//# sourceMappingURL=trips.module.js.map