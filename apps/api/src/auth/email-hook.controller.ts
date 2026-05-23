import {
  Controller,
  Post,
  Body,
  Headers,
  HttpCode,
  HttpStatus,
  UnauthorizedException,
  BadRequestException,
  Logger,
} from '@nestjs/common'
import { ConfigService } from '@nestjs/config'
import { JwtService } from '@nestjs/jwt'
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
 * Security: Bearer token in Authorization header must match
 * the SUPABASE_HOOK_SECRET environment variable.
 */
@Controller('auth/hook')
export class EmailHookController {
  private readonly logger = new Logger(EmailHookController.name)

  constructor(
    private readonly mailService: MailService,
    private readonly config: ConfigService,
    private readonly jwtService: JwtService,
  ) {}

  @Post('email')
  @HttpCode(HttpStatus.OK)
  async handleEmailHook(
    @Headers('authorization') authHeader: string | undefined,
    @Body() payload: SupabaseEmailHookPayload,
  ): Promise<Record<string, never>> {
    // ── 1. Verify hook secret ──────────────────────────────────────────────
    this.verifySecret(authHeader)

    // ── 2. Validate payload ────────────────────────────────────────────────
    const recipientEmail = payload?.user?.email
    const emailData = payload?.email_data

    if (!recipientEmail || !emailData?.email_action_type) {
      throw new BadRequestException('Invalid hook payload')
    }

    const actionType = emailData.email_action_type

    // ── 3. Build email content ─────────────────────────────────────────────
    const html = buildAuthEmail({
      actionType,
      recipientEmail,
      token:      emailData.token,
      tokenHash:  emailData.token_hash,
      redirectTo: emailData.redirect_to,
      siteUrl:    emailData.site_url,
    })

    const subject = buildSubject(actionType)

    // ── 4. Send via fallback chain (Brevo → Resend → MailerSend) ──────────
    this.logger.log(`Sending [${actionType}] email to ${recipientEmail}`)
    await this.mailService.sendWithFallback({ to: recipientEmail, subject, html })

    // Supabase expects an empty 200 response on success
    return {}
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private verifySecret(authHeader: string | undefined): void {
    const hookSecret = this.config.get<string>('SUPABASE_HOOK_SECRET')
    if (!hookSecret) {
      this.logger.warn('SUPABASE_HOOK_SECRET is not set — hook is unprotected!')
      return
    }

    const token = authHeader?.replace(/^Bearer\s+/i, '')
    if (!token) {
      throw new UnauthorizedException('Missing authorization token')
    }

    // Supabase hook secrets have the format "v1,whsec_<base64>".
    // The actual HMAC-SHA256 signing key is the base64-decoded bytes of
    // the part after "whsec_". Passing the raw string will always fail.
    const b64 = hookSecret.replace(/^v\d+,whsec_/, '')
    const signingKey = Buffer.from(b64, 'base64')

    try {
      this.jwtService.verify(token, { secret: signingKey })
    } catch (err) {
      this.logger.warn(`Hook JWT verification failed: ${(err as Error).message}`)
      throw new UnauthorizedException('Invalid hook token')
    }
  }
}
