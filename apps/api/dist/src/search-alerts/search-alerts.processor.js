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
var SearchAlertsProcessor_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.SearchAlertsProcessor = exports.MATCH_ALERTS_JOB = exports.SEARCH_ALERTS_QUEUE = void 0;
const bullmq_1 = require("@nestjs/bullmq");
const common_1 = require("@nestjs/common");
const search_alerts_service_1 = require("./search-alerts.service");
exports.SEARCH_ALERTS_QUEUE = 'search-alerts';
exports.MATCH_ALERTS_JOB = 'match-alerts';
let SearchAlertsProcessor = SearchAlertsProcessor_1 = class SearchAlertsProcessor extends bullmq_1.WorkerHost {
    searchAlertsService;
    logger = new common_1.Logger(SearchAlertsProcessor_1.name);
    constructor(searchAlertsService) {
        super();
        this.searchAlertsService = searchAlertsService;
    }
    async process(job) {
        this.logger.debug(`Processing ${job.name} for trip ${job.data.tripId}`);
        await this.searchAlertsService.matchAndNotify({
            id: job.data.tripId,
            originAddress: job.data.originAddress,
            destAddress: job.data.destAddress,
        });
    }
};
exports.SearchAlertsProcessor = SearchAlertsProcessor;
exports.SearchAlertsProcessor = SearchAlertsProcessor = SearchAlertsProcessor_1 = __decorate([
    (0, bullmq_1.Processor)(exports.SEARCH_ALERTS_QUEUE),
    __metadata("design:paramtypes", [search_alerts_service_1.SearchAlertsService])
], SearchAlertsProcessor);
//# sourceMappingURL=search-alerts.processor.js.map