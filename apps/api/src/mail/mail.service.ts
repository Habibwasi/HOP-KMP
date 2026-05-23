import { Injectable, Logger } from '@nestjs/common'
import { ConfigService } from '@nestjs/config'
import { Resend } from 'resend'

interface SendOptions {
  to: string
  subject: string
  html: string
}

interface ProviderResult {
  provider: string
  error: string
}

/**
 * Transactional email service with a three-provider fallback chain:
 *   1. Brevo   (primary)
 *   2. Resend  (secondary)
 *   3. MailerSend (tertiary)
 *
 * On failure each provider is skipped in order. If all three fail the
 * combined error is thrown so the caller (Supabase hook) receives a 5xx
 * and Supabase retries automatically.
 */
@Injectable()
export class MailService {
  private readonly logger = new Logger(MailService.name)
  private readonly fromAddress: string
  private readonly fromName: string
  private readonly resend: Resend

  constructor(private readonly config: ConfigService) {
    this.fromAddress = config.getOrThrow<string>('MAIL_FROM_ADDRESS')
    this.fromName = config.getOrThrow<string>('MAIL_FROM_NAME')
    this.resend = new Resend(config.get<string>('RESEND_API_KEY'))
  }

  // ── Public API ────────────────────────────────────────────────────────────

  async sendWithFallback(opts: SendOptions): Promise<void> {
    const failures: ProviderResult[] = []

    const providers: Array<{ name: string; fn: () => Promise<void> }> = [
      { name: 'Brevo',      fn: () => this.sendViaBrevo(opts) },
      { name: 'Resend',     fn: () => this.sendViaResend(opts) },
      { name: 'MailerSend', fn: () => this.sendViaMailerSend(opts) },
    ]

    for (const provider of providers) {
      try {
        await provider.fn()
        this.logger.log(`Email sent via ${provider.name} → ${opts.to}`)
        return
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err)
        this.logger.warn(`${provider.name} failed: ${message}`)
        failures.push({ provider: provider.name, error: message })
      }
    }

    throw new Error(
      `All email providers failed: ${JSON.stringify(failures)}`,
    )
  }

  // ── Brevo ─────────────────────────────────────────────────────────────────

  private async sendViaBrevo({ to, subject, html }: SendOptions): Promise<void> {
    const apiKey = this.config.get<string>('BREVO_API_KEY')
    if (!apiKey) throw new Error('BREVO_API_KEY not configured')

    const res = await fetch('https://api.brevo.com/v3/smtp/email', {
      method: 'POST',
      headers: {
        'api-key': apiKey,
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({
        sender: { name: this.fromName, email: this.fromAddress },
        to: [{ email: to }],
        subject,
        htmlContent: html,
      }),
    })

    if (!res.ok) {
      const body = await res.text()
      throw new Error(`Brevo HTTP ${res.status}: ${body}`)
    }
  }

  // ── Resend ────────────────────────────────────────────────────────────────

  private async sendViaResend({ to, subject, html }: SendOptions): Promise<void> {
    const apiKey = this.config.get<string>('RESEND_API_KEY')
    if (!apiKey) throw new Error('RESEND_API_KEY not configured')

    const { error } = await this.resend.emails.send({
      from: `${this.fromName} <${this.fromAddress}>`,
      to,
      subject,
      html,
    })

    if (error) throw new Error(`Resend error: ${error.message}`)
  }

  // ── MailerSend ────────────────────────────────────────────────────────────

  private async sendViaMailerSend({ to, subject, html }: SendOptions): Promise<void> {
    const apiKey = this.config.get<string>('MAILERSEND_API_KEY')
    if (!apiKey) throw new Error('MAILERSEND_API_KEY not configured')

    const res = await fetch('https://api.mailersend.com/v1/email', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${apiKey}`,
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({
        from: { email: this.fromAddress, name: this.fromName },
        to: [{ email: to }],
        subject,
        html,
      }),
    })

    // MailerSend returns 202 on success
    if (!res.ok) {
      const body = await res.text()
      throw new Error(`MailerSend HTTP ${res.status}: ${body}`)
    }
  }
}
