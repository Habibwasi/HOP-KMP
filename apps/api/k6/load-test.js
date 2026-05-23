/**
 * Hop API — K6 Load Test
 *
 * Three concurrent scenarios modelling realistic carpooling app traffic:
 *
 *   anonymous_search   (60 %)  Public trip search — no auth, highest volume
 *   authenticated_read (30 %)  Profile, trips and bookings reads — needs auth
 *   write_flow         (10 %)  Trip / booking creation — needs auth, DB writes
 *
 * Quick reference:
 *   k6 run -e BASE_URL=http://localhost:3000 -e TEST_TYPE=smoke load-test.js
 *   k6 run -e BASE_URL=https://api.staging.yourdomain.com \
 *          -e TEST_TYPE=load \
 *          -e TOKENS="eyJ...,eyJ..." \
 *          load-test.js
 *
 * TEST_TYPE values: smoke | load | stress   (default: smoke)
 */

import { sleep } from 'k6'
import { Trend, Rate, Counter } from 'k6/metrics'
import {
  buildTokenPool,
  pickToken,
  authGet,
  authPost,
  authDelete,
  get,
  expectOk,
  pickRoute,
  searchQueryForDate,
  BASE_URL,
} from './helpers.js'

// ---------------------------------------------------------------------------
// Custom metrics
// ---------------------------------------------------------------------------

const tripSearchDuration  = new Trend('trip_search_duration',  true)
const authReadDuration    = new Trend('auth_read_duration',    true)
const writeFlowDuration   = new Trend('write_flow_duration',   true)
const authErrors          = new Rate('auth_errors')
const searchErrors        = new Rate('search_errors')
const writeErrors         = new Rate('write_errors')
const totalRequests       = new Counter('total_requests')

// ---------------------------------------------------------------------------
// Load profiles
// ---------------------------------------------------------------------------

const PROFILES = {
  /** 2 VUs for 30 s — just verifies the test runs without error. */
  smoke: {
    scenarios: {
      smoke_search: {
        executor: 'constant-vus',
        vus: 2,
        duration: '30s',
        exec: 'searchScenario',
      },
    },
  },

  /**
   * Ramp to 50 VUs, hold for 5 min, ramp down.
   * Simulates a moderate busy period (e.g. morning commute window).
   */
  load: {
    scenarios: {
      anonymous_search: {
        executor: 'ramping-vus',
        startVUs: 0,
        stages: [
          { duration: '1m',  target: 30 }, // ramp-up
          { duration: '5m',  target: 30 }, // steady state
          { duration: '30s', target: 0  }, // ramp-down
        ],
        exec: 'searchScenario',
        gracefulRampDown: '15s',
      },
      authenticated_read: {
        executor: 'ramping-vus',
        startVUs: 0,
        stages: [
          { duration: '1m',  target: 15 },
          { duration: '5m',  target: 15 },
          { duration: '30s', target: 0  },
        ],
        exec: 'authReadScenario',
        gracefulRampDown: '15s',
      },
      write_flow: {
        executor: 'ramping-vus',
        startVUs: 0,
        stages: [
          { duration: '1m',  target: 5 },
          { duration: '5m',  target: 5 },
          { duration: '30s', target: 0 },
        ],
        exec: 'writeScenario',
        gracefulRampDown: '15s',
      },
    },
  },

  /**
   * Progressive stress test — keeps increasing load until something buckles.
   * Watch for p(95) latency spikes and rising error rates.
   */
  stress: {
    scenarios: {
      anonymous_search: {
        executor: 'ramping-vus',
        startVUs: 0,
        stages: [
          { duration: '2m', target: 50  },
          { duration: '2m', target: 100 },
          { duration: '2m', target: 150 },
          { duration: '2m', target: 200 },
          { duration: '2m', target: 0   },
        ],
        exec: 'searchScenario',
        gracefulRampDown: '30s',
      },
      authenticated_read: {
        executor: 'ramping-vus',
        startVUs: 0,
        stages: [
          { duration: '2m', target: 25 },
          { duration: '2m', target: 50 },
          { duration: '2m', target: 75 },
          { duration: '2m', target: 100},
          { duration: '2m', target: 0  },
        ],
        exec: 'authReadScenario',
        gracefulRampDown: '30s',
      },
    },
  },

  /**
   * Breakpoint test — ramps VUs aggressively until the system breaks.
   * NOT a pass/fail test; thresholds are intentionally loose.
   * Goal: find where error rate climbs and latency collapses.
   *
   * Peak: 800 anonymous + 400 auth = 1 200 VUs over 20 min.
   * Stop early if you see errors spiking in the console.
   *
   *   k6 run -e TEST_TYPE=breakpoint -e TOKENS="..." load-test.js
   */
  breakpoint: {
    scenarios: {
      anonymous_search: {
        executor: 'ramping-vus',
        startVUs: 0,
        stages: [
          { duration: '2m',  target: 100 },
          { duration: '2m',  target: 200 },
          { duration: '2m',  target: 300 },
          { duration: '2m',  target: 400 },
          { duration: '2m',  target: 500 },
          { duration: '2m',  target: 600 },
          { duration: '2m',  target: 700 },
          { duration: '2m',  target: 800 },
          { duration: '2m',  target: 0   },
        ],
        exec: 'searchScenario',
        gracefulRampDown: '30s',
      },
      authenticated_read: {
        executor: 'ramping-vus',
        startVUs: 0,
        stages: [
          { duration: '2m',  target: 50  },
          { duration: '2m',  target: 100 },
          { duration: '2m',  target: 150 },
          { duration: '2m',  target: 200 },
          { duration: '2m',  target: 250 },
          { duration: '2m',  target: 300 },
          { duration: '2m',  target: 350 },
          { duration: '2m',  target: 400 },
          { duration: '2m',  target: 0   },
        ],
        exec: 'authReadScenario',
        gracefulRampDown: '30s',
      },
    },
  },
}

