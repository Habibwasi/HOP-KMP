# Hop — Agent Instructions

You are working on **Hop**, a carpooling platform for Denmark.
This file is the single source of truth for all architectural and project decisions.
Do not suggest alternatives to decisions already made. Do not ask clarifying questions covered here.

---

## Skills

Always load and apply these skills before generating any code:

- `kmp-compose-multiplatform` — KMP architecture, Ktor, Koin, expect/actual, iOS interop
- `compose-skill` — MVI, Compose UI, state management, navigation, performance, anti-patterns
ui-ux-pro-max skill to make the screens

When a skill suggestion conflicts with a decision in this file, stop and notify me with:
1. What the skill recommends
2. What this file says
3. Your recommendation and why

Do not resolve the conflict silently. Wait for my decision before proceeding.

Additional requirements on every screen:
- Add `@Preview` composables for every screen (empty, loading, error/filled states minimum)
- Apply pro-level UI/UX polish: spacing, visual hierarchy, micro-interactions, accessibility

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
ktor                    = 3.1.3
kotlinx-serialization   = 1.8.1
kotlinx-coroutines      = 1.10.1
koin-bom                = 4.1.1
skie                    = 0.10.11
```

When adding new dependencies, always add them to `gradle/libs.versions.toml` first,
then reference via `libs.*` aliases. Never hardcode version strings in build.gradle.kts files.

---

## Current Dependencies

### shared/build.gradle.kts — commonMain
```kotlin
implementation(platform(libs.koin.bom))
implementation(libs.koin.core)
implementation(libs.ktor.client.core)
implementation(libs.ktor.client.content.negotiation)
implementation(libs.ktor.serialization.kotlinx.json)
implementation(libs.kotlinx.serialization.json)
implementation(libs.kotlinx.coroutines.core)
```

### shared/build.gradle.kts — androidMain
```kotlin
implementation(libs.ktor.client.android)
implementation(platform(libs.koin.bom))
implementation(libs.koin.android)
```

### shared/build.gradle.kts — iosMain
```kotlin
implementation(libs.ktor.client.darwin)
```

### shared/build.gradle.kts — plugins
```kotlin
alias(libs.plugins.kotlinMultiplatform)
alias(libs.plugins.androidLibrary)
alias(libs.plugins.kotlinxSerialization)
alias(libs.plugins.skie)  // co.touchlab.skie version 0.10.11
```

### composeApp/build.gradle.kts — commonMain
```kotlin
implementation(platform(libs.koin.bom))
implementation(libs.koin.compose)
implementation(libs.koin.compose.viewmodel)
implementation(libs.compose.runtime)
implementation(libs.compose.foundation)
implementation(libs.compose.material3)
implementation(libs.compose.ui)
implementation(libs.compose.components.resources)
implementation(libs.compose.uiToolingPreview)
implementation(libs.androidx.lifecycle.viewmodelCompose)
implementation(libs.androidx.lifecycle.runtimeCompose)
implementation(projects.shared)
```

### composeApp/build.gradle.kts — androidMain
```kotlin
implementation(libs.ktor.client.android)
implementation(libs.androidx.activity.compose)
implementation(platform(libs.koin.bom))
implementation(libs.koin.android)
implementation(libs.androidx.core.splashscreen)
implementation(libs.androidx.security.crypto)
```

---

## Architecture Decisions (Final — Do Not Revisit)

### KMP Layer Split
- **shared/commonMain** — Ktor API client, domain models, repository interfaces + implementations,
  pricing engine, trip state machine, auth token storage interface (expect/actual)
- **composeApp** — Jetpack Compose UI screens only. Observes StateFlow from shared ViewModels.
- **iosApp** — SwiftUI screens only. Consumes shared xcframework via SKIE.
- Never put business logic in composeApp. Never put UI code in shared.

### State Management — MVI
Pattern: `Event → ViewModel.onEvent() → StateFlow<UiState>`
- Shared: `StateFlow` + `ViewModel` (`org.jetbrains.androidx.lifecycle.ViewModel` — NOT androidx.lifecycle)
- Android: collect via `collectAsStateWithLifecycle()`
- One-shot effects (navigation, snackbar): `Channel<Effect>(Channel.BUFFERED)` — never boolean flags in state
- Do not use `mutableStateOf` outside of Composables
- ViewModel imports: `org.jetbrains.androidx.lifecycle.ViewModel` and `org.jetbrains.androidx.lifecycle.viewModelScope`

### Networking — Ktor
- `ktor-client-core` in commonMain
- `ktor-client-android` in androidMain
- `ktor-client-darwin` in iosMain
- Base URL: `https://api.hop.dk/v1` (staging: `https://staging-api.hop.dk/v1`)
- All responses envelope: `{ data, error, meta }`
- Auth: Bearer JWT in `Authorization` header
- Monetary values: always **Int in øre** (1 DKK = 100 øre). Never Float. Never String.
- Pagination: cursor-based (`cursor` + `limit` params)
- Error handling: `ApiResponse` sealed class — `Success<T>` / `Error(code, message)`
- `safeApiCall` and `safeEnvelopeCall` wrappers — never raw exceptions in UI
- ContentNegotiation with kotlinx-json: `ignoreUnknownKeys = true`, `isLenient = true`

