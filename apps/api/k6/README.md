# Hop API — K6 Load Tests

## Prerequisites

```bash
# macOS
brew install k6

# Windows (Chocolatey)
choco install k6

# Linux
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update && sudo apt-get install k6
```

## Files

| File | Purpose |
|------|---------|
| `load-test.js` | Concurrent-user load test — 3 scenarios, 3 profiles |
| `failure-test.js` | Resilience / negative test — bad input, auth failures, floods, timeouts |
| `helpers.js` | Shared utilities: HTTP wrappers, fixtures, token pool |

## Scenarios

| Scenario | Traffic share | Auth required | Endpoints covered |
|----------|--------------|---------------|-------------------|
| `anonymous_search` | 60 % | No | `GET /trips/search` |
| `authenticated_read` | 30 % | Yes | `/users/me`, `/trips/me/driver`, `/trips/me/passenger`, `/bookings/me/active`, `/notifications/unread-count` |
| `write_flow` | 10 % | Yes | `POST /trips` |

## Running the tests

All commands are run from the `apps/api/` directory (or pass the file path explicitly).

### 1 — Smoke test (2 VUs, 30 s, no auth)

Fastest sanity check — just confirms the API is reachable and returning 2xx.

```bash
k6 run k6/load-test.js \
  -e BASE_URL=http://localhost:3000 \
  -e TEST_TYPE=smoke
```

### 2 — Load test (50 VUs peak, ~7 min, with auth)

Simulates a normal busy period. You **must** provide at least one Supabase JWT
for the authenticated scenarios to run.

```bash
k6 run k6/load-test.js \
  -e BASE_URL=http://localhost:3000 \
  -e TEST_TYPE=load \
  -e TOKENS="eyJhbGci...,eyJhbGci..."
```

### 3 — Stress test (up to 300 VUs, ~10 min)

Progressive overload — ramps until latency or error thresholds are breached.

```bash
k6 run k6/load-test.js \
  -e BASE_URL=https://api.staging.yourdomain.com \
  -e TEST_TYPE=stress \
  -e TOKENS="eyJhbGci...,eyJhbGci..."
```

## Getting test tokens

You need real Supabase JWTs for the authenticated scenarios. The easiest way
during development:

1. Sign in through the app or Supabase Studio.
2. Copy the `access_token` from the session (e.g. from localStorage or the
   Supabase client's `getSession()` response).
3. Pass one or more tokens separated by commas in the `TOKENS` env var.

For CI / staging you can create dedicated test accounts and use the
[Supabase Auth REST API](https://supabase.com/docs/reference/javascript/auth-signinwithpassword)
to generate tokens programmatically before the test run:

```bash
TOKEN=$(curl -s -X POST \
  "https://<project>.supabase.co/auth/v1/token?grant_type=password" \
  -H "apikey: <anon-key>" \
  -H "Content-Type: application/json" \
  -d '{"email":"loadtest@example.com","password":"..."}' \
  | jq -r '.access_token')

k6 run k6/load-test.js -e BASE_URL=http://localhost:3000 -e TEST_TYPE=load -e TOKEN="$TOKEN"
```

## Thresholds

The test will **fail** (exit code 99) if any of these are breached:

| Metric | Limit |
|--------|-------|
| `http_req_failed` | < 1 % |
| `http_req_duration` p(95) | < 800 ms |
| `trip_search_duration` p(95) | < 600 ms |
| `auth_read_duration` p(95) | < 500 ms |
| `write_flow_duration` p(95) | < 800 ms |

## Customising routes

The test uses Algerian city coordinates by default. Edit the `ROUTE_FIXTURES`
array in `helpers.js` to match your deployment region.

## HTML report

```bash
k6 run k6/load-test.js -e TEST_TYPE=load --out json=results.json
# Then open results.json in https://k6.io/docs/results-output/real-time/json/
# or use the k6 Cloud dashboard
```

---

## Failure / resilience test

`failure-test.js` runs 5 concurrent adversarial scenarios for 1 min each:

| Scenario | What it does |
|----------|-------------|
| `bad_input` | Missing fields, out-of-range values, huge strings, malformed JSON → expects 4xx |
| `auth_failures` | No token, garbage token, wrong scheme, empty bearer → expects 401 |
| `not_found` | Fake UUIDs and unknown routes → expects 404 |
| `flood_single` | 50 RPS burst on public search → no 5xx allowed |
| `timeout_behaviour` | 200 ms deadline → verifies the server doesn't crash |

### Thresholds (failure test)

| Metric | Requirement |
|--------|-------------|
| `unexpected_5xx` | must be **0 %** |
| `correct_rejections` | must be **> 95 %** |
| `timeout_handled` | must be **100 %** |
| `http_req_failed` | < 5 % |
| `http_req_duration` p(95) | < 2 000 ms |

### Run the failure test

```bash
# No tokens needed — failure scenarios are mostly unauthenticated
k6 run k6/failure-test.js -e BASE_URL=http://localhost:3000/api/v1

# Against Railway production
k6 run k6/failure-test.js -e BASE_URL=https://<your-service>.up.railway.app/api/v1
```
