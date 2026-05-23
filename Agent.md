# Ridly - Agent Instructions

You are working on **Ridly**, a carpooling platform for Denmark. The repository and package names still use `HOP-KMP` and `com.example.hop`; do not rename packages, modules, or bundle identifiers unless explicitly asked.

This file records current project decisions and working rules. Do not silently invent architecture changes. If a request conflicts with this file or with verified code, stop and ask for feedback before changing direction.

---

## Required Context Before Code Changes

Before generating or modifying code, load and apply the relevant project skills/instructions:

- `kmp-compose-multiplatform` for Kotlin Multiplatform, Compose Multiplatform, Ktor, Koin, expect/actual, and iOS interop.
- `hop-debug` when auditing, debugging, reviewing, or looking for bugs.

If another instruction or skill conflicts with this file, report:

1. What the other instruction recommends.
2. What this file says.
3. Your recommendation and why.

Then wait for a decision.

---

## Current Project Shape

```text
HOP-KMP/
  shared/               KMP shared module: domain, data, repositories, networking, pricing, ViewModels, DI
  composeApp/           Android app and Compose UI
  iosApp/               iOS SwiftUI app and wrappers around shared ViewModels
  apps/api/             NestJS API with Prisma/PostgreSQL, Supabase auth, Redis/BullMQ
  gradle/               Version catalog and Gradle wrapper configuration
```

Package name: `com.example.hop`.

Shared module namespace: `com.example.hop.shared`.

Keep business logic in `shared/` or backend services. Keep `composeApp/` and `iosApp/` focused on UI, navigation, platform shell, and platform-specific integration code.

---

## Verified Versions And Dependencies

Use [gradle/libs.versions.toml](gradle/libs.versions.toml) as the source of truth for Android/KMP dependencies. Do not hardcode dependency versions in Gradle build files.

Current important versions:

```text
kotlin                = 2.3.20
agp                   = 8.11.2
composeMultiplatform  = 1.10.3
material3             = 1.10.0-alpha05
androidx-lifecycle    = 2.10.0
androidx-activity     = 1.13.0
android-compileSdk    = 36
android-minSdk        = 24
android-targetSdk     = 36
ktor                  = 3.1.3
kotlinx-serialization = 1.8.1
kotlinx-coroutines    = 1.10.1
koin                  = 4.1.1
skie                  = 0.10.11
supabase-kt           = 3.1.4
```

Backend versions are managed in [apps/api/package.json](apps/api/package.json). The API currently uses NestJS 11, Prisma 7, PostgreSQL via `@prisma/adapter-pg`, Redis/BullMQ, Supabase JS, APNs, FCM, Socket.IO, and TypeScript 5.

---

## Architecture Decisions

### Shared KMP Layer

- `shared/src/commonMain` contains domain models, repository contracts, repository implementations, DTO mappers, Ktor networking, pricing, presentation ViewModels, and Koin modules.
- `shared/src/androidMain` contains Android-specific implementations such as token storage, connectivity, and chat repository support.
- `shared/src/iosMain` contains iOS-specific implementations and Koin accessors used by SwiftUI.
- Shared ViewModels expose `StateFlow<UiState>` and one-shot effects via `Channel<Effect>(Channel.BUFFERED)` / `receiveAsFlow()`.

### Android Compose App

- `composeApp/` owns Android UI, navigation, app shell, push integration, and Android-specific entry points.
- Screens collect shared ViewModel state using lifecycle-aware collection.
- Use `koinViewModel()` for injected shared ViewModels.
- Do not put server/business rules in Compose screens.

### iOS SwiftUI App

- `iosApp/` owns SwiftUI screens, navigation, wrappers, and platform UI behavior.
- Use `@StateObject` for ViewModel wrappers. Do not use `@ObservedObject` for wrappers that collect shared flows.
- SKIE exposes Kotlin sealed subtypes as flattened top-level Swift names, for example `AuthEffectNavigateToHome`, not `AuthEffect.NavigateToHome`.
- Kotlin `data object` subtypes are exposed as Swift singletons via `.shared`.

---

## State Management

Pattern: `Event -> ViewModel.onEvent() -> StateFlow<UiState>` plus one-shot `Effect`.

Rules:

- UI sends events; ViewModels mutate state and emit effects.
- Navigation and snackbars are effects, not boolean flags in state.
- Guard network/write actions before launching coroutines to avoid double-tap races.
- Do not use `mutableStateOf` in shared ViewModels.
- When nullable public state crosses module boundaries, capture it in a local value before using it in smart-cast-sensitive expressions.

---

## Networking

The mobile client uses Ktor in shared code.

Current base URLs from [shared/src/commonMain/kotlin/com/example/hop/network/NetworkConstants.kt](shared/src/commonMain/kotlin/com/example/hop/network/NetworkConstants.kt):

```text
Production: https://hop.ridly.dk/api/v1
Staging:    https://hop.ridly.dk/api/v1
Android emulator local: http://10.0.2.2:3000/api/v1
Physical device local:  http://172.20.10.2:3000/api/v1
```

