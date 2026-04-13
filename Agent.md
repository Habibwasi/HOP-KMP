# Hop — Agent Instructions
## Skills

Always load and apply these skills before generating any code:

- `kmp-compose-multiplatform` — KMP architecture, Ktor, Koin, expect/actual, iOS interop
- `compose-skill` — MVI, Compose UI, state management, navigation, performance, anti-patterns

When a skill suggestion conflicts with a decision in this file, stop and notify me with:
1. What the skill recommends
2. What this file says
3. Your recommendation and why

Do not resolve the conflict silently. Wait for my decision before proceeding.

You are working on **Hop**, a carpooling platform for Denmark.
This file is the single source of truth for all architectural and project decisions.
Do not suggest alternatives to decisions already made. Do not ask clarifying questions covered here.

---

## Project Structure

```
Hop/
  shared/               ← KMP module. ALL business logic lives here.
    src/
      commonMain/       ← Shared Kotlin: domain models, repositories, API client, pricing engine
      androidMain/      ← Android-specific expect/actual implementations
      iosMain/          ← iOS-specific expect/actual implementations
  composeApp/           ← Android UI only. Jetpack Compose. No business logic.
  iosApp/               ← Xcode project. SwiftUI. No business logic.
  gradle/
    libs.versions.toml  ← Single source of truth for all versions and dependencies
```

Package: `com.example.hop` (do not change)
Shared module namespace: `com.example.hop.shared`

---

## Exact Versions (from libs.versions.toml)

```
kotlin                  = 2.3.20
agp                     = 8.11.2
composeMultiplatform    = 1.10.3
material3               = 1.10.0-alpha05
androidx-lifecycle      = 2.10.0
androidx-activity       = 1.13.0
android-compileSdk      = 36
android-minSdk          = 24
android-targetSdk       = 36
```

When adding new dependencies, always add them to `gradle/libs.versions.toml` first,
then reference via `libs.*` aliases. Never hardcode version strings in build.gradle.kts files.

---

## Current Dependencies

### shared/build.gradle.kts — commonMain
Currently empty. Add all multiplatform dependencies here.
Planned additions:
- Ktor client (networking)
- kotlinx-serialization (JSON)
- kotlinx-coroutines-core (async)

### composeApp/build.gradle.kts — already present
- `compose.runtime`, `compose.foundation`, `compose.material3`, `compose.ui`
- `compose.components.resources`, `compose.uiToolingPreview`
- `androidx.lifecycle.viewmodelCompose`, `androidx.lifecycle.runtimeCompose`
- `androidx.activity.compose` (androidMain only)
- `projects.shared` (depends on shared module)

---

## Architecture Decisions (Final — Do Not Revisit)

### KMP Layer Split
- **shared/commonMain** — Ktor API client, domain models, repository interfaces + implementations,
  pricing engine, trip state machine, auth token storage interface (expect/actual)
- **composeApp** — Jetpack Compose UI screens only. Observes StateFlow from shared ViewModels.
- **iosApp** — SwiftUI screens only. Consumes shared xcframework.
- Never put business logic in composeApp. Never put UI code in shared.

### State Management — MVI
Pattern: `Event → ViewModel.onEvent() → StateFlow<UiState>`
- Shared: `StateFlow` + `ViewModel` (KMP-compatible, lives in commonMain)
- Android: collect via `collectAsStateWithLifecycle()`
- One-shot effects (navigation, snackbar): `Channel<Effect>` — never boolean flags in state
- Do not use `mutableStateOf` outside of Composables

### Networking — Ktor
- `ktor-client-core` in commonMain
- `ktor-client-android` in androidMain
- `ktor-client-darwin` in iosMain
- Base URL: `https://api.hop.dk/v1` (staging: `https://staging-api.hop.dk/v1`)
- All responses envelope: `{ data, error, meta }`
- Auth: Bearer JWT in `Authorization` header
- Monetary values: always **Int in øre** (1 DKK = 100 øre). Never Float. Never String.
- Pagination: cursor-based (`cursor` + `limit` params)
- Error handling: `ApiResponse` sealed class wrapping responses — never raw exceptions in UI

### Dependency Injection — Koin
- Koin Multiplatform for all DI
- Do not suggest Hilt — it is Android-only and incompatible with KMP shared module
- Koin modules defined in commonMain, platform-specific modules in androidMain/iosMain

### Auth
- JWT access token: 15-minute lifetime
- Stored in Keychain (iOS) / EncryptedSharedPreferences (Android) via expect/actual
- Refresh token: 30-day lifetime, rotated on every use
- No MitID in MVP — email + phone OTP only (Twilio SMS on backend)

### Maps
- iOS: MapKit (no extra dependency)
- Android: Google Maps SDK for Android
- Do not suggest react-native-maps, Mapbox, or any other library

