import { Injectable, Logger } from '@nestjs/common'
import { ConfigService } from '@nestjs/config'
import { randomBytes } from 'node:crypto'
import { PrismaService } from '../prisma/prisma.service'
import { MailService } from '../mail/mail.service'
import { AppException } from '../common/errors/app-exception'
import { ApiErrorCode } from '../common/errors/api-error-codes'
import { JoinWaitlistDto } from './dto/join-waitlist.dto'
import { buildWaitlistEmail, buildWaitlistSubject, WaitlistLocale } from './waitlist-email.template'

const IP_WINDOW_MS = 10 * 60 * 1000
const IP_MAX_REQUESTS = 5
/** Minimum gap between confirmation emails to the same address. */
const RESEND_COOLDOWN_MS = 10 * 60 * 1000

export interface RouteDemand {
  origin: string
  dest: string
  passengers: number
  drivers: number
  total: number
}

@Injectable()
export class WaitlistService {
  private readonly logger = new Logger(WaitlistService.name)
  /** In-memory per-IP limiter — fine for a single API instance on Railway. */
  private readonly hits = new Map<string, number[]>()

  constructor(
    private readonly prisma: PrismaService,
    private readonly mail: MailService,
    private readonly config: ConfigService,
  ) {}

  /**
   * Always resolves the same way for new, pending, and confirmed emails so the
   * endpoint can't be used to check whether an address is on the list.
   */
  async join(dto: JoinWaitlistDto, ip: string): Promise<{ ok: true }> {
    this.checkRateLimit(ip)
    if (dto.website) return { ok: true }

    const locale: WaitlistLocale = dto.locale ?? 'da'
    const now = new Date()
    const existing = await this.prisma.waitlistSignup.findUnique({ where: { email: dto.email } })

    if (existing?.confirmedAt) return { ok: true }

    const fields = {
      role: dto.role,
      origin: dto.origin || null,
      dest: dto.dest || null,
      locale,
      source: dto.source ?? null,
      consentAt: now,
    }

    const signup = existing
      ? await this.prisma.waitlistSignup.update({ where: { id: existing.id }, data: fields })
      : await this.prisma.waitlistSignup.create({
          data: { ...fields, email: dto.email, token: randomBytes(32).toString('hex') },
        })

    const lastSent = signup.confirmSentAt?.getTime() ?? 0
    if (now.getTime() - lastSent < RESEND_COOLDOWN_MS) return { ok: true }

    try {
      await this.sendConfirmation(signup.email, signup.token, locale)
      await this.prisma.waitlistSignup.update({
        where: { id: signup.id },
        data: { confirmSentAt: now },
      })
    } catch (err: unknown) {
      // Leave confirmSentAt unset so a resubmit retries the email.
      this.logger.error(`Waitlist confirmation email failed: ${err instanceof Error ? err.message : String(err)}`)
    }
    return { ok: true }
  }

  /** Returns the signup's locale, or null if the token is unknown. */
  async confirm(token: string): Promise<WaitlistLocale | null> {
    const signup = await this.prisma.waitlistSignup.findUnique({ where: { token } })
    if (!signup) return null
    if (!signup.confirmedAt) {
      await this.prisma.waitlistSignup.update({
        where: { id: signup.id },
        data: { confirmedAt: new Date() },
      })
    }
    return signup.locale === 'en' ? 'en' : 'da'
  }

  /** Hard-deletes the signup (GDPR erasure). Returns its locale, or null if unknown. */
  async unsubscribe(token: string): Promise<WaitlistLocale | null> {
    const signup = await this.prisma.waitlistSignup.findUnique({ where: { token } })
    if (!signup) return null
    await this.prisma.waitlistSignup.delete({ where: { id: signup.id } })
    return signup.locale === 'en' ? 'en' : 'da'
  }

  /** Confirmed signups by role, plus route demand grouped case-insensitively. */
  async stats() {
    const confirmed = { confirmedAt: { not: null } }
    const [byRole, pending, routeRows] = await Promise.all([
      this.prisma.waitlistSignup.groupBy({ by: ['role'], where: confirmed, _count: { _all: true } }),
      this.prisma.waitlistSignup.count({ where: { confirmedAt: null } }),
      this.prisma.waitlistSignup.groupBy({
        by: ['origin', 'dest', 'role'],
        where: { ...confirmed, origin: { not: null }, dest: { not: null } },
        _count: { _all: true },
      }),
    ])

    const routes = new Map<string, RouteDemand>()
    for (const row of routeRows) {
      const key = `${row.origin!.toLowerCase()}|${row.dest!.toLowerCase()}`
      const route = routes.get(key) ?? { origin: row.origin!, dest: row.dest!, passengers: 0, drivers: 0, total: 0 }
      const n = row._count._all
      if (row.role !== 'DRIVER') route.passengers += n
      if (row.role !== 'PASSENGER') route.drivers += n
      route.total += n
      routes.set(key, route)
    }

    return {
      confirmedByRole: Object.fromEntries(byRole.map((r) => [r.role, r._count._all])),
      pendingConfirmation: pending,
      topRoutes: [...routes.values()].sort((a, b) => b.total - a.total).slice(0, 50),
    }
  }

  websiteUrl(locale: WaitlistLocale, status: 'confirmed' | 'removed' | 'invalid'): string {
    const base = this.config.get<string>('WEBSITE_URL', 'https://ridly.dk').replace(/\/$/, '')
    return `${base}${locale === 'en' ? '/en/' : '/'}?waitlist=${status}`
  }

  private async sendConfirmation(email: string, token: string, locale: WaitlistLocale) {
    const apiBase = this.config.get<string>('API_BASE_URL', 'https://hop-kmp-production.up.railway.app')
    const link = (action: string) => `${apiBase}/api/v1/waitlist/${action}?token=${token}`
    await this.mail.sendWithFallback({
      to: email,
      subject: buildWaitlistSubject(locale),
      html: buildWaitlistEmail({
        locale,
        confirmUrl: link('confirm'),
        unsubscribeUrl: link('unsubscribe'),
      }),
    })
  }

  private checkRateLimit(ip: string) {
    const now = Date.now()
    const recent = (this.hits.get(ip) ?? []).filter((t) => now - t < IP_WINDOW_MS)
    if (recent.length >= IP_MAX_REQUESTS) throw new AppException(ApiErrorCode.TOO_MANY_REQUESTS)
    recent.push(now)
    this.hits.set(ip, recent)
    if (this.hits.size > 10_000) this.pruneHits(now)
  }

  private pruneHits(now: number) {
    for (const [ip, times] of this.hits) {
      if (times.every((t) => now - t >= IP_WINDOW_MS)) this.hits.delete(ip)
    }
  }
}
