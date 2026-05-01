import { OnModuleInit } from '@nestjs/common';
import { Queue } from 'bullmq';
export { BOOKINGS_QUEUE, CHECK_THRESHOLD_JOB } from './trips.constants';
export declare class TripsModule implements OnModuleInit {
    private tripsQueue;
    constructor(tripsQueue: Queue);
    onModuleInit(): Promise<void>;
}
