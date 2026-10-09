// Mirrors shared/.../pricing/PricingEngine.kt — keep in sync.
const SKAT_RATE_OERE_PER_METRE = 0.228

function pricePerSeatOere(km: number, seats: number) {
  const total = Math.round(km * 1000 * SKAT_RATE_OERE_PER_METRE)
  return { total, perSeat: Math.max(Math.floor(total / seats), 100) }
}

const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

// ── Waitlist forms ─────────────────────────────────────────────────────────
for (const form of document.querySelectorAll<HTMLFormElement>('[data-waitlist]')) {
  const status = form.querySelector<HTMLElement>('.wl__status')!
  const button = form.querySelector<HTMLButtonElement>('button[type=submit]')!
  const email = form.elements.namedItem('email') as HTMLInputElement
  const consent = form.elements.namedItem('consent') as HTMLInputElement

  const show = (key: string, kind: 'ok' | 'err') => {
    status.textContent = status.dataset[key] ?? ''
    status.dataset.kind = kind
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault()
    if (!email.value.trim() || !email.checkValidity()) {
      show('errEmail', 'err')
      email.focus()
      return
    }
    if (!consent.checked) {
      show('errConsent', 'err')
      consent.focus()
      return
    }

    const data = new FormData(form)
    const body = {
      email: email.value.trim(),
      role: data.get('role'),
      origin: (data.get('origin') as string)?.trim() || undefined,
      dest: (data.get('dest') as string)?.trim() || undefined,
      locale: document.documentElement.lang === 'en' ? 'en' : 'da',
      source: form.dataset.source,
      consent: true,
      website: (data.get('website') as string) || undefined,
    }

    button.disabled = true
    button.firstChild!.textContent = button.dataset.sending ?? ''
    status.textContent = ''
    try {
      const res = await fetch('/api/waitlist', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      if (res.ok) {
        form.classList.add('is-done')
        show('success', 'ok')
        return
      }
      show(res.status === 429 ? 'errRate' : res.status === 400 ? 'errEmail' : 'errGeneric', 'err')
    } catch {
      show('errGeneric', 'err')
    } finally {
      button.disabled = false
      button.firstChild!.textContent = button.dataset.label ?? ''
    }
  })
}

// ── Banner from confirm / unsubscribe redirects ────────────────────────────
{
  const banner = document.querySelector<HTMLElement>('[data-banner]')!
  const text = banner.querySelector<HTMLElement>('[data-banner-text]')!
  const state = new URLSearchParams(location.search).get('waitlist')
  if (state && ['confirmed', 'removed', 'invalid'].includes(state)) {
    text.textContent = text.dataset[state] ?? ''
    banner.hidden = false
    banner.dataset.kind = state
    history.replaceState(null, '', location.pathname + location.hash)
  }
  banner.querySelector('[data-banner-close]')!.addEventListener('click', () => (banner.hidden = true))
}

// ── Passenger / driver tabs ────────────────────────────────────────────────
for (const root of document.querySelectorAll<HTMLElement>('[data-tabs]')) {
  const tabs = [...root.querySelectorAll<HTMLButtonElement>('[role=tab]')]
  const select = (tab: HTMLButtonElement, focus = false) => {
    tabs.forEach((t, i) => {
      const on = t === tab
      t.setAttribute('aria-selected', String(on))
      t.tabIndex = on ? 0 : -1
      document.getElementById(t.getAttribute('aria-controls')!)!.hidden = !on
      if (on) root.style.setProperty('--tab', String(i))
    })
    if (focus) tab.focus()
  }
  tabs.forEach((tab, i) => {
    tab.addEventListener('click', () => select(tab))
    tab.addEventListener('keydown', (e) => {
      if (e.key !== 'ArrowRight' && e.key !== 'ArrowLeft') return
      const next = tabs[(i + (e.key === 'ArrowRight' ? 1 : tabs.length - 1)) % tabs.length]
      select(next, true)
    })
  })
}

// ── Price calculator ───────────────────────────────────────────────────────
for (const calc of document.querySelectorAll<HTMLElement>('[data-calc]')) {
  const fmt = new Intl.NumberFormat(calc.dataset.locale, { style: 'currency', currency: 'DKK' })
  const range = calc.querySelector<HTMLInputElement>('input[data-km]')!
  const kmOut = calc.querySelector<HTMLElement>('[data-km-out]')!
  const perSeatOut = calc.querySelector<HTMLElement>('[data-per-seat]')!
  const totalOut = calc.querySelector<HTMLElement>('[data-total]')!
  const chips = [...calc.querySelectorAll<HTMLButtonElement>('[data-km]:not(input)')]
  const seatInputs = [...calc.querySelectorAll<HTMLInputElement>('input[name=seats]')]

  const render = () => {
    const km = Number(range.value)
    const seats = Number(seatInputs.find((s) => s.checked)?.value ?? 1)
    const { total, perSeat } = pricePerSeatOere(km, seats)
    kmOut.textContent = `${km} km`
    perSeatOut.textContent = fmt.format(perSeat / 100)
    totalOut.textContent = fmt.format(total / 100)
    range.style.setProperty('--fill', `${((km - Number(range.min)) / (Number(range.max) - Number(range.min))) * 100}%`)
  }

  chips.forEach((chip) =>
    chip.addEventListener('click', () => {
      chips.forEach((c) => c.setAttribute('aria-pressed', String(c === chip)))
      range.value = chip.dataset.km!
      const seat = seatInputs.find((s) => s.value === chip.dataset.seats)
      if (seat) seat.checked = true
      render()
    }),
  )
  range.addEventListener('input', () => {
    chips.forEach((c) => c.setAttribute('aria-pressed', String(c.dataset.km === range.value)))
    render()
  })
  seatInputs.forEach((s) => s.addEventListener('change', render))
  render()
}

// ── Scroll reveals + route rail progress ───────────────────────────────────
if (!reduceMotion && 'IntersectionObserver' in window) {
  const io = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          entry.target.classList.add('in')
          io.unobserve(entry.target)
        }
      }
    },
    { rootMargin: '0px 0px -10% 0px' },
  )
  document.querySelectorAll('.reveal, .stop').forEach((el) => io.observe(el))
} else {
  document.querySelectorAll('.reveal, .stop').forEach((el) => el.classList.add('in'))
}

const rail = document.querySelector<HTMLElement>('.rail-wrap')
if (rail) {
  let ticking = false
  const update = () => {
    const r = rail.getBoundingClientRect()
    const p = Math.min(1, Math.max(0, (window.innerHeight * 0.55 - r.top) / r.height))
    rail.style.setProperty('--progress', p.toFixed(4))
    ticking = false
  }
  window.addEventListener('scroll', () => {
    if (!ticking) {
      ticking = true
      requestAnimationFrame(update)
    }
  }, { passive: true })
  update()
}
