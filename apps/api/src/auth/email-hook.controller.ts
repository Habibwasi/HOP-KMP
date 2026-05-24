import {
  Controller,
  Post,
  RawBodyRequest,
  Req,
  HttpCode,
  HttpStatus,
  UnauthorizedException,
  BadRequestException,
  Logger,
} from '@nestjs/common'
import { ConfigService } from '@nestjs/config'
import { Webhook } from 'standardwebhooks'
import { MailService } from '../mail/mail.service'
import {
  buildAuthEmail,
  buildSubject,
  EmailActionType,
} from '../mail/templates/auth-email.template'

/** Payload Supabase sends to the Send Email hook */
interface SupabaseEmailHookPayload {
  user: {
    id: string
    email: string
  }
  email_data: {
    token: string
    token_hash: string
    redirect_to: string
    email_action_type: EmailActionType
    site_url: string
    token_new: string
    token_hash_new: string
  }
}

/**
 * POST /auth/hook/email
 *
 * Supabase Auth "Send Email" HTTP hook.
 * Configure in Supabase Dashboard → Authentication → Hooks → Send Email.
 *
 * Security: Supabase signs requests using the Standard Webhooks protocol
 * (webhook-id / webhook-timestamp / webhook-signature headers).
 * The signing key is SUPABASE_HOOK_SECRET (format: "v1,whsec_<base64>").
 */
@Controller('auth/hook')
export class EmailHookController {
  private readonly logger = new Logger(EmailHookController.name)

  constructor(
    private readonly mailService: MailService,
    private readonly config: ConfigService,
  ) {}

  @Post('email')
  @HttpCode(HttpStatus.OK)
  async handleEmailHook(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    @Req() req: any,
  ): Promise<Record<string, never>> {
    // ── 1. Verify signature (Standard Webhooks protocol) ──────────────────
    const payload = this.verifyWebhook(req)

    // ── 2. Validate payload ────────────────────────────────────────────────
    const recipientEmail = payload?.user?.email
    const emailData = payload?.email_data

    if (!recipientEmail || !emailData?.email_action_type) {
      throw new BadRequestException('Invalid hook payload')
    }

    const actionType = emailData.email_action_type

    // ── 3. Build email content ─────────────────────────────────────────────
    const apiBaseUrl = this.config.get<string>('API_BASE_URL', 'https://hop-kmp-production.up.railway.app')
    const html = buildAuthEmail({
      actionType,
      recipientEmail,
      token:      emailData.token,
      tokenHash:  emailData.token_hash,
      redirectTo: emailData.redirect_to,
      siteUrl:    emailData.site_url,
      apiBaseUrl,
    })

    const subject = buildSubject(actionType)

    // ── 4. Send via fallback chain (Brevo → Resend → MailerSend) ──────────
    this.logger.log(`Sending [${actionType}] email to ${recipientEmail}`)
    await this.mailService.sendWithFallback({ to: recipientEmail, subject, html })

    // Supabase expects an empty 200 response on success
    return {}
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private verifyWebhook(req: RawBodyRequest<any>): SupabaseEmailHookPayload {
    const hookSecret = this.config.get<string>('SUPABASE_HOOK_SECRET')
    if (!hookSecret) {
      this.logger.warn('SUPABASE_HOOK_SECRET is not set — hook is unprotected!')
      // Fall back to parsing the body directly
      return req.body as SupabaseEmailHookPayload
    }

    // Supabase signs hook requests using the Standard Webhooks protocol.
    // Strip "v1,whsec_" prefix — the Webhook class expects just the base64 secret.
    const signingSecret = hookSecret.replace(/^v\d+,whsec_/, '')

    const wh = new Webhook(signingSecret)
    const rawBody = req.rawBody
    if (!rawBody) {
      throw new UnauthorizedException('Raw body unavailable')
    }

    try {
      const verified = wh.verify(rawBody.toString(), req.headers as Record<string, string>)
      return verified as SupabaseEmailHookPayload
    } catch (err) {
      this.logger.warn(`Hook signature verification failed: ${(err as Error).message}`)
      throw new UnauthorizedException('Invalid webhook signature')
    }
  }
}