Current networking rules:

- Auth is Supabase session based. The Ktor auth interceptor reads the current Supabase access token and sends it as a bearer token.
- API calls use response wrappers through `ApiResponse.Success<T>` and `ApiResponse.Error(code, message)`.
- Ktor JSON uses `ignoreUnknownKeys = true`, `isLenient = true`, and `explicitNulls = false`.
- Never surface raw exceptions directly to UI.
- Monetary values remain `Int` in ore throughout domain/data layers. Convert to DKK only at display boundaries.

---

## Dependency Injection

Koin is the DI framework.

- Register ViewModels in [shared/src/commonMain/kotlin/com/example/hop/di/PresentationModule.kt](shared/src/commonMain/kotlin/com/example/hop/di/PresentationModule.kt) with `viewModelOf(::MyViewModel)`.
- Compose screens retrieve ViewModels with `koinViewModel()`.
- iOS retrieves shared dependencies/ViewModels through the accessors in `shared/src/iosMain/kotlin/com/example/hop/di/KoinIOS.kt`.
- Do not suggest Hilt for this KMP project.

---

## Auth

Current decision: **Supabase Auth + Prisma profile guard**.

- Backend auth routes have been removed; request-level auth is handled by `SupabaseGuard`.
- Supabase bearer tokens are verified server-side using Supabase.
- Prisma user profiles are found by Supabase user ID or auto-created from Supabase user metadata when possible.
- Registration collects phone on the client and forwards it through Supabase metadata/profile creation.
- Do not reintroduce custom JWT refresh, Twilio OTP, or MitID unless explicitly requested.

---

## Maps And Routing

Current decision: **backend Mapbox routing/geocoding; client map UI unresolved**.

- Backend places/routing uses Mapbox via `MAPBOX_ACCESS_TOKEN`.
- Client-side map UI/provider selection is not finalized. Verify current Gradle files and screen code before adding, removing, or reintroducing any map SDK.
- Do not claim a final client map-provider decision unless it has been explicitly made.
- Do not switch providers or remove map dependencies without checking current usage and asking when the decision is product-level.

---

## Payments And Settlements

Current MVP decision: **manual P2P MobilePay settlement**.

Current flow:

1. Driver must have an 8-digit Danish MobilePay number before publishing/completing payment-relevant trips.
2. Passenger books a trip.
3. Driver completes the trip.
4. Backend creates `RideSettlement` rows for confirmed bookings and marks bookings `AWAITING_PAYMENT`.
5. Passenger opens MobilePay using the current app flow and marks payment sent.
6. Driver confirms receipt.
7. Booking becomes `COMPLETED`, or either party can dispute.

Important constraints:

- There is no active Vipps MobilePay ePayment API integration in the current code.
- There is no active Stripe integration in MVP.
- Treat payment provider/webhook/capture/refund flows as future work unless the user explicitly asks to implement them.
- Be careful with money formatting: values are stored as integer ore; display DKK at the UI boundary.

---

## Pricing

Current pricing source: [shared/src/commonMain/kotlin/com/example/hop/pricing/PricingEngine.kt](shared/src/commonMain/kotlin/com/example/hop/pricing/PricingEngine.kt).

Current formula:

```kotlin
private const val SKAT_RATE_OERE_PER_METRE = 0.228 // DKK 2.28/km

val totalTripCostOere = (distanceMetres * SKAT_RATE_OERE_PER_METRE).toInt()
val pricePerSeatOere = maxOf(totalTripCostOere / seatsTotal, 100)
```

Do not add platform-fee, tax, payment-hold, or provider-capture assumptions unless those are present in current code or explicitly requested.

---

## Trip Models

### Model A - Recurring Commute

- Driver sets route, recurring day codes, departure time, and seats.
- Backend generates trip instances in a rolling 30-day window.
- Recurring day codes in backend are uppercase values such as `MON`, `TUE`, `WED`, `THU`, `FRI`, `SAT`, `SUN`.
- Client draft names may differ (`recurrenceDays`), so verify DTO mappings before edits.

### Model B - One-Off Threshold Trip

- Driver sets route, departure date/time, total seats, minimum threshold, and threshold deadline.
- Backend requires `minPassengers` and `thresholdDeadline` for Model B.
- Backend schedules a BullMQ threshold check at the deadline.
- If threshold is met, users are notified.
- If threshold is not met, trip becomes `THRESHOLD_NOT_MET`, active bookings are cancelled, and users are notified.

### Seat Atomicity

Current code uses Prisma transactions and aggregate checks, but does **not** yet implement true `SELECT FOR UPDATE` or Redis `INCR` seat pre-checks. Do not state that these are implemented. If working on booking safety, fix the backend concurrency problem at the database/service level and add targeted tests.

---

## Domain Model Notes

Current shared domain models include:

