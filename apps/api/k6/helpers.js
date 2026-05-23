/**
 * Shared helpers for Hop API K6 load tests.
 */

import { check } from 'k6'
import http from 'k6/http'

// ---------------------------------------------------------------------------
// Configuration
// ---------------------------------------------------------------------------

/**
 * BASE_URL must include the API prefix.
 *
 * Local:      http://localhost:3000/api/v1   (default)
 * Production: https://<service>.up.railway.app/api/v1
 *
 * Pass via:  -e BASE_URL=https://your-service.up.railway.app/api/v1
 */
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:3000/api/v1'

/**
 * Build a token pool from the TOKENS env var (comma-separated list of Supabase
 * JWTs) or fall back to the single TOKEN env var.
 *
 * Usage:
 *   k6 run -e TOKENS="eyJ...,eyJ..." load-test.js
 *   k6 run -e TOKEN="eyJ..."        load-test.js
 */
export function buildTokenPool() {
  if (__ENV.TOKENS) {
    return __ENV.TOKENS.split(',').map((t) => t.trim()).filter(Boolean)
  }
  if (__ENV.TOKEN) {
    return [__ENV.TOKEN.trim()]
  }
  return []
}

/** Pick a token from the pool using the VU index so each VU is consistent. */
export function pickToken(pool) {
  if (!pool || pool.length === 0) return null
  return pool[__VU % pool.length]
}

// ---------------------------------------------------------------------------
// HTTP helpers
// ---------------------------------------------------------------------------

export function authHeaders(token) {
  if (!token) return { 'Content-Type': 'application/json' }
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  }
}

/** GET wrapper — returns the response and checks in one call. */
export function get(path, params = {}) {
  // Strip a leading slash so BASE_URL can end with or without one
  return http.get(`${BASE_URL}/${path.replace(/^\//, '')}`, params)
}

/** Authenticated GET. */
export function authGet(path, token) {
  return http.get(`${BASE_URL}/${path.replace(/^\//, '')}`, { headers: authHeaders(token) })
}

/** Authenticated POST. */
export function authPost(path, body, token) {
  return http.post(`${BASE_URL}/${path.replace(/^\//, '')}`, JSON.stringify(body), {
    headers: authHeaders(token),
  })
}

/** Authenticated DELETE. */
export function authDelete(path, token) {
  return http.del(`${BASE_URL}/${path.replace(/^\//, '')}`, null, {
    headers: authHeaders(token),
  })
}

// ---------------------------------------------------------------------------
// Check helpers
// ---------------------------------------------------------------------------

/**
 * Assert that a response is a 2xx success.
 * Records a named check in the k6 summary.
 */
export function expectOk(res, label) {
  return check(res, {
    [`${label} → status 2xx`]: (r) => r.status >= 200 && r.status < 300,
    [`${label} → has body`]: (r) => r.body && r.body.length > 0,
  })
}

/**
 * Assert a specific status code.
 */
export function expectStatus(res, status, label) {
  return check(res, {
    [`${label} → status ${status}`]: (r) => r.status === status,
  })
}

// ---------------------------------------------------------------------------
// Data fixtures
// ---------------------------------------------------------------------------

/**
 * A handful of realistic origin/destination coordinate pairs spread across a
 * typical service area. Add or replace to match your actual deployment region.
 */
export const ROUTE_FIXTURES = [
  { originLat: 36.7372, originLng: 3.0868,  destLat: 36.3650, destLng: 6.6147  }, // Algiers → Sétif
  { originLat: 36.3650, originLng: 6.6147,  destLat: 36.7372, destLng: 3.0868  }, // Sétif → Algiers
  { originLat: 36.7372, originLng: 3.0868,  destLat: 35.6974, destLng: -0.6337 }, // Algiers → Oran
  { originLat: 35.6974, originLng: -0.6337, destLat: 36.7372, destLng: 3.0868  }, // Oran → Algiers
  { originLat: 36.3650, originLng: 6.6147,  destLat: 36.4600, destLng: 2.8277  }, // Sétif → Blida
  { originLat: 36.4600, originLng: 2.8277,  destLat: 36.3650, destLng: 6.6147  }, // Blida → Sétif
  { originLat: 36.7372, originLng: 3.0868,  destLat: 36.4600, destLng: 2.8277  }, // Algiers → Blida
  { originLat: 36.9000, originLng: 7.7500,  destLat: 36.7372, destLng: 3.0868  }, // Annaba → Algiers
]

/** Returns one of the route fixtures, cycling by VU + iteration. */
export function pickRoute(iter) {
  return ROUTE_FIXTURES[((__VU - 1) * 13 + iter) % ROUTE_FIXTURES.length]
}

/**
 * Build a trip search query string for a date that is always ~7 days from now.
 * (K6 scripts are compiled once; Date.now() gives execution-time timestamps.)
 */
export function searchQueryForDate(offset = 7) {
  const d = new Date(Date.now() + offset * 86400 * 1000)
  return d.toISOString().substring(0, 10) // "YYYY-MM-DD"
}
