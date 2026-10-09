export type WaitlistLocale = 'da' | 'en'

interface WaitlistEmailContext {
  locale: WaitlistLocale
  confirmUrl: string
  unsubscribeUrl: string
}

const COPY: Record<WaitlistLocale, {
  subject: string
  heading: string
  body: string
  button: string
  ignore: string
  unsubscribe: string
}> = {
  da: {
    subject: 'Bekræft din plads på Ridlys venteliste',
    heading: 'Næsten på plads',
    body: 'Tak for din interesse i Ridly. Bekræft din e-mail, så giver vi dig besked, når appen åbner — og når der kommer bilister på din rute.',
    button: 'Bekræft e-mail',
    ignore: 'Har du ikke tilmeldt dig? Så kan du bare ignorere denne mail.',
    unsubscribe: 'Afmeld og slet mine oplysninger',
  },
  en: {
    subject: 'Confirm your spot on the Ridly waitlist',
    heading: 'Almost there',
    body: 'Thanks for your interest in Ridly. Confirm your email and we’ll let you know when the app opens — and when drivers appear on your route.',
    button: 'Confirm email',
    ignore: 'Didn’t sign up? You can safely ignore this email.',
    unsubscribe: 'Unsubscribe and delete my details',
  },
}

export function buildWaitlistSubject(locale: WaitlistLocale): string {
  return COPY[locale].subject
}

export function buildWaitlistEmail(ctx: WaitlistEmailContext): string {
  const c = COPY[ctx.locale]
  return `<!DOCTYPE html>
<html lang="${ctx.locale}">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>${c.subject}</title>
</head>
<body style="margin:0;padding:0;background:#f5f5f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
    <tr>
      <td align="center">
        <table width="100%" style="max-width:520px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.06);">
          <tr>
            <td style="background:#0d0d0d;padding:28px 32px;text-align:center;">
              <span style="font-size:22px;font-weight:700;color:#C8F135;letter-spacing:-0.5px;">ridly</span>
            </td>
          </tr>
          <tr>
            <td style="padding:36px 32px 24px;">
              <h1 style="margin:0 0 12px;font-size:20px;font-weight:700;color:#0d0d0d;line-height:1.3;">${c.heading}</h1>
              <p style="margin:0 0 24px;font-size:15px;color:#555;line-height:1.6;">${c.body}</p>
              <a href="${ctx.confirmUrl}" style="display:inline-block;background:#C8F135;color:#0d0d0d;font-size:15px;font-weight:700;text-decoration:none;padding:14px 28px;border-radius:10px;">${c.button}</a>
              <p style="margin:24px 0 0;font-size:13px;color:#888;line-height:1.6;">${c.ignore}</p>
            </td>
          </tr>
          <tr>
            <td style="padding:16px 32px 28px;border-top:1px solid #eee;">
              <a href="${ctx.unsubscribeUrl}" style="font-size:12px;color:#888;">${c.unsubscribe}</a>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>`
}