### Design Tokens
- No NativeWind. No Tailwind. No CSS.
- Tokens defined as a Kotlin `object` in commonMain, consumed natively on each platform
- Colour palette:
  - Primary Lime:     `#C8F135`
  - Primary Green:    `#1DB954`
  - Background:       `#FFFFFF`
  - Surface:          `#1A1A1A`
  - Surface Elevated: `#242424`
  - Text Primary:     `#FFFFFF`
  - Text Secondary:   `#B3B3B3`
  - Success:          `#22C55E`
  - Warning:          `#FBBF24`
  - Error:            `#EF4444`
- Typography: Syne (headings), Inter (body), JetBrains Mono (DKK amounts in tax dashboard only)
- Base spacing unit: 4px. All spacing is multiples of 4.
- Icon library: Lucide Icons. Stroke only — never filled. Always paired with a label.

---

## Pricing Engine (Canonical — Do Not Alter)

File: `shared/src/commonMain/kotlin/com/example/hop/pricing/PricingEngine.kt`

```kotlin
object PricingEngine {
    private const val SKAT_RATE_OERE_PER_METRE = 0.228 // DKK 2.28/km
    private const val PLATFORM_FEE_RATE = 0.15

    data class PriceResult(
        val totalTripCostOere: Int,
        val driverNetPerSeatOere: Int,
        val passengerPaysPerSeatOere: Int,
        val platformFeeOere: Int
    )

    fun calculate(distanceMetres: Int, seatsTotal: Int): PriceResult {
        require(seatsTotal in 1..4)
        require(distanceMetres > 0)
        val totalTripCostOere = (distanceMetres * SKAT_RATE_OERE_PER_METRE).toInt()
        val driverNetPerSeatOere = totalTripCostOere / seatsTotal
        val passengerPaysPerSeatOere = (driverNetPerSeatOere / (1 - PLATFORM_FEE_RATE)).toInt()
        val platformFeeOere = passengerPaysPerSeatOere - driverNetPerSeatOere
        return PriceResult(totalTripCostOere, driverNetPerSeatOere, passengerPaysPerSeatOere, platformFeeOere)
    }
}
```

Verification (Aarhus → Copenhagen, 304,000m, 4 seats):
- totalTripCost    = 69,312 øre  (DKK 693)
- driverNetPerSeat = 17,328 øre  (DKK 173)
- passengerPays    ≈ 20,386 øre  (DKK 204)
- platformFee      ≈  3,058 øre  (DKK  31)

---

## Trip Models

### Model A — Daily Commute
- Driver sets route + days-of-week + departure time + seats
- System creates one trip record per day, 30-day rolling window, auto-extending
- Booking immediately confirmed. Payment charged immediately.
- Driver absorbs occupancy risk — trip runs regardless of seat fill
- Driver can cancel individual days up to 2h before departure

### Model B — One-Off Long Distance
- Driver sets route + date/time + total seats + minimum threshold
- Booking status: `pending`. Payment held by platform.
- BullMQ job (backend) checks every 30 min: if `seats_booked >= min_threshold` → confirm
- 6h before departure: if threshold not met → auto-cancel → all passengers refunded
- PUTW (Pick-Up-on-the-Way): post-MVP only, Model A only. Do not implement.

### Seat Atomicity
- PostgreSQL `SELECT FOR UPDATE` inside a transaction (backend concern)
- Redis `INCR` pre-check before DB lock
- Never optimistic locking for seat booking

---

## Domain Models (shared/commonMain/domain/model/)

All amounts: Int in øre. All distances: Int in metres. All timestamps: UTC.
Display only converts øre → DKK at the UI layer.

```
User          id, fullName, email, phone, phoneVerified, roles, isBanned, ratingDriver, ratingPassenger
Trip          id, driverId, model[A|B], originName, originLat, originLng, destName, destLat, destLng,
              distanceMetres, departsAt, seatsTotal, seatsBooked, minThreshold,
              priceOerePerSeat, driverNetOere, status[active|confirmed|cancelled|completed], recurrenceDays
Booking       id, tripId, passengerId, seats, status[pending|confirmed|cancelled|completed], paymentId
Payment       id, bookingId, passengerId, driverId, amountOere, driverNetOere, platformFeeOere,
              status[held|released|refunded|failed], mobilepayRef
Rating        id, bookingId, raterId, rateeId, roleRated[driver|passenger], stars, comment
TaxRecord     id, driverId, tripId, bookingId, distanceMetres, grossOere, skatRateOere,
              deductionOere, taxableOere, tripDate, taxYear
Message       id, bookingId, senderId, body, sentAt, readAt
```

---

## API Endpoints (Backend — NestJS, Do Not Modify)

Base: `https://api.hop.dk/v1`

