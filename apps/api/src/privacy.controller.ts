import { Controller, Get, Header } from '@nestjs/common'

@Controller('privacy')
export class PrivacyController {
  @Get()
  @Header('Content-Type', 'text/html; charset=utf-8')
  getPrivacyPolicy(): string {
    return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Privacy Policy – Hop by Ridly</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; max-width: 720px; margin: 48px auto; padding: 0 24px; color: #1a1a1a; line-height: 1.7; }
    h1 { font-size: 28px; margin-bottom: 4px; }
    .updated { color: #666; font-size: 14px; margin-bottom: 40px; }
    h2 { font-size: 16px; margin-top: 32px; margin-bottom: 6px; }
    p, ul { font-size: 15px; color: #333; margin: 0 0 12px; }
    ul { padding-left: 20px; }
    li { margin-bottom: 4px; }
    a { color: #1a73e8; }
  </style>
</head>
<body>
  <h1>Privacy Policy</h1>
  <p class="updated">Last updated: May 2026</p>

  <p>This Privacy Policy explains how Ridly ("we", "us", or "our") collects, uses, and shares information about you when you use our carpooling platform, Hop.</p>

  <h2>1. Information We Collect</h2>
  <p>We collect information you provide directly to us, such as your name, email address, and phone number when you register an account. We also collect location data when you use the app to find or offer rides, as well as payment-related information (MobilePay phone number) to facilitate ride settlements between passengers and drivers.</p>

  <h2>2. How We Use Your Information</h2>
  <p>We use the information we collect to:</p>
  <ul>
    <li>Provide, maintain, and improve our services</li>
    <li>Match passengers with drivers</li>
    <li>Facilitate payments between users</li>
    <li>Send notifications about your bookings and trips</li>
    <li>Respond to your comments and questions</li>
    <li>Send you technical notices and updates</li>
    <li>Monitor and analyse usage patterns</li>
  </ul>

  <h2>3. Information Sharing</h2>
  <p>We share your information only in the following circumstances:</p>
  <ul>
    <li>With other users as necessary to facilitate a ride (e.g., your first name and profile picture are visible to matched passengers/drivers)</li>
    <li>With service providers who assist us in operating the platform</li>
    <li>If required by law or to protect the rights and safety of our users</li>
    <li>In connection with a merger, acquisition, or sale of assets</li>
  </ul>

  <h2>4. Location Data</h2>
  <p>We collect precise location data when you use the app to enable ride matching and route display. Location is only collected while the app is in use. You can disable location access through your device settings, though this will limit the functionality of the app.</p>

  <h2>5. Data Retention</h2>
  <p>We retain your personal data for as long as your account is active or as needed to provide services. You may request deletion of your account and associated data by contacting us at <a href="mailto:support@ridly.dk">support@ridly.dk</a>.</p>

  <h2>6. Security</h2>
  <p>We use industry-standard security measures to protect your information. Authentication is handled with email verification. However, no method of transmission over the internet is 100% secure.</p>

  <h2>7. Your Rights (GDPR)</h2>
  <p>If you are located in the European Economic Area, you have the right to:</p>
  <ul>
    <li>Access your personal data</li>
    <li>Correct inaccurate data</li>
    <li>Request deletion of your data</li>
    <li>Object to or restrict processing</li>
    <li>Data portability</li>
  </ul>
  <p>To exercise these rights, contact us at <a href="mailto:support@ridly.dk">support@ridly.dk</a>.</p>

  <h2>8. Children's Privacy</h2>
  <p>Our services are not directed to individuals under the age of 18. We do not knowingly collect personal information from children.</p>

  <h2>9. Contact Us</h2>
  <p>If you have questions about this Privacy Policy, please contact us at:</p>
  <p>Ridly<br />Email: <a href="mailto:support@ridly.dk">support@ridly.dk</a><br />Website: <a href="https://ridly.dk">ridly.dk</a></p>
</body>
</html>`
  }
}