### Dependency Injection — Koin 4.1.1
- Use Koin BOM — all Koin artifacts inherit version from BOM
- `koin-core` in shared commonMain
- `koin-android` in androidMain
- `koin-compose` + `koin-compose-viewmodel` in composeApp commonMain
- ViewModels registered with `viewModelOf(::MyViewModel)` in Koin modules
- Injected in Compose screens with `koinViewModel()`
- iOS: `fun initKoin()` top-level function in `shared/iosMain/di/KoinIOS.kt`
  called from `iOSApp.swift` as `KoinIOSKt.doInitKoin()`
- Do not suggest Hilt — Android-only, incompatible with KMP

### iOS Swift Interop — SKIE 0.10.11
SKIE automatically converts KMP types to Swift-native equivalents:
- `StateFlow<T>` → Swift `AsyncSequence` (iterate with `for await`)
- `suspend fun` → Swift `async` function
- Sealed classes → Swift enums with exhaustive switch

**SKIE sealed interface/class naming rule (critical):**
SKIE flattens Kotlin nested sealed interface subtypes into **top-level Swift classes** — it does NOT preserve dot-notation nesting. The Swift name is formed by concatenating the parent and subtype names in PascalCase.

| Kotlin | Swift (SKIE) |
|---|---|
| `AuthEffect.NavigateToHome` | `AuthEffectNavigateToHome` |
| `AuthEffect.ShowSnackbar` | `AuthEffectShowSnackbar` |
| `AuthEffect.SessionExpired` | `AuthEffectSessionExpired` |
| `AuthEffect.NavigateToOtpVerification` | `AuthEffectNavigateToOtpVerification` |
| `AuthEvent.Register` | `AuthEventRegister` |
| `AuthEvent.Login` | `AuthEventLogin` |
| `AuthEvent.ClearError` (data object) | `AuthEventClearError.shared` |
| `AuthEvent.Logout` (data object) | `AuthEventLogout.shared` |

Always use the flattened top-level names in Swift `switch` pattern matching:
```swift
// ✅ Correct
case is AuthEffectNavigateToHome:
case let snack as AuthEffectShowSnackbar:

// ❌ Wrong — will not compile
case is AuthEffect.NavigateToHome:
case let snack as AuthEffect.ShowSnackbar:
```

Kotlin `data object` subtypes are exposed as Swift singletons — always access via `.shared`.

**Canonical iOS ViewModel wrapper pattern:**
```swift
// Use @StateObject — NEVER @ObservedObject
// @ObservedObject recreates the wrapper on re-render, cancelling the flow
@MainActor
class AuthViewModelWrapper: ObservableObject {
    let viewModel: AuthViewModel
    @Published var state: AuthUiState

    init() {
        self.viewModel = KoinKt.get()  // Koin provides it
        self.state = viewModel.state.value
    }

    func startObserving() {
        Task {
            // SKIE makes StateFlow directly iterable with for-await
            // No type casts needed — generic type T is preserved
            for await s in viewModel.state {
                self.state = s
            }
        }
        // Also collect Channel<Effect> for navigation:
        Task {
            for await effect in viewModel.effect {
                handleEffect(effect)
            }
        }
    }
}

struct MyScreen: View {
    @StateObject private var wrapper = AuthViewModelWrapper()

    var body: some View {
        // use wrapper.state
    }
    .task { await wrapper.startObserving() }
    // .task cancels automatically when view disappears
}
```

### Auth
- JWT access token: 15-minute lifetime
- Stored in Keychain (iOS) / EncryptedSharedPreferences (Android) via expect/actual
- Refresh token: 30-day lifetime, rotated on every use
- Token refresh interceptor: on 401 response → call `/auth/refresh` → retry original request
  → on refresh failure → clear tokens → emit `AuthEffect.SessionExpired`
- No MitID in MVP — email + phone OTP only (Twilio SMS on backend)
- MitID button visible on ON-02 but disabled