const TEST_TYPE = __ENV.TEST_TYPE || 'smoke'
const profile   = PROFILES[TEST_TYPE]

if (!profile) {
  throw new Error(`Unknown TEST_TYPE "${TEST_TYPE}". Choose: smoke | load | stress | breakpoint`)
}

// ---------------------------------------------------------------------------
// Thresholds
// ---------------------------------------------------------------------------

export const options = {
  ...profile,

  thresholds: {
    // Overall HTTP error rate must stay below 1 %
    http_req_failed: ['rate<0.01'],

    // 95th-percentile response time for the whole test suite
    // Stress allows more headroom — 300 VUs vs 50 VUs at load
    // Breakpoint is observation-only — ceiling set high so test never aborts early
    http_req_duration: TEST_TYPE === 'breakpoint'
      ? ['p(95)<5000']
      : [TEST_TYPE === 'stress' ? 'p(95)<1500' : 'p(95)<800'],

    // Per-scenario latency budgets
    trip_search_duration: TEST_TYPE === 'breakpoint'
      ? ['p(95)<3000']
      : ['p(95)<600', 'p(99)<1200'],
    // auth_read_duration measures the full iteration (5 sequential auth calls)
    // Under stress (300 VUs) each call takes ~400 ms → 5× accumulation → ~2 s p(95)
    auth_read_duration: TEST_TYPE === 'stress'
      ? ['p(95)<2500', 'p(99)<4000']
      : TEST_TYPE === 'breakpoint'
        ? ['p(95)<10000']
        : ['p(95)<1500', 'p(99)<2500'],
    write_flow_duration:  ['p(95)<800', 'p(99)<1500'],

    // Per-scenario error rates
    search_errors: ['rate<0.01'],
    auth_errors:   ['rate<0.02'],
    write_errors:  ['rate<0.05'],
  },
}

// ---------------------------------------------------------------------------
// Setup — build token pool once, share with all VUs
// ---------------------------------------------------------------------------

export function setup() {
  const tokens = buildTokenPool()

  if (tokens.length === 0) {
    console.warn(
      '[setup] No tokens provided. Authenticated scenarios will be skipped. ' +
      'Pass -e TOKEN="..." or -e TOKENS="t1,t2,..." to enable them.',
    )
  } else {
    console.log(`[setup] Token pool size: ${tokens.length}`)
  }

  // Verify the API is reachable before the test starts
  const health = get('/health')
  if (health.status !== 200) {
    throw new Error(`Health check failed (status ${health.status}). Is the API running at ${BASE_URL}?`)
  }
  console.log(`[setup] API is healthy at ${BASE_URL}`)

  return { tokens }
}

// ---------------------------------------------------------------------------
// Scenario: anonymous trip search
// ---------------------------------------------------------------------------

export function searchScenario(data) {
  const iter    = __ITER
  const route   = pickRoute(iter)
  const date    = searchQueryForDate(7 + (iter % 14)) // 7–20 days ahead

  const qs = [
    `date=${date}`,
    `originLat=${route.originLat}`,
    `originLng=${route.originLng}`,
    `destLat=${route.destLat}`,
    `destLng=${route.destLng}`,
    `radiusKm=20`,
  ].join('&')

  const start = Date.now()
  const res   = get(`/trips/search?${qs}`)
  const dur   = Date.now() - start

  tripSearchDuration.add(dur)
  totalRequests.add(1)

  const ok = expectOk(res, 'trip-search')
  searchErrors.add(!ok)

  sleep(randomBetween(1, 3))
}

// ---------------------------------------------------------------------------
// Scenario: authenticated reads (profile, trips, bookings)
// ---------------------------------------------------------------------------

