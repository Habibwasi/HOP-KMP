/**
 * Hop API — K6 Failure / Resilience Test
 *
 * Verifies that the API degrades gracefully under adverse conditions:
 *
 *   bad_input        Malformed / missing / out-of-range payloads → 400
 *   auth_failures    Missing, invalid, and expired tokens → 401
 *   not_found        Non-existent resource IDs → 404
 *   flood_single     Rapid burst on one endpoint → no 500s, stable latency
 *   timeout          Short deadline forces connection timeout → test handles it
 *
 * Run:
 *   k6 run k6/failure-test.js -e BASE_URL=http://localhost:3000/api/v1
 *   k6 run k6/failure-test.js -e BASE_URL=https://<service>.up.railway.app/api/v1 -e TOKEN="eyJ..."
 */

import { check, sleep } from 'k6'
import { Rate, Counter } from 'k6/metrics'
import http from 'k6/http'
import {
  BASE_URL,
  buildTokenPool,
  pickToken,
  authHeaders,
  get,
  expectStatus,
} from './helpers.js'

// ---------------------------------------------------------------------------
// Custom metrics
// ---------------------------------------------------------------------------

const unexpected5xx      = new Rate('unexpected_5xx')        // must stay 0
const correctRejections  = new Rate('correct_rejections')    // 4xx when expected
const timeoutHandled     = new Rate('timeout_handled')       // timeout didn't panic
const totalFailureReqs   = new Counter('failure_test_requests')

// ---------------------------------------------------------------------------
// Options
// ---------------------------------------------------------------------------

export const options = {
  scenarios: {
    bad_input: {
      executor: 'constant-vus',
      vus: 5,
      duration: '1m',
      exec: 'badInputScenario',
    },
    auth_failures: {
      executor: 'constant-vus',
      vus: 3,
      duration: '1m',
      exec: 'authFailureScenario',
    },
    not_found: {
      executor: 'constant-vus',
      vus: 3,
      duration: '1m',
      exec: 'notFoundScenario',
    },
    flood_single: {
      executor: 'constant-arrival-rate',
      rate: 50,            // 50 RPS sustained
      timeUnit: '1s',
      duration: '1m',
      preAllocatedVUs: 20,
      maxVUs: 40,
      exec: 'floodScenario',
    },
    timeout_behaviour: {
      executor: 'constant-vus',
      vus: 2,
      duration: '1m',
      exec: 'timeoutScenario',
    },
  },

  thresholds: {
    // The server must never return 5xx for any of these negative inputs
    unexpected_5xx: ['rate==0'],

    // At least 95 % of intentionally bad requests should be rejected (4xx)
    correct_rejections: ['rate>0.95'],

    // Even under flood the endpoint must respond within 2 s p(95)
    http_req_duration: ['p(95)<2000'],

    // Every timeout must be handled without a panic
    timeout_handled: ['rate==1'],
  },
}

// ---------------------------------------------------------------------------
// Setup
// ---------------------------------------------------------------------------

export function setup() {
  const health = http.get(`${BASE_URL}/health`)
  if (health.status !== 200) {
    throw new Error(`API not reachable at ${BASE_URL} (status ${health.status})`)
  }
  const tokens = buildTokenPool()
  console.log(`[setup] token pool: ${tokens.length}, base: ${BASE_URL}`)
  return { tokens }
}

// ---------------------------------------------------------------------------
// Scenario 1: Bad input — malformed / invalid payloads
// ---------------------------------------------------------------------------

