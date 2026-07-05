import { Controller, Get, Res } from '@nestjs/common'
import type { Response } from 'express'

// Served at hop.ridly.dk/delete-account — required by Play Store Data Safety
// section. Uses @Res() to bypass the global TransformInterceptor.
@Controller('delete-account')
export class DeleteAccountController {
  @Get()
  getDeleteAccountPage(@Res() res: Response): void {
    res.setHeader('Content-Type', 'text/html; charset=utf-8')
    res.send(`<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Delete Account – Hop by Ridly</title>
  <style>
    * { box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; background: #f5f5f5; margin: 0; padding: 0; display: flex; align-items: center; justify-content: center; min-height: 100vh; }
    .card { background: #fff; border-radius: 16px; padding: 40px 36px; max-width: 480px; width: 100%; margin: 24px; box-shadow: 0 2px 16px rgba(0,0,0,0.08); }
    h1 { font-size: 22px; margin: 0 0 8px; color: #1a1a1a; }
    .subtitle { color: #666; font-size: 15px; margin: 0 0 28px; line-height: 1.6; }
    .warning { background: #fff4f4; border: 1px solid #fecaca; border-radius: 10px; padding: 14px 16px; margin-bottom: 28px; font-size: 14px; color: #991b1b; line-height: 1.6; }
    ul { margin: 0; padding-left: 18px; }
    li { margin-bottom: 4px; }
    a.button { display: block; background: #ef4444; color: #fff; text-decoration: none; text-align: center; padding: 14px; border-radius: 10px; font-size: 16px; font-weight: 600; }
    a.button:hover { background: #dc2626; }
    .note { margin-top: 20px; font-size: 13px; color: #888; text-align: center; line-height: 1.6; }
    .note a { color: #1a73e8; }
  </style>
</head>
<body>
  <div class="card">
    <h1>Delete your Hop account</h1>
    <p class="subtitle">To request deletion of your account and all associated data, send an email to our support team. Your account will be removed within 30 days.</p>

    <div class="warning">
      <strong>This action is permanent. The following data will be deleted:</strong>
      <ul>
        <li>Your profile (name, email, phone number)</li>
        <li>Trip and booking history</li>
        <li>Ratings and reviews</li>
        <li>Saved places and search history</li>
        <li>Chat messages</li>
      </ul>
    </div>

    <a class="button" href="mailto:support@ridly.dk?subject=Account%20Deletion%20Request&body=Please%20delete%20my%20Hop%20account%20and%20all%20associated%20data.%0A%0ARegistered%20email%3A%20">
      Request account deletion
    </a>

    <p class="note">
      You can also delete your account from within the app:<br />
      Settings → Account → Delete account<br /><br />
      Questions? <a href="mailto:support@ridly.dk">support@ridly.dk</a>
    </p>
  </div>
</body>
</html>`)
  }
}