### Maps
- iOS: MapKit (no extra dependency)
- Android: Google Maps SDK for Android
- Do not suggest react-native-maps, Mapbox, or any other library

### Design Tokens
- No NativeWind. No Tailwind. No CSS.
- Tokens defined as Kotlin `object` in commonMain, consumed natively on each platform
- iOS: `extension Color` in `HopColors.swift`, `struct HopSpacing` in `HopSpacing.swift`
- **All screens use a white background theme** — no dark surfaces anywhere in the app.
- Colour palette (global — white theme):
  - Primary Lime:     `#C8F135` (use as fills/badges only — NOT as text/icon colour on white, contrast ~1.4:1)
  - Primary Green:    `#1DB954`
  - Background:       `#FFFFFF` (pure white scaffold on every screen)
  - Surface:          `#F1F3F4` (off-white card / input background)
  - Surface Elevated: `#E8EAED` (slightly darker card elevation)
  - Text Primary:     `#0D0D0D` (~18:1 on white, WCAG AAA)
  - Text Secondary:   `#5F6368` (~7.1:1 on white, WCAG AA)
  - Accent:           `#167A30` (dark green, ~5.3:1 on white, WCAG AA — use for CTAs, links, active states)
  - Input Border:     `#D1D5DB` (subtle cool-gray border)
  - Success:          `#22C55E`
  - Warning:          `#FBBF24`
  - Error:            `#EF4444`
- Typography: Syne (headings), Inter (body), JetBrains Mono (DKK amounts in tax dashboard only)
- Base spacing unit: 4dp. All spacing is multiples of 4.
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
- `recurrenceDays: List<String>?` — values are `"mon"`, `"tue"`, `"wed"`, `"thu"`, `"fri"`
- System creates one trip record per day, 30-day rolling window, auto-extending
- Booking immediately confirmed. Payment charged immediately.
- Driver absorbs occupancy risk — trip runs regardless of seat fill
- Driver can cancel individual days up to 2h before departure

### Model B — One-Off Long Distance
- Driver sets route + date/time + total seats + minimum threshold
- Booking status: `pending`. Payment held by platform.
- BullMQ job (backend) checks every 30 min: if `seats_booked >= min_threshold` → confirm
- 6h before departure: if threshold not met → auto-cancel → all passengers refunded
- PUTW (Pick-Up-on-the-Way): post-MVP only. Do not implement.

### Seat Atomicity
- PostgreSQL `SELECT FOR UPDATE` inside a transaction (backend concern)
- Redis `INCR` pre-check before DB lock
- Never optimistic locking for seat booking

---

## Domain Models (shared/commonMain/domain/model/)

All amounts: Int in øre. All distances: Int in metres. All timestamps: UTC ISO-8601 String.
Display only converts øre → DKK at the UI layer (`amount / 100.0`).

```
User          id, fullName, email, phone: String?, phoneVerified, roles: List<UserRole>,
              isBanned, ratingDriver: Double?, ratingPassenger: Double?

Trip          id, driverId, model: TripModel[A|B], originName, originLat: Double, originLng: Double,
              destName, destLat: Double, destLng: Double, distanceMetres: Int, departsAt: String,
              seatsTotal: Int, seatsBooked: Int, minThreshold: Int?,
              priceOerePerSeat: Int, driverNetOere: Int,
              status: TripStatus[active|confirmed|cancelled|completed],
              recurrenceDays: List<String>?

Booking       id, tripId, passengerId, seats: Int,
              status: BookingStatus[pending|confirmed|cancelled|completed], paymentId: String?

Payment       id, bookingId, passengerId, driverId, amountOere: Int, driverNetOere: Int,
              platformFeeOere: Int, status: PaymentStatus[held|released|refunded|failed],
              mobilepayRef: String?

Rating        id, bookingId, raterId, rateeId, roleRated: RoleRated[driver|passenger],
              stars: Int (1-5), comment: String?

TaxRecord     id, driverId, tripId, bookingId, distanceMetres: Int, grossOere: Int,
              skatRateOere: Int, deductionOere: Int, taxableOere: Int,
              tripDate: String (ISO-8601), taxYear: Int

Message       id, bookingId, senderId, body: String, sentAt: String, readAt: String?
```

All enums annotated with `@Serializable`. Unknown role strings silently dropped via `runCatching`.
`phone: String?` — null means user has not added phone yet. Never use `orEmpty()` on it.

---

## Payment — Vipps MobilePay ePayment API

**IMPORTANT: The old MobilePay AppSwitch SDK is deprecated and no longer available.**
The current payment system is the **Vipps MobilePay ePayment API**.

