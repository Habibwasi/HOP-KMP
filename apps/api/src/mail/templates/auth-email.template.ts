/** Supabase `email_action_type` values */
export type EmailActionType =
  | 'signup'
  | 'recovery'
  | 'magic_link'
  | 'invite'
  | 'email_change'
  | 'email_otp'

export interface AuthEmailContext {
  actionType: EmailActionType
  recipientEmail: string
  /** 6-digit OTP (present for OTP-style flows) */
  token: string
  /** Hash used to build the confirmation URL */
  tokenHash: string
  /** Where to send the user after they confirm */
  redirectTo: string
  /** Your project's base URL e.g. https://app.ridly.com */
  siteUrl: string
  /** Base URL of this API server, used to build the confirm-email redirect URL */
  apiBaseUrl: string
}

// ── Subject lines ──────────────────────────────────────────────────────────

export function buildSubject(actionType: EmailActionType): string {
  const subjects: Record<EmailActionType, string> = {
    signup:       'Confirm your Ridly account',
    recovery:     'Reset your Ridly password',
    magic_link:   'Your Ridly sign-in link',
    invite:       "You've been invited to Ridly",
    email_change: 'Confirm your new email address',
    email_otp:    'Your Ridly verification code',
  }
  return subjects[actionType] ?? 'Action required — Ridly'
}

// ── HTML templates ─────────────────────────────────────────────────────────

export function buildAuthEmail(ctx: AuthEmailContext): string {
  // Route confirmation through our own API endpoint so the browser → app
  // redirect is handled via JavaScript (more reliable than following a
  // server-side 302 to a custom URL scheme across all email clients).
  const confirmUrl =
    `${ctx.apiBaseUrl}/api/v1/auth/confirm-email` +
    `?token_hash=${encodeURIComponent(ctx.tokenHash)}` +
    `&type=${encodeURIComponent(ctx.actionType)}`

  const content = buildContent(ctx, confirmUrl)

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>${content.subject}</title>
</head>
<body style="margin:0;padding:0;background:#f5f5f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
    <tr>
      <td align="center">
        <table width="100%" style="max-width:520px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.06);">

          <!-- Header -->
          <tr>
            <td style="background:#0d0d0d;padding:28px 32px;text-align:center;">
              <span style="font-size:22px;font-weight:700;color:#C8F135;letter-spacing:-0.5px;">Ridly</span>
            </td>
          </tr>

          <!-- Body -->
          <tr>
            <td style="padding:36px 32px 24px;">
              <h1 style="margin:0 0 12px;font-size:20px;font-weight:700;color:#0d0d0d;line-height:1.3;">
                ${content.heading}
              </h1>
              <p style="margin:0 0 24px;font-size:15px;color:#555;line-height:1.6;">
                ${content.body}
              </p>

              ${content.showOtp ? `
              <!-- OTP code -->
              <div style="margin:0 0 24px;text-align:center;">
                <span style="display:inline-block;padding:14px 32px;background:#f0f0f0;border-radius:8px;font-size:28px;font-weight:700;letter-spacing:8px;color:#0d0d0d;">
                  ${ctx.token}
                </span>
              </div>
              <p style="margin:0 0 24px;font-size:13px;color:#888;text-align:center;">
                This code expires in 60 minutes. Do not share it with anyone.
              </p>` : ''}

              ${content.showButton ? `
              <!-- CTA button -->
              <div style="text-align:center;margin:0 0 24px;">
                <a href="${confirmUrl}"
                   style="display:inline-block;padding:14px 32px;background:#C8F135;border-radius:8px;font-size:15px;font-weight:600;color:#0d0d0d;text-decoration:none;">
                  ${content.ctaLabel}
                </a>
              </div>
              <p style="margin:0 0 24px;font-size:12px;color:#aaa;text-align:center;word-break:break-all;">
                Or copy this link: <a href="${confirmUrl}" style="color:#666;">${confirmUrl}</a>
              </p>` : ''}
            </td>
          </tr>

          <!-- Footer -->
          <tr>
            <td style="padding:20px 32px;border-top:1px solid #f0f0f0;text-align:center;">
              <p style="margin:0;font-size:12px;color:#bbb;line-height:1.6;">
                If you didn't request this email, you can safely ignore it.<br />
                &copy; ${new Date().getFullYear()} Ridly. All rights reserved.
              </p>
            </td>
          </tr>

        </table>
      </td>
    </tr>
  </table>
</body>
</html>`
}

// ── Per-action content ─────────────────────────────────────────────────────

interface EmailContent {
  subject: string
  heading: string
  body: string
  showOtp: boolean
  showButton: boolean
  ctaLabel: string
}

function buildContent(ctx: AuthEmailContext, _url: string): EmailContent {
  switch (ctx.actionType) {
    case 'signup':
      return {
        subject:    buildSubject('signup'),
        heading:    'Confirm your email address',
        body:       "Thanks for signing up to Ridly! Tap the button below to verify your email address and activate your account.",
        showOtp:    false,
        showButton: true,
        ctaLabel:   'Confirm email',
      }

    case 'recovery':
      return {
        subject:    buildSubject('recovery'),
        heading:    'Reset your password',
        body:       "We received a request to reset your Ridly password. Tap the button below — the link is valid for 60 minutes.",
        showOtp:    false,
        showButton: true,
        ctaLabel:   'Reset password',
      }

    case 'magic_link':
      return {
        subject:    buildSubject('magic_link'),
        heading:    'Sign in to Ridly',
        body:       'Tap the button below to sign in. This link expires in 60 minutes and can only be used once.',
        showOtp:    false,
        showButton: true,
        ctaLabel:   'Sign in',
      }

    case 'invite':
      return {
        subject:    buildSubject('invite'),
        heading:    "You've been invited to Ridly",
        body:       "Someone invited you to join Ridly — your carpooling community. Tap below to accept the invitation and create your account.",
        showOtp:    false,
        showButton: true,
        ctaLabel:   'Accept invitation',
      }

    case 'email_change':
      return {
        subject:    buildSubject('email_change'),
        heading:    'Confirm your new email address',
        body:       `We received a request to change the email on your Ridly account to <strong>${ctx.recipientEmail}</strong>. Tap below to confirm.`,
        showOtp:    false,
        showButton: true,
        ctaLabel:   'Confirm new email',
      }

    case 'email_otp':
    default:
      return {
        subject:    buildSubject('email_otp'),
        heading:    'Your verification code',
        body:       'Use the code below to verify your identity. Do not share this code.',
        showOtp:    true,
        showButton: false,
        ctaLabel:   '',
      }
  }
}
