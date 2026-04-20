import { Processor, WorkerHost } from '@nestjs/bullmq'
import { Job } from 'bullmq'
import { BookingsService } from './bookings.service'

@Processor('bookings')
export class BookingsProcessor extends WorkerHost {
  constructor(private bookings: BookingsService) {
    super()
  }

  async process(job: Job) {
    if (job.name === 'check-threshold') {
      await this.bookings.checkModelBThreshold(job.data.tripId)
    }
  }
}
