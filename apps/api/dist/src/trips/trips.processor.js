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
exports.TripsProcessor = exports.EXTEND_RECURRING_JOB = exports.TRIPS_QUEUE = void 0;
const bullmq_1 = require("@nestjs/bullmq");
const trips_service_1 = require("./trips.service");
exports.TRIPS_QUEUE = 'trips';
exports.EXTEND_RECURRING_JOB = 'extend-recurring';
let TripsProcessor = class TripsProcessor extends bullmq_1.WorkerHost {
    trips;
    constructor(trips) {
        super();
        this.trips = trips;
    }
    async process(job) {
        if (job.name === exports.EXTEND_RECURRING_JOB) {
            await this.trips.extendRecurringWindow();
        }
    }
};
exports.TripsProcessor = TripsProcessor;
exports.TripsProcessor = TripsProcessor = __decorate([
    (0, bullmq_1.Processor)(exports.TRIPS_QUEUE),
    __metadata("design:paramtypes", [trips_service_1.TripsService])
], TripsProcessor);
//# sourceMappingURL=trips.processor.js.map