### How it works
1. Backend calls `POST https://api.vippsmobilepay.com/epayment/v1/payments`
   with `userFlow: "NATIVE_REDIRECT"` and a `returnUrl` pointing back to the Hop app
2. API returns a `redirectUrl` (universal link — standard `https://` URL)
3. Mobile app opens the `redirectUrl`:
   - Android: `Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))`
   - iOS: `UIApplication.shared.open(URL(string: redirectUrl)!)`
4. OS recognises the URL as belonging to MobilePay and automatically opens the app
5. User confirms payment in MobilePay app
6. MobilePay sends webhook (`PaymentReserved`) to backend
7. Backend verifies HMAC-SHA256 signature, processes asynchronously via BullMQ

### Rules
- Always use universal links — never custom URL schemes (`vipps://`, `mobilepay://`)
- Webhooks: respond 200 immediately, process async
- Idempotency: deduplicate by MobilePay payment ID
- Test environment: register at `portal.vippsmobilepay.com` for sandbox MSN + API keys
- Stripe: post-MVP only. Do not implement.

### Payment hold flow (Model B)
```
initiated → PaymentReserved webhook → held → threshold reached → captured (released)
                                           → auto-cancel 6h   → cancelled (refunded)
```

---

## API Endpoints (Backend — NestJS, Do Not Modify)

Base: `https://api.hop.dk/v1`

```
Auth      POST  /auth/register, /auth/login, /auth/refresh, /auth/logout

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

Admin   
          GET   /admin/users
          POST  /admin/users/:id/ban
          POST  /admin/users/:id/unban
          GET   /admin/export/trips
          GET   /admin/export/revenue
```

---

## Screen Inventory (39 screens)
ui-ux-pro-max skill to make the screens

Every screen must have `@Preview` composables (Android) or `#Preview` (iOS).
Minimum 2 previews per screen: empty/default state + loading or filled state.

### Onboarding
ON-01 Onboarding · ON-02 Sign Up · ON-03 Log In ·

### Passenger
PA-01 Home · PA-02 Search Results · PA-03 Trip Detail · PA-04 Booking Confirmation ·
PA-05 MobilePay Handoff · PA-06 Booking Success · PA-07 My Trips · PA-08 Trip Detail Active ·
PA-09 Rate Driver · PA-10 Cancellation Confirmation

### Driver
DR-01 Home · DR-02 Car Details · DR-05 Post Trip Model Select · DR-06 Post Trip Model A ·
DR-07 Post Trip Model B · DR-08 Price Review & Confirm · DR-09 My Trips ·
DR-10 Trip Detail Active · DR-11 Mark Trip Complete · DR-12 Rate Passenger ·
DR-13 Tax Dashboard · DR-14 Annual Tax Report Download

### Shared
SH-01 Home Role Toggle · SH-02 Own Profile · SH-03 Other Profile ·
SH-04 In-App Chat · SH-05 Notifications · SH-06 Settings

### Admin (web — React + shadcn/ui, not mobile)
AD-03 User Search ·
AD-04 Apply Ban · AD-05 Trip & Revenue Export

---

## Key UI Rules

- Role toggle pill always visible on home — passenger ↔ driver switch
- Every monetary amount displayed in DKK: `"DKK %.0f".format(amountOere / 100.0)`
- Tax dashboard: JetBrains Mono font for all DKK amounts
- Empty states: illustration + headline + CTA button. Never a blank list.
- Bottom nav: Home | My Trips | Chat | Profile
- MitID button on ON-02 is visible but disabled — shows "Coming soon" snackbar on tap
- WCAG AA contrast on all text (4.5:1 minimum)
- Bottom safe areas: `navigationBarsPadding()` on Android, `ignoresSafeArea` on iOS
- **iOS TabView page-style layout rule:** Never use `.safeAreaInset(edge: .bottom)` to float chrome over a `TabView` with `.tabViewStyle(.page(...))`. The TabView renders each page as a full-screen view and ignores the inset's safe area, causing text/content to underlap the chrome. **Correct pattern:** place the `TabView` and bottom chrome in a `VStack` flow so they are structurally separated and can never overlap. Apply `.ignoresSafeArea(.all, edges: .top)` (top only) to extend slides under the status bar while keeping the bottom boundary in the natural layout flow. Use a thin `.overlay(alignment: .bottom)` gradient on the `TabView` for visual blending only (`.allowsHitTesting(false)`).
- **iOS `.ignoresSafeArea()` scope rule:** Always specify edges explicitly — `.ignoresSafeArea(.all, edges: .top)` not `.ignoresSafeArea()`. The unscoped form ignores every safe area including ones created by `.safeAreaInset`, `.toolbar`, and system chrome, which causes content to underlap interactive elements.
- All spacing multiples of 4dp
- Error clearing: always call `AuthEvent.ClearError` after showing an error snackbar
- Back stack: use `popUpTo(0) { inclusive = true }` when navigating to home after login

