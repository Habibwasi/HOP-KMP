import { WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { BookingsService } from './bookings.service';
export declare class BookingsProcessor extends WorkerHost {
    private bookings;
    constructor(bookings: BookingsService);
    process(job: Job): Promise<void>;
}