export function authReadScenario(data) {
  const token = pickToken(data.tokens)

  if (!token) {
    // Skip gracefully if no tokens were provided
    sleep(2)
    return
  }

  const start = Date.now()

  // 1. Fetch own profile
  const me = authGet('/users/me', token)
  expectOk(me, 'GET /users/me')
  totalRequests.add(1)

  // 2. My driver trips
  const driverTrips = authGet('/trips/me/driver', token)
  expectOk(driverTrips, 'GET /trips/me/driver')
  totalRequests.add(1)

  // 3. My passenger trips
  const passengerTrips = authGet('/trips/me/passenger', token)
  expectOk(passengerTrips, 'GET /trips/me/passenger')
  totalRequests.add(1)

  // 4. Active booking
  const activeBooking = authGet('/bookings/me/active', token)
  expectOk(activeBooking, 'GET /bookings/me/active')
  totalRequests.add(1)

  // 5. Unread notification count
  const notifCount = authGet('/notifications/unread-count', token)
  expectOk(notifCount, 'GET /notifications/unread-count')
  totalRequests.add(1)

  const dur = Date.now() - start
  authReadDuration.add(dur)

  const allOk = [me, driverTrips, passengerTrips, activeBooking, notifCount]
    .every((r) => r.status >= 200 && r.status < 300)
  authErrors.add(!allOk)

  sleep(randomBetween(2, 5))
}

// ---------------------------------------------------------------------------
// Scenario: write flow (trip creation)
// ---------------------------------------------------------------------------

export function writeScenario(data) {
  const token = pickToken(data.tokens)

  if (!token) {
    sleep(3)
    return
  }

  const route = pickRoute(__ITER)
  const departAt = new Date(Date.now() + (7 + (__ITER % 10)) * 86400 * 1000)
  departAt.setHours(8 + (__ITER % 10), 0, 0, 0)
  // thresholdDeadline must be strictly before departureAt (Model B requirement)
  const deadline = new Date(departAt.getTime() - 2 * 86400 * 1000)

  const body = {
    model:             'B',
    originLat:         route.originLat,
    originLng:         route.originLng,
    originAddress:     'Load test origin',
    destLat:           route.destLat,
    destLng:           route.destLng,
    destAddress:       'Load test destination',
    seats:             2,
    departureAt:       departAt.toISOString(),
    distanceMetres:    200000,
    minPassengers:     1,
    thresholdDeadline: deadline.toISOString(),
  }

  const start = Date.now()
  const res   = authPost('/trips', body, token)
  const dur   = Date.now() - start

  writeFlowDuration.add(dur)
  totalRequests.add(1)

  // 201 Created expected; 400 means our fixture data was rejected
  const ok = res.status === 201 || res.status === 200
  writeErrors.add(!ok)

  if (!ok) {
    console.error(`[write] POST /trips failed: ${res.status} — ${res.body}`)
  }

  sleep(randomBetween(5, 15))
}

// ---------------------------------------------------------------------------
// Teardown
// ---------------------------------------------------------------------------

export function teardown(data) {
  if (!data || !data.tokens || data.tokens.length === 0) {
    console.log('[teardown] No tokens — skipping load-test trip cleanup.')
    return
  }

  // Deduplicate tokens so we don't hit the same account twice
  const seen = {}
  const uniqueTokens = data.tokens.filter((t) => {
    if (seen[t]) return false
    seen[t] = true
    return true
  })

  let cancelled = 0
  let errors = 0

  uniqueTokens.forEach((token) => {
    const res = authGet('/trips/me/driver', token)
    if (res.status !== 200) {
      console.warn(`[teardown] Could not fetch driver trips (${res.status}) — skipping cleanup for this token.`)
      errors++
      return
    }

    let trips
    try {
      const parsed = JSON.parse(res.body)
      // API wraps all responses in { data: ... } via TransformInterceptor
      trips = Array.isArray(parsed) ? parsed : (parsed.data ?? null)
    } catch (_) {
      console.warn('[teardown] Failed to parse /trips/me/driver response — skipping.')
      errors++
      return
    }

    if (!Array.isArray(trips)) {
      console.warn('[teardown] Unexpected response shape from /trips/me/driver — skipping.')
      errors++
      return
    }

    trips
      .filter((t) => t.originAddress === 'Load test origin' && t.status !== 'CANCELLED' && t.status !== 'COMPLETED')
      .forEach((trip) => {
        const del = authDelete(`/trips/${trip.id}`, token)
        if (del.status === 200 || del.status === 204) {
          cancelled++
        } else {
          console.warn(`[teardown] DELETE /trips/${trip.id} returned ${del.status}`)
          errors++
        }
      })
  })

  console.log(`[teardown] Cleanup complete — cancelled ${cancelled} load-test trip(s), ${errors} error(s).`)
}

// ---------------------------------------------------------------------------
// Utility
// ---------------------------------------------------------------------------

function randomBetween(min, max) {
  return Math.random() * (max - min) + min
}
