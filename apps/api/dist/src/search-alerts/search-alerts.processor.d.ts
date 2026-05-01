import { WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { SearchAlertsService } from './search-alerts.service';
export declare const SEARCH_ALERTS_QUEUE = "search-alerts";
export declare const MATCH_ALERTS_JOB = "match-alerts";
export interface MatchAlertsJobData {
    tripId: string;
    originAddress: string;
    destAddress: string;
}
export declare class SearchAlertsProcessor extends WorkerHost {
    private readonly searchAlertsService;
    private readonly logger;
    constructor(searchAlertsService: SearchAlertsService);
    process(job: Job<MatchAlertsJobData>): Promise<void>;
}