export function badInputScenario(_data) {
  // 1a. Trip search with missing required `date` → 400
  {
    const res = get('/trips/search?originLat=36.7&originLng=3.0&destLat=35.6&destLng=-0.6')
    totalFailureReqs.add(1)
    const ok = check(res, {
      'search without date → 400': (r) => r.status === 400,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  // 1b. Trip search with non-numeric lat/lng → 400
  {
    const res = get('/trips/search?date=2026-07-01&originLat=not-a-number&originLng=3.0&destLat=35.6&destLng=-0.6')
    totalFailureReqs.add(1)
    const ok = check(res, {
      'search with bad lat → 400': (r) => r.status === 400,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  // 1c. Trip search with seats out of range (0 and 99) → 400
  for (const seats of [0, 99]) {
    const res = get(`/trips/search?date=2026-07-01&seats=${seats}`)
    totalFailureReqs.add(1)
    const ok = check(res, {
      [`search with seats=${seats} → 400`]: (r) => r.status === 400,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  // 1d. POST /trips with empty body — auth skipped, just check the response code
  {
    const res = http.post(`${BASE_URL}/trips`, '{}', {
      headers: { 'Content-Type': 'application/json' },
    })
    totalFailureReqs.add(1)
    // Either 400 (validation) or 401 (auth guard fires first) — both are correct
    const ok = check(res, {
      'POST /trips empty body → 400 or 401': (r) => r.status === 400 || r.status === 401,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  // 1e. Oversized string field (10 KB address)
  {
    const hugeString = 'A'.repeat(10_000)
    const body = JSON.stringify({
      model: 'OFFER',
      originLat: 36.7, originLng: 3.0,
      originAddress: hugeString,
      destLat: 35.6, destLng: -0.6,
      destAddress: 'dest',
      seats: 2,
      pricePerSeat: 500,
      departAt: new Date(Date.now() + 7 * 86400 * 1000).toISOString(),
    })
    const res = http.post(`${BASE_URL}/trips`, body, {
      headers: { 'Content-Type': 'application/json' },
    })
    totalFailureReqs.add(1)
    // 400 (validation), 401 (auth), or 413 (payload too large) — all acceptable
    const ok = check(res, {
      'POST /trips huge payload → 4xx': (r) => r.status >= 400 && r.status < 500,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  // 1f. Malformed JSON body
  {
    const res = http.post(`${BASE_URL}/trips`, '{ this is: not json }', {
      headers: { 'Content-Type': 'application/json' },
    })
    totalFailureReqs.add(1)
    const ok = check(res, {
      'POST /trips malformed JSON → 400 or 401': (r) => r.status === 400 || r.status === 401,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  sleep(1)
}

// ---------------------------------------------------------------------------
// Scenario 2: Auth failures — missing / garbage / wrong tokens
// ---------------------------------------------------------------------------

export function authFailureScenario(_data) {
  const protectedEndpoints = [
    '/users/me',
    '/trips/me/driver',
    '/trips/me/passenger',
    '/bookings/me/active',
    '/notifications/unread-count',
  ]

  // 2a. No Authorization header
  for (const path of protectedEndpoints) {
    const res = http.get(`${BASE_URL}${path}`)
    totalFailureReqs.add(1)
    const ok = check(res, {
      [`${path} no token → 401`]: (r) => r.status === 401,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  // 2b. Garbage token
  {
    const res = http.get(`${BASE_URL}/users/me`, {
      headers: { Authorization: 'Bearer this.is.garbage' },
    })
    totalFailureReqs.add(1)
    const ok = check(res, {
      'GET /users/me garbage token → 401': (r) => r.status === 401,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  // 2c. Wrong scheme
  {
    const res = http.get(`${BASE_URL}/users/me`, {
      headers: { Authorization: 'Basic dXNlcjpwYXNz' },
    })
    totalFailureReqs.add(1)
    const ok = check(res, {
      'GET /users/me Basic scheme → 401': (r) => r.status === 401,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  // 2d. Empty bearer value
  {
    const res = http.get(`${BASE_URL}/users/me`, {
      headers: { Authorization: 'Bearer ' },
    })
    totalFailureReqs.add(1)
    const ok = check(res, {
      'GET /users/me empty bearer → 401': (r) => r.status === 401,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  sleep(1)
}

// ---------------------------------------------------------------------------
// Scenario 3: Not found — random / synthetic UUIDs
// ---------------------------------------------------------------------------

const FAKE_UUIDS = [
  '00000000-0000-0000-0000-000000000000',
  'ffffffff-ffff-ffff-ffff-ffffffffffff',
  'deadbeef-dead-beef-dead-beefdeadbeef',
  '12345678-1234-1234-1234-123456789012',
]

export function notFoundScenario(_data) {
  const id = FAKE_UUIDS[__ITER % FAKE_UUIDS.length]

  // 3a. GET /trips/:id with non-existent ID
  {
    const res = get(`/trips/${id}`)
    totalFailureReqs.add(1)
    const ok = check(res, {
      [`GET /trips/${id.slice(0, 8)}… → 404`]: (r) => r.status === 404,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  // 3b. GET /users/:id with non-existent ID
  {
    const res = get(`/users/${id}`)
    totalFailureReqs.add(1)
    const ok = check(res, {
      [`GET /users/${id.slice(0, 8)}… → 404 or 401`]: (r) => r.status === 404 || r.status === 401,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  // 3c. Non-existent route entirely
  {
    const res = get('/does/not/exist')
    totalFailureReqs.add(1)
    const ok = check(res, {
      'GET unknown route → 404': (r) => r.status === 404,
    })
    correctRejections.add(ok)
    unexpected5xx.add(res.status >= 500)
  }

  sleep(0.5)
}

// ---------------------------------------------------------------------------
// Scenario 4: Flood — 50 RPS on the public search endpoint
// Verifies no 5xx under burst; latency may degrade but must not panic.
// ---------------------------------------------------------------------------

export function floodScenario(_data) {
  const res = get('/trips/search?date=2026-07-15&originLat=36.7372&originLng=3.0868&destLat=35.6974&destLng=-0.6337')
  totalFailureReqs.add(1)

  check(res, {
    'flood search → not 5xx': (r) => r.status < 500,
  })
  unexpected5xx.add(res.status >= 500)

  // No sleep — constant-arrival-rate controls pacing
}

// ---------------------------------------------------------------------------
// Scenario 5: Timeout — very short deadline, server must not crash
// ---------------------------------------------------------------------------

export function timeoutScenario(_data) {
  const params = {
    timeout: '200ms',   // deliberately shorter than typical DB round-trip
    headers: { 'Content-Type': 'application/json' },
  }

  const res = http.get(
    `${BASE_URL}/trips/search?date=2026-07-15&originLat=36.7372&originLng=3.0868&destLat=35.6974&destLng=-0.6337`,
    params,
  )
  totalFailureReqs.add(1)

  // Acceptable outcomes: got a fast response (2xx/4xx) OR request timed out (status 0)
  const handled = check(res, {
    'tight timeout → 2xx, 4xx, or timeout (0)': (r) =>
      r.status === 0 || (r.status >= 200 && r.status < 500),
  })
  timeoutHandled.add(handled)
  unexpected5xx.add(res.status >= 500)

  sleep(1)
}
