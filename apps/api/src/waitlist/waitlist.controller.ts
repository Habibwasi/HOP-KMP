import { Body, Controller, Get, HttpCode, Post, Query, Req, Res, UseGuards } from '@nestjs/common'
import type { Request, Response } from 'express'
import { SupabaseGuard } from '../auth/supabase.guard'
import { AdminGuard } from '../admin/guards/admin.guard'
import { JoinWaitlistDto } from './dto/join-waitlist.dto'
import { WaitlistService } from './waitlist.service'

/** First hop in X-Forwarded-For is the visitor (Vercel → Railway both append). */
function clientIp(req: Request): string {
  const forwarded = req.headers['x-forwarded-for']
  const first = (Array.isArray(forwarded) ? forwarded[0] : forwarded)?.split(',')[0]?.trim()
  return first || req.ip || 'unknown'
}

@Controller('waitlist')
export class WaitlistController {
  constructor(private readonly waitlist: WaitlistService) {}

  /** POST /waitlist — public. Called by ridly.dk through a Vercel rewrite. */
  @Post()
  @HttpCode(202)
  join(@Body() dto: JoinWaitlistDto, @Req() req: Request) {
    return this.waitlist.join(dto, clientIp(req))
  }

  /** GET /waitlist/confirm?token= — link from the confirmation email. */
  @Get('confirm')
  async confirm(@Query('token') token: string, @Res() res: Response) {
    const locale = token ? await this.waitlist.confirm(token) : null
    res.redirect(302, this.waitlist.websiteUrl(locale ?? 'da', locale ? 'confirmed' : 'invalid'))
  }

  /** GET /waitlist/unsubscribe?token= — deletes the signup. */
  @Get('unsubscribe')
  async unsubscribe(@Query('token') token: string, @Res() res: Response) {
    const locale = token ? await this.waitlist.unsubscribe(token) : null
    res.redirect(302, this.waitlist.websiteUrl(locale ?? 'da', locale ? 'removed' : 'invalid'))
  }

  /** GET /waitlist/stats — admin only. Where passengers and drivers overlap. */
  @Get('stats')
  @UseGuards(SupabaseGuard, AdminGuard)
  stats() {
    return this.waitlist.stats()
  }
}