```
Auth      POST  /auth/register, /auth/login, /auth/refresh, /auth/logout
          POST  /auth/otp/send, /auth/otp/verify
          POST  /auth/password/reset-request, /auth/password/reset

Trips     GET   /trips/search?origin=&dest=&date=&seats=
          GET   /trips/:id
          POST  /trips
          PATCH /trips/:id
          DELETE /trips/:id
          GET   /trips/me/driver
          GET   /trips/me/passenger
          POST  /trips/:id/complete

Bookings  POST  /bookings
          GET   /bookings/:id
          DELETE /bookings/:id
          POST  /bookings/:id/rate

Tax       GET   /tax/summary?year=&month=
          GET   /tax/records?year=
          GET   /tax/report/:year

Admin     GET   /admin/licences
          POST  /admin/licences/:id/approve
          POST  /admin/licences/:id/reject
          GET   /admin/users
          POST  /admin/users/:id/ban
          POST  /admin/users/:id/unban
          GET   /admin/export/trips
          GET   /admin/export/revenue
```

---

## Screen Inventory (39 screens)

### Onboarding
ON-01 Splash · ON-02 Sign Up · ON-03 Log In · ON-04 Phone OTP Verification

### Passenger
PA-01 Home · PA-02 Search Results · PA-03 Trip Detail · PA-04 Booking Confirmation ·
PA-05 MobilePay Handoff · PA-06 Booking Success · PA-07 My Trips · PA-08 Trip Detail Active ·
PA-09 Rate Driver · PA-10 Cancellation Confirmation

### Driver
DR-01 Home · DR-02 Car Details (Step 1) · DR-03 Licence Upload (Step 2) ·
DR-04 Review Pending (Step 3) · DR-05 Post Trip Model Select · DR-06 Post Trip Model A ·
DR-07 Post Trip Model B · DR-08 Price Review & Confirm · DR-09 My Trips ·
DR-10 Trip Detail Active · DR-11 Mark Trip Complete · DR-12 Rate Passenger ·
DR-13 Tax Dashboard · DR-14 Annual Tax Report Download

### Shared
SH-01 Home Role Toggle · SH-02 Own Profile · SH-03 Other Profile ·
SH-04 In-App Chat · SH-05 Notifications · SH-06 Settings

### Admin (web — React + shadcn/ui, not mobile)
AD-01 Licence Queue · AD-02 Licence Detail · AD-03 User Search ·
AD-04 Apply Ban · AD-05 Trip & Revenue Export

---

## Key UI Rules

- Role toggle pill always visible on home — passenger ↔ driver switch
- Every monetary amount displayed in DKK (divide øre by 100 at render time only)
- Tax dashboard: JetBrains Mono for all DKK amounts
- Empty states: illustration + headline + CTA. Never a blank list.
- Bottom nav: Home | My Trips | Chat | Profile
- MitID button on ON-02 is visible but disabled in MVP
- WCAG AA contrast on all text (4.5:1 minimum)
- Bottom safe areas respected on both iOS and Android
- All spacing multiples of 4dp

---

## Payment — MobilePay

- Primary and only payment method in MVP
- AppSwitch deep-link on mobile → MobilePay app
- Flow: `initiated → held (PaymentReserved webhook) → released or refunded`
- Webhooks signed with HMAC-SHA256 — verified before processing (backend)
- Stripe: post-MVP only. Do not implement.

---

## Backend (Separate — Do Not Generate Unless Asked)

NestJS 10, TypeScript 5, Prisma 5, PostgreSQL 16, Redis 7, BullMQ
AWS ECS Fargate, eu-west-1, RDS + ElastiCache
Modular monolith: Auth, Trips, Payments, Ratings, Chat, Notifications, Tax, Admin modules
Do not suggest microservices.

---

## What NOT To Do

- Do not suggest React Native, Flutter, or Expo
- Do not suggest MongoDB — PostgreSQL is final
- Do not suggest Hilt — Koin is the DI framework
- Do not suggest Retrofit — Ktor is the HTTP client
- Do not suggest Room or DataStore — no local persistence in MVP, all data from REST API
- Do not suggest Stripe in MVP — MobilePay only
- Do not suggest NativeWind, Tailwind, or any CSS framework
- Do not suggest react-native-maps or Mapbox
- Do not suggest microservices
- Do not implement PUTW in MVP
- Do not implement MitID in MVP
- Do not store monetary amounts as Float or String — always Int in øre
- Do not hard-delete User, Trip, or Booking records — soft delete only (deleted_at)
- Do not put business logic in composeApp or iosApp
- Do not hardcode version strings in build.gradle.kts — use libs.versions.toml aliases

---

## Scope Discipline

Every decision should be evaluated against MVP scope.
Scope creep, premature optimisation, and over-engineering are the primary risks.
When in doubt, ship the simpler version and iterate post-MVP.