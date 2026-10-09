import { ConfigService } from '@nestjs/config'
import { AppException } from '../common/errors/app-exception'
import { WaitlistService } from './waitlist.service'
import { JoinWaitlistDto } from './dto/join-waitlist.dto'

function makeService(existing: Record<string, unknown> | null = null) {
  const prisma = {
    waitlistSignup: {
      findUnique: jest.fn().mockResolvedValue(existing),
      create: jest.fn().mockImplementation(({ data }) => Promise.resolve({ id: 'new', confirmSentAt: null, ...data })),
      update: jest.fn().mockImplementation(({ data }) => Promise.resolve({ ...existing, ...data })),
      delete: jest.fn().mockResolvedValue(undefined),
      groupBy: jest.fn(),
      count: jest.fn(),
    },
  }
  const mail = { sendWithFallback: jest.fn().mockResolvedValue(undefined) }
  const config = { get: jest.fn((_key: string, fallback?: string) => fallback) } as unknown as ConfigService
  const service = new WaitlistService(prisma as never, mail as never, config)
  return { service, prisma, mail }
}

const dto = (over: Partial<JoinWaitlistDto> = {}): JoinWaitlistDto => ({
  email: 'sofie@example.dk',
  role: 'PASSENGER',
  origin: 'Roskilde',
  dest: 'København',
  consent: true,
  ...over,
})

describe('WaitlistService.join', () => {
  it('creates a signup and sends a Danish confirmation email by default', async () => {
    const { service, prisma, mail } = makeService()
    await expect(service.join(dto(), '1.1.1.1')).resolves.toEqual({ ok: true })
    expect(prisma.waitlistSignup.create).toHaveBeenCalledTimes(1)
    const created = prisma.waitlistSignup.create.mock.calls[0][0].data
    expect(created.locale).toBe('da')
    expect(created.token).toMatch(/^[0-9a-f]{64}$/)
    expect(mail.sendWithFallback).toHaveBeenCalledTimes(1)
    expect(mail.sendWithFallback.mock.calls[0][0].subject).toContain('venteliste')
  })

  it('silently drops honeypot submissions', async () => {
    const { service, prisma, mail } = makeService()
    await expect(service.join(dto({ website: 'spam.example' }), '1.1.1.1')).resolves.toEqual({ ok: true })
    expect(prisma.waitlistSignup.create).not.toHaveBeenCalled()
    expect(mail.sendWithFallback).not.toHaveBeenCalled()
  })

  it('does nothing for an already confirmed email', async () => {
    const { service, prisma, mail } = makeService({ id: 'a', confirmedAt: new Date() })
    await service.join(dto(), '1.1.1.1')
    expect(prisma.waitlistSignup.update).not.toHaveBeenCalled()
    expect(mail.sendWithFallback).not.toHaveBeenCalled()
  })

  it('does not resend within the cooldown', async () => {
    const { service, mail } = makeService({ id: 'a', email: 'sofie@example.dk', token: 't', confirmedAt: null, confirmSentAt: new Date() })
    await service.join(dto(), '1.1.1.1')
    expect(mail.sendWithFallback).not.toHaveBeenCalled()
  })

  it('still returns ok when every mail provider fails', async () => {
    const { service, mail, prisma } = makeService()
    mail.sendWithFallback.mockRejectedValue(new Error('down'))
    await expect(service.join(dto(), '1.1.1.1')).resolves.toEqual({ ok: true })
    expect(prisma.waitlistSignup.update).not.toHaveBeenCalled()
  })

  it('rate-limits after 5 requests from one IP', async () => {
    const { service } = makeService()
    for (let i = 0; i < 5; i++) await service.join(dto({ website: 'x' }), '2.2.2.2')
    await expect(service.join(dto(), '2.2.2.2')).rejects.toBeInstanceOf(AppException)
    await expect(service.join(dto({ website: 'x' }), '3.3.3.3')).resolves.toEqual({ ok: true })
  })
})

describe('WaitlistService.stats', () => {
  it('merges routes case-insensitively and counts BOTH on each side', async () => {
    const { service, prisma } = makeService()
    prisma.waitlistSignup.groupBy
      .mockResolvedValueOnce([{ role: 'PASSENGER', _count: { _all: 3 } }])
      .mockResolvedValueOnce([
        { origin: 'Roskilde', dest: 'København', role: 'PASSENGER', _count: { _all: 2 } },
        { origin: 'roskilde', dest: 'københavn', role: 'DRIVER', _count: { _all: 1 } },
        { origin: 'Roskilde', dest: 'København', role: 'BOTH', _count: { _all: 1 } },
      ])
    prisma.waitlistSignup.count.mockResolvedValue(4)

    const stats = await service.stats()
    expect(stats.pendingConfirmation).toBe(4)
    expect(stats.topRoutes).toEqual([
      { origin: 'Roskilde', dest: 'København', passengers: 3, drivers: 2, total: 4 },
    ])
  })
})

describe('WaitlistService.websiteUrl', () => {
  it('sends English signups to /en/', () => {
    const { service } = makeService()
    expect(service.websiteUrl('en', 'confirmed')).toBe('https://ridly.dk/en/?waitlist=confirmed')
    expect(service.websiteUrl('da', 'removed')).toBe('https://ridly.dk/?waitlist=removed')
  })
})
