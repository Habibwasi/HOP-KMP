import { Body, Controller, Get, Post, Query, Res } from '@nestjs/common'
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

/**
 * Step 1 — landing page returned by the GET.
 *
 * Email scanners (Gmail, Outlook, Chrome Safe-Browsing) pre-fetch every link
 * in an email as a GET request.  If we called verifyOtp here, the scanner
 * would consume the one-time token before the user ever clicks, and the
 * user would always see "link expired".
 *
 * Instead the GET page just shows a button.  The token is embedded as data
 * attributes so it never leaves the page until the user explicitly clicks.
 * The actual verifyOtp call happens only in the POST handler below.
 */
function buildLandingPage(tokenHash: string, type: string, postUrl: string): string {
  const safeTokenHash = escapeHtml(tokenHash)
  const safeType      = escapeHtml(type)
  const safePostUrl   = escapeHtml(postUrl)
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Confirm your email — Ridly</title>
  <style>
    *{box-sizing:border-box;}
    body{margin:0;padding:40px 24px;background:#f5f5f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;text-align:center;}
    .card{max-width:400px;margin:60px auto;background:#fff;border-radius:16px;padding:40px 32px;box-shadow:0 2px 16px rgba(0,0,0,.08);}
    h2{color:#0d0d0d;font-size:22px;margin:0 0 12px;}
    p{color:#666;font-size:15px;margin:0 0 24px;line-height:1.5;}
    .btn{display:inline-block;padding:14px 32px;background:#C8F135;border-radius:10px;font-size:16px;font-weight:700;color:#0d0d0d;text-decoration:none;border:none;cursor:pointer;width:100%;}
    .btn:disabled{opacity:.5;cursor:not-allowed;}
    .status{font-size:14px;color:#999;min-height:20px;margin-top:12px;}
    .error{color:#c0392b;}
  </style>
</head>
<body>
  <div class="card">
    <h2>Confirm your email</h2>
    <p>Tap the button below to verify your Ridly account and open the app.</p>
    <button class="btn" id="confirmBtn" onclick="confirmEmail()">Confirm email</button>
    <div class="status" id="status"></div>
  </div>
  <script>
    async function confirmEmail() {
      var btn    = document.getElementById('confirmBtn');
      var status = document.getElementById('status');
      btn.disabled = true;
      status.textContent = 'Verifying…';
      status.className = 'status';
      try {
        var resp = await fetch('${safePostUrl}', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ tokenHash: '${safeTokenHash}', type: '${safeType}' })
        });
        var json = await resp.json();
        if (!resp.ok || json.error) {
          status.textContent = json.message || 'Verification failed. The link may have expired.';
          status.className = 'status error';
          btn.disabled = false;
          return;
        }
        status.textContent = 'Email confirmed! Opening Ridly…';
        window.location.href = json.deepLink;
        // Fallback: show Open button if the app doesn't open within 3 s
        setTimeout(function () {
          var a = document.createElement('a');
          a.href = json.deepLink;
          a.className = 'btn';
          a.textContent = 'Open Ridly';
          a.style.display = 'inline-block';
          a.style.marginTop = '16px';
          document.querySelector('.card').appendChild(a);
        }, 3000);
      } catch (e) {
        status.textContent = 'Network error. Please try again.';
        status.className = 'status error';
        btn.disabled = false;
      }
    }
  </script>
</body>
</html>`
}

/**
 * Handles email confirmation deep-link redirects.
 *
 * Two-step approach to survive email-scanner pre-fetching:
 *   GET  /confirm-email  → Returns a landing page (safe for scanners — no OTP consumed).
 *   POST /confirm-email  → Called by the landing page button click; verifies OTP and
 *                          returns the ridly:// deep link as JSON.
 */
@Controller('auth')
export class AuthController {
  constructor(private readonly config: ConfigService) {}

  /** Step 1 — safe for email-scanner pre-fetches; does NOT verify the token. */
  @Get('confirm-email')
  confirmEmail(
    @Query('token_hash') tokenHash: string,
    @Query('type') type: string,
    @Res() res: Response,
  ): void {
    if (!tokenHash || !type || !VALID_OTP_TYPES.has(type)) {
      res.status(400).send('<h1>Invalid confirmation link</h1>')
      return
    }
    const baseUrl  = this.config.get<string>('API_BASE_URL', 'https://hop-kmp-production.up.railway.app')
    const postUrl  = `${baseUrl}/api/v1/auth/confirm-email`
    res.status(200).send(buildLandingPage(tokenHash, type, postUrl))
  }

  /** Step 2 — user-initiated; verifies OTP and returns the deep-link URL. */
  @Post('confirm-email')
  async confirmEmailPost(
    @Body('tokenHash') tokenHash: string,
    @Body('type') type: string,
    @Res() res: Response,
  ): Promise<void> {
    if (!tokenHash || !type || !VALID_OTP_TYPES.has(type)) {
      res.status(400).json({ error: true, message: 'Invalid confirmation parameters.' })
      return
    }

    const supabaseUrl = this.config.getOrThrow<string>('SUPABASE_URL')
    const anonKey     = this.config.getOrThrow<string>('SUPABASE_ANON_KEY')

    const supabase = createClient(supabaseUrl, anonKey, {
      auth: { persistSession: false, autoRefreshToken: false },
    })

    const { data, error } = await supabase.auth.verifyOtp({
      token_hash: tokenHash,
      type: type as EmailOtpType,
    })

    if (error || !data.session) {
      const message = error?.message ?? 'Verification failed — the link may have already been used.'
      res.status(400).json({ error: true, message })
      return
    }

    const { access_token, refresh_token } = data.session
    const deepLink =
      `ridly://auth/callback` +
      `?access_token=${encodeURIComponent(access_token)}` +
      `&refresh_token=${encodeURIComponent(refresh_token)}` +
      `&type=${encodeURIComponent(type)}`

    res.status(200).json({ deepLink })
  }
}