---

## Shared Module File Structure

```
shared/src/commonMain/kotlin/com/example/hop/
  domain/
    model/          ← User, Trip, Booking, Payment, Rating, TaxRecord, Message + enums
    repository/     ← AuthRepository, TripRepository, BookingRepository, TaxRepository interfaces
  data/
    dto/            ← @Serializable DTOs + toDomain() mappers
    repository/     ← *RepositoryImpl classes
    local/          ← TokenStorage interface (expect/actual)
  network/
    ApiResponse.kt          ← sealed class Success<T> / Error(code, message)
    HttpClientFactory.kt    ← creates Ktor client
    AuthInterceptor.kt      ← appends Bearer JWT
    NetworkConstants.kt     ← base URLs, timeout
    TokenStorage.kt         ← interface: get/save access+refresh tokens
  pricing/
    PricingEngine.kt        ← canonical formula (do not alter)
  presentation/
    auth/           ← AuthViewModel, AuthUiState, AuthEvent, AuthEffect
    trip/           ← TripViewModel
    search/         ← SearchViewModel
    booking/        ← BookingViewModel
    driver/         ← DriverViewModel
    tax/            ← TaxViewModel
    chat/           ← ChatViewModel
  di/
    NetworkModule.kt
    RepositoryModule.kt
    PresentationModule.kt
    AppModule.kt            ← val appModules = listOf(...)
```

```
shared/src/androidMain/kotlin/com/example/hop/
  data/local/TokenStorageImpl.kt  ← EncryptedSharedPreferences

shared/src/iosMain/kotlin/com/example/hop/
  data/local/TokenStorageImpl.kt  ← Keychain via Security framework
  di/KoinIOS.kt                   ← fun initKoin() called from Swift
```

---

## Backend (Separate — Do Not Generate Unless Asked)

NestJS 10, TypeScript 5, Prisma 7, PostgreSQL 16, Redis 7, BullMQ
Railway (Docker, auto-deploy on push), Supabase Postgres + Auth, Railway Redis plugin
Modular monolith: Auth, Trips, Payments, Ratings, Chat, Notifications, Tax, Admin modules
Do not suggest microservices.

---

## What NOT To Do

- Do not suggest React Native, Flutter, or Expo
- Do not suggest MongoDB — PostgreSQL is final
- Do not suggest Hilt — Koin is the DI framework
- Do not suggest Retrofit — Ktor is the HTTP client
- Do not suggest Room or DataStore — no local persistence in MVP, all data from REST API
- Do not suggest Stripe in MVP — Vipps MobilePay ePayment only
- Do not suggest the old MobilePay AppSwitch SDK — it is deprecated and unavailable
- Do not use custom URL schemes (`vipps://`, `mobilepay://`) — use universal links only
- Do not suggest NativeWind, Tailwind, or any CSS framework
- Do not suggest react-native-maps or Mapbox
- Do not suggest microservices
- Do not implement PUTW in MVP
- Do not implement MitID in MVP
- Do not use `androidx.lifecycle.ViewModel` — use `org.jetbrains.androidx.lifecycle.ViewModel`
- Do not use `@ObservedObject` for ViewModel wrappers in SwiftUI — use `@StateObject`
- Do not use dot-notation for SKIE sealed subtypes in Swift (e.g. `AuthEffect.NavigateToHome`) — use the flattened top-level name (`AuthEffectNavigateToHome`); dot-notation will not compile
- Do not use unscoped `.ignoresSafeArea()` in iOS — always specify edges (e.g. `.ignoresSafeArea(.all, edges: .top)`); the unscoped form ignores all safe areas including `.safeAreaInset` chrome
- Do not float bottom chrome over a `TabView(.page)` using `.safeAreaInset` — use a `VStack` flow instead (see Key UI Rules)
- Do not store monetary amounts as Float or String — always Int in øre
- Do not use `phone.orEmpty()` — phone is nullable, pass it through as-is
- Do not hard-delete User, Trip, or Booking records — soft delete only (deleted_at)
- Do not put business logic in composeApp or iosApp
- Do not hardcode version strings in build.gradle.kts — use libs.versions.toml aliases
- Do not skip `@Preview` annotations on screens

---

## Scope Discipline

Every decision should be evaluated against MVP scope.
Scope creep, premature optimisation, and over-engineering are the primary risks.
