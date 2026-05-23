import { Processor, WorkerHost } from '@nestjs/bullmq'
import { Job } from 'bullmq'
import { TripsService } from './trips.service'

export const TRIPS_QUEUE = 'trips'
export const EXTEND_RECURRING_JOB = 'extend-recurring'

@Processor(TRIPS_QUEUE)
export class TripsProcessor extends WorkerHost {
  constructor(private trips: TripsService) {
    super()
  }

  async process(job: Job) {
    if (job.name === EXTEND_RECURRING_JOB) {
      await this.trips.extendRecurringWindow()
    }
  }
}