- `User`, `Trip`, `Booking`, `ActiveBooking`, `RideSettlement`, `Rating`, `UserReview`, `HopNotification`, `SavedPlace`, `RecentSearch`, `SearchAlert`, `CarDetails`, `PassengerSummary`, `Aggregates`, tax models, and chat/message models.
- `TripStatus` includes `ACTIVE`, `CONFIRMED`, `CANCELLED`, `COMPLETED`, `THRESHOLD_NOT_MET`, and `UNKNOWN`.
- `BookingStatus` includes `PENDING`, `CONFIRMED`, `AWAITING_PAYMENT`, `CANCELLED`, `COMPLETED`, `DISPUTED`, and `UNKNOWN`.
- Unknown enum values should be handled gracefully, not crashed through UI assumptions.
- `phone: String?` means the phone may be absent. Do not turn it into an empty string unless a specific UI display needs a placeholder.

---

## Backend

Current backend: NestJS 11, TypeScript 5, Prisma 7, PostgreSQL, Redis/BullMQ, Supabase Auth, Railway deployment, APNs/FCM notifications, Socket.IO chat.

Main modules:

- Auth/Supabase guard
- Users
- Trips
- Bookings
- Settlements
- Ratings
- Notifications
- Places
- Search history
- Search alerts
- Aggregates
- Chat
- Admin

Do not suggest microservices for this project.

When editing backend behavior, keep changes inside the modular monolith and add focused tests for high-risk lifecycle or concurrency changes.

---

## Database

Current database is PostgreSQL via Prisma.

- Schema lives in [apps/api/prisma/schema.prisma](apps/api/prisma/schema.prisma).
- Migrations live in `apps/api/prisma/migrations/`.
- Current schema does not consistently implement soft deletes. Do not claim soft-delete behavior exists unless adding it as an explicit change.
- Do not hard-delete important user/trip/booking data casually. If deletion behavior is part of a task, inspect schema/service behavior first and ask before changing policy.
- `DriverLicence.status` currently reuses `BookingStatus`; treat this as a known schema smell, not a desired pattern.

---

## UI And Design Rules

Follow existing component/theme conventions before introducing new styles.

- Use the existing `HopColors`, `HopSpacing`, and typography files for platform UI.
- Current theme includes white/auth surfaces and dark/general surfaces; do not assume every screen is pure white unless the surrounding screen family already is.
- Keep spacing in multiples of 4.
- Preserve safe-area handling on Android and iOS.
- iOS: never use unscoped `.ignoresSafeArea()`; specify edges explicitly.
- iOS page-style `TabView`: keep bottom chrome structurally separated from page content instead of floating it over content with unsafe insets.
- Empty states should be intentional and actionable, not blank lists.
- Add Android `@Preview` / iOS `#Preview` for new screens or major screen rewrites when practical, covering at least default and filled/loading/error states appropriate to the screen.

---

## Current Screen Areas

Onboarding/auth:

- Onboarding
- Sign Up
- Log In
- Forgot Password
- Set New Password

Passenger:

- Home
- Search results
- Trip detail
- Booking confirmation
- Booking success
- My trips
- Active trip detail
- Rate driver
- Cancellation confirmation
- Passenger settlement

Driver:

- Home
- Car details
- MobilePay onboarding/setup
- Post trip model select
- Post trip Model A
- Post trip Model B
- Price review
- My trips
- Active trip detail
- Mark trip complete
- Rate passenger
- Tax dashboard
- Annual tax report download
- Driver settlement

Shared:

- Role toggle / home shell
- Own profile
- Other profile
- In-app chat
- Notifications
- Settings

Admin currently lives in the backend/admin API area, not a verified React/shadcn web app in this repository.

---

## What Not To Do

- Do not suggest React Native, Flutter, Expo, MongoDB, Hilt, Retrofit, NativeWind, Tailwind, or microservices.
- Do not put business logic in `composeApp/` or `iosApp/`.
- Do not put UI code in `shared/`.
- Do not hardcode Gradle dependency versions outside [gradle/libs.versions.toml](gradle/libs.versions.toml).
- Do not store monetary amounts as `Float` or `String` in domain/data layers.
- Do not claim Vipps MobilePay ePayment, Stripe, Twilio OTP, MitID, Redis seat pre-checks, or true DB row locks are implemented unless you verify code first.
- Do not silently resolve conflicts between old documentation and current code. Ask.

---

## Known High-Risk Areas

Use [CODEBASE_EXPLORATION_REPORT.md](CODEBASE_EXPLORATION_REPORT.md) for the latest detailed audit. Current top risks include:

- Booking concurrency can overbook under simultaneous requests.
- Trip completion can partially fail settlement creation after marking a trip completed.
- Manual MobilePay settlement is trust-based and not provider-verified.
- Recurring trip creation is not fully idempotent.
- iOS CI can hide build failures because the build command is piped through `xcpretty || true`.
- Backend tests/build are not currently part of CI.

---

## Scope Discipline

Prefer small, MVP-aligned changes. Avoid scope creep, premature abstraction, and unrelated refactors. When a task touches product policy, payment behavior, auth behavior, data retention, or provider choice, ask before changing the decision.
