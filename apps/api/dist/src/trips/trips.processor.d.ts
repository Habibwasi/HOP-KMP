import { WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { TripsService } from './trips.service';
export declare const TRIPS_QUEUE = "trips";
export declare const EXTEND_RECURRING_JOB = "extend-recurring";
export declare class TripsProcessor extends WorkerHost {
    private trips;
    constructor(trips: TripsService);
    process(job: Job): Promise<void>;
}
