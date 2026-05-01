import { Processor, WorkerHost } from '@nestjs/bullmq'
import { Job } from 'bullmq'
import { Logger } from '@nestjs/common'
import { SearchAlertsService } from './search-alerts.service'

export const SEARCH_ALERTS_QUEUE = 'search-alerts'
export const MATCH_ALERTS_JOB = 'match-alerts'

export interface MatchAlertsJobData {
  tripId: string
  originAddress: string
  destAddress: string
}

@Processor(SEARCH_ALERTS_QUEUE)
export class SearchAlertsProcessor extends WorkerHost {
  private readonly logger = new Logger(SearchAlertsProcessor.name)

  constructor(private readonly searchAlertsService: SearchAlertsService) {
    super()
  }

  async process(job: Job<MatchAlertsJobData>) {
    this.logger.debug(`Processing ${job.name} for trip ${job.data.tripId}`)
    await this.searchAlertsService.matchAndNotify({
      id: job.data.tripId,
      originAddress: job.data.originAddress,
      destAddress: job.data.destAddress,
    })
  }
}
