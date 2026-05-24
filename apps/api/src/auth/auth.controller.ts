import { Controller, Get, Query, Res } from '@nestjs/common'
import { ConfigService } from '@nestjs/config'
import type { Response } from 'express'
import { createClient } from '@supabase/supabase-js'
import type { EmailOtpType } from '@supabase/supabase-js'

const VALID_OTP_TYPES: ReadonlySet<string> = new Set([
  'signup', 'recovery', 'email_change', 'magic_link', 'invite', 'email_otp',
])

function escapeHtml(s: string): string {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
}

function buildRedirectPage(deepLink: string): string {
  const safeLinkJson = JSON.stringify(deepLink)
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Opening Ridly…</title>
  <script>
    var deepLink = ${safeLinkJson};
    window.location.href = deepLink;
    setTimeout(function () {
      var btn = document.getElementById('btn');
      var loading = document.getElementById('loading');
      if (btn) { btn.href = deepLink; btn.style.display = 'inline-block'; }
      if (loading) loading.style.display = 'none';
    }, 2000);
  </script>
  <style>
    body{margin:0;padding:40px 24px;background:#f5f5f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;text-align:center;}
    .card{max-width:400px;margin:60px auto;background:#fff;border-radius:16px;padding:40px 32px;box-shadow:0 2px 16px rgba(0,0,0,.08);}
    h2{color:#0d0d0d;font-size:22px;margin:0 0 12px;}
    p{color:#666;font-size:15px;margin:0 0 24px;line-height:1.5;}
    .btn{display:none;padding:14px 32px;background:#C8F135;border-radius:10px;font-size:16px;font-weight:700;color:#0d0d0d;text-decoration:none;}
    .loading{font-size:14px;color:#999;}
  </style>
</head>
<body>
  <div class="card">
    <h2>Email confirmed ✓</h2>
    <p>Your Ridly account is ready. Opening the app…</p>
    <span class="loading" id="loading">Opening Ridly…</span>
    <a class="btn" id="btn" href="#">Open Ridly</a>
  </div>
</body>
</html>`
}

function buildErrorPage(message: string): string {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Link expired — Ridly</title>
  <style>
    body{margin:0;padding:40px 24px;background:#f5f5f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;text-align:center;}
    .card{max-width:400px;margin:60px auto;background:#fff;border-radius:16px;padding:40px 32px;box-shadow:0 2px 16px rgba(0,0,0,.08);}
    h2{color:#0d0d0d;font-size:22px;margin:0 0 12px;}
    p{color:#666;font-size:15px;margin:0;line-height:1.5;}
  </style>
</head>
<body>
  <div class="card">
    <h2>Link expired</h2>
    <p>${escapeHtml(message)}</p>
    <p style="margin-top:16px">Please return to the Ridly app and request a new confirmation email.</p>
  </div>
</body>
</html>`
}

/**
 * Handles email confirmation deep-link redirects.
 *
 * The email's confirmation button points to this endpoint (HTTPS) rather than
 * directly to Supabase's /verify URL. This intermediate step:
 *   1. Verifies the token server-side via Supabase JS client.
 *   2. Passes the resulting session tokens to the app via a ridly:// deep link
 *      using a JS-initiated redirect — which is more reliable than following a
 *      server-side 302 to a custom URL scheme across all email clients / browsers.
 */
@Controller('auth')
export class AuthController {
  constructor(private readonly config: ConfigService) {}

  @Get('confirm-email')
  async confirmEmail(
    @Query('token_hash') tokenHash: string,
    @Query('type') type: string,
    @Res() res: Response,
  ): Promise<void> {
    if (!tokenHash || !type || !VALID_OTP_TYPES.has(type)) {
      res.status(400).send(buildErrorPage('Invalid or missing confirmation parameters.'))
      return
    }

    const supabaseUrl = this.config.getOrThrow<string>('SUPABASE_URL')
    const anonKey    = this.config.getOrThrow<string>('SUPABASE_ANON_KEY')

    const supabase = createClient(supabaseUrl, anonKey, {
      auth: { persistSession: false, autoRefreshToken: false },
    })

    const { data, error } = await supabase.auth.verifyOtp({
      token_hash: tokenHash,
      type: type as EmailOtpType,
    })

    if (error || !data.session) {
      const message = error?.message ?? 'Verification failed — the link may have already been used.'
      res.status(200).send(buildErrorPage(message))
      return
    }

    const { access_token, refresh_token } = data.session
    const deepLink =
      `ridly://auth/callback` +
      `?access_token=${encodeURIComponent(access_token)}` +
      `&refresh_token=${encodeURIComponent(refresh_token)}` +
      `&type=${encodeURIComponent(type)}`

    res.status(200).send(buildRedirectPage(deepLink))
  }
}
