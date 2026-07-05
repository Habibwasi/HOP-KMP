"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthController = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const supabase_js_1 = require("@supabase/supabase-js");
const VALID_OTP_TYPES = new Set([
    'signup', 'recovery', 'email_change', 'magic_link', 'invite', 'email_otp',
]);
function escapeHtml(s) {
    return s
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#x27;');
}
function buildLandingPage(tokenHash, type, postUrl) {
    const safeTokenHash = escapeHtml(tokenHash);
    const safeType = escapeHtml(type);
    const safePostUrl = escapeHtml(postUrl);
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
</html>`;
}
let AuthController = class AuthController {
    config;
    constructor(config) {
        this.config = config;
    }
    confirmEmail(tokenHash, type, res) {
        if (!tokenHash || !type || !VALID_OTP_TYPES.has(type)) {
            res.status(400).send('<h1>Invalid confirmation link</h1>');
            return;
        }
        const baseUrl = this.config.get('API_BASE_URL', 'https://hop-kmp-production.up.railway.app');
        const postUrl = `${baseUrl}/api/v1/auth/confirm-email`;
        res.status(200).send(buildLandingPage(tokenHash, type, postUrl));
    }
    async confirmEmailPost(tokenHash, type, res) {
        if (!tokenHash || !type || !VALID_OTP_TYPES.has(type)) {
            res.status(400).json({ error: true, message: 'Invalid confirmation parameters.' });
            return;
        }
        const supabaseUrl = this.config.getOrThrow('SUPABASE_URL');
        const anonKey = this.config.getOrThrow('SUPABASE_ANON_KEY');
        const supabase = (0, supabase_js_1.createClient)(supabaseUrl, anonKey, {
            auth: { persistSession: false, autoRefreshToken: false },
        });
        const { data, error } = await supabase.auth.verifyOtp({
            token_hash: tokenHash,
            type: type,
        });
        if (error || !data.session) {
            const message = error?.message ?? 'Verification failed — the link may have already been used.';
            res.status(400).json({ error: true, message });
            return;
        }
        const { access_token, refresh_token } = data.session;
        const deepLink = `ridly://auth/callback` +
            `?access_token=${encodeURIComponent(access_token)}` +
            `&refresh_token=${encodeURIComponent(refresh_token)}` +
            `&type=${encodeURIComponent(type)}`;
        res.status(200).json({ deepLink });
    }
};
exports.AuthController = AuthController;
__decorate([
    (0, common_1.Get)('confirm-email'),
    __param(0, (0, common_1.Query)('token_hash')),
    __param(1, (0, common_1.Query)('type')),
    __param(2, (0, common_1.Res)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, String, Object]),
    __metadata("design:returntype", void 0)
], AuthController.prototype, "confirmEmail", null);
__decorate([
    (0, common_1.Post)('confirm-email'),
    __param(0, (0, common_1.Body)('tokenHash')),
    __param(1, (0, common_1.Body)('type')),
    __param(2, (0, common_1.Res)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, String, Object]),
    __metadata("design:returntype", Promise)
], AuthController.prototype, "confirmEmailPost", null);
exports.AuthController = AuthController = __decorate([
    (0, common_1.Controller)('auth'),
    __metadata("design:paramtypes", [config_1.ConfigService])
], AuthController);
//# sourceMappingURL=auth.controller.js.map