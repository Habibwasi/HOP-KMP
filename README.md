# Hop — Carpooling by Ridly

Hop is a peer-to-peer carpooling platform for Android and iOS, built with Kotlin Multiplatform and a NestJS backend. Drivers post trips and passengers book seats, with payments handled directly via MobilePay.

---

## Repository Structure

```
HOP-KMP/
├── apps/api/          # NestJS backend (TypeScript, Prisma, PostgreSQL)
├── composeApp/        # Android Compose Multiplatform UI
├── iosApp/            # iOS SwiftUI app
├── shared/            # KMP shared business logic (domain, data, ViewModels)
└── infra/             # Deployment configuration
```

### [`/apps/api`](./apps/api)
NestJS REST + WebSocket backend.

| Module | Description |
|---|---|
| `auth` | Supabase JWT authentication |
| `trips` | Trip creation, search, and lifecycle |
| `bookings` | Booking requests and confirmations |
| `settlements` | Post-trip payment settlement flows |
| `ratings` | Passenger ↔ driver mutual ratings |
| `chat` | Real-time messaging via Socket.IO |
| `notifications` | Push notifications (BullMQ queue) |
| `users` | Profile management |
| `places` | Google Places autocomplete proxy |
| `search-alerts` | Saved search alert subscriptions |
| `help-center` | FAQ endpoint (public, no auth required) |
| `admin` | Internal admin operations |

### [`/shared`](./shared/src/commonMain/kotlin/com/example/hop)
KMP shared module consumed by both Android and iOS.

```
shared/src/commonMain/
├── domain/
│   ├── model/          # Domain data classes
│   └── repository/     # Repository interfaces
├── data/
│   ├── dto/            # API response DTOs
│   └── repository/     # Ktor repository implementations
├── presentation/       # MVI ViewModels (state / events / effects)
└── di/                 # Koin modules (repository + presentation)
```

### [`/composeApp`](./composeApp/src/commonMain/kotlin/com/example/hop/ui)
Android UI in Compose Multiplatform with type-safe Navigation Compose.

**Screens:**

| Area | Screens |
|---|---|
| Auth | Onboarding, Sign Up, Login, Verify Email, Forgot Password, Set New Password |
| Passenger | Home, Search Results, Trip Detail, Booking Confirmation, Booking Success, Active Trip, MobilePay Handoff, Rate Driver, Cancellation Confirmation |
| Driver | Home, Post Trip (Model A / B / Select), Price Review, Car Details, MobilePay Setup, My Trips, Active Trip Detail, Mark Complete, Rate Passenger, Review Pending, Tax Dashboard, Tax Report Download |
| Settlement | Passenger Settlement, Driver Settlement, Past Trip Detail |
| Shared | Chat, Chat List, Notifications, Profile, Other Profile, Settings, Help Center, Privacy Policy, Terms of Service |

### [`/iosApp`](./iosApp/iosApp)
Native SwiftUI app. ViewModels are shared from KMP via `KoinIOS.kt` accessors and wrapped in `@MainActor ObservableObject` wrappers (`*ViewModelWrapper.swift`).

---

## App Screens

Every screen state below is rendered from the Android app's Compose `@Preview` functions (Pixel 5, Robolectric) — the real UI with sample data, not mockups. To refresh:

```bash
./gradlew :composeApp:recordRoborazziDebug
python scripts/screenshots_to_readme.py
```

<!-- screenshots:start -->

<details open>
<summary><b>Onboarding & account</b> — 6 screens, 13 states</summary>

<table>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/onboarding--light.png" width="180" alt="Onboarding — Light"><br><b>Onboarding</b><br><sub>Light</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/sign-up--empty.png" width="180" alt="Sign Up — Empty"><br><b>Sign Up</b><br><sub>Empty</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/sign-up--filled.png" width="180" alt="Sign Up — Filled"><br><b>Sign Up</b><br><sub>Filled</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/sign-up--loading.png" width="180" alt="Sign Up — Loading"><br><b>Sign Up</b><br><sub>Loading</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/login--empty.png" width="180" alt="Login — Empty"><br><b>Login</b><br><sub>Empty</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/login--error.png" width="180" alt="Login — Error"><br><b>Login</b><br><sub>Error</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/login--loading.png" width="180" alt="Login — Loading"><br><b>Login</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/verify-email--cooldown.png" width="180" alt="Verify Email — Cooldown"><br><b>Verify Email</b><br><sub>Cooldown</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/verify-email--ready.png" width="180" alt="Verify Email — Ready"><br><b>Verify Email</b><br><sub>Ready</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/email-verified--default.png" width="180" alt="Email Verified — Default"><br><b>Email Verified</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/forgot-password--empty.png" width="180" alt="Forgot Password — Empty"><br><b>Forgot Password</b><br><sub>Empty</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/forgot-password--loading.png" width="180" alt="Forgot Password — Loading"><br><b>Forgot Password</b><br><sub>Loading</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/auth/forgot-password--success.png" width="180" alt="Forgot Password — Success"><br><b>Forgot Password</b><br><sub>Success</sub></td></tr>
</table>
</details>

<details>
<summary><b>Home, chat & profile</b> — 7 screens, 17 states</summary>

<table>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/home--driver-role-driver-selected.png" width="180" alt="Home — Driver role, driver selected"><br><b>Home</b><br><sub>Driver role, driver selected</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/home--driver-role-passenger-selected.png" width="180" alt="Home — Driver role, passenger selected"><br><b>Home</b><br><sub>Driver role, passenger selected</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/home--passenger-only-no-driver-role.png" width="180" alt="Home — Passenger only (no driver role)"><br><b>Home</b><br><sub>Passenger only (no driver role)</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/notifications--empty-state.png" width="180" alt="Notifications — Empty State"><br><b>Notifications</b><br><sub>Empty State</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/notifications--loading.png" width="180" alt="Notifications — Loading"><br><b>Notifications</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/notifications--with-items.png" width="180" alt="Notifications — With Items"><br><b>Notifications</b><br><sub>With Items</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/chat--default.png" width="180" alt="Chat — Default"><br><b>Chat</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/chat--empty.png" width="180" alt="Chat — Empty"><br><b>Chat</b><br><sub>Empty</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/chat--reconnecting.png" width="180" alt="Chat — Reconnecting"><br><b>Chat</b><br><sub>Reconnecting</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/own-profile--default.png" width="180" alt="Own Profile — Default"><br><b>Own Profile</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/own-profile--editing-name.png" width="180" alt="Own Profile — Editing Name"><br><b>Own Profile</b><br><sub>Editing Name</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/other-profile--default.png" width="180" alt="Other Profile — Default"><br><b>Other Profile</b><br><sub>Default</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/other-profile--report-dialog.png" width="180" alt="Other Profile — Report Dialog"><br><b>Other Profile</b><br><sub>Report Dialog</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/settings--default.png" width="180" alt="Settings — Default"><br><b>Settings</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/settings--logout-dialog.png" width="180" alt="Settings — Logout dialog"><br><b>Settings</b><br><sub>Logout dialog</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/settings--notifications-off.png" width="180" alt="Settings — Notifications off"><br><b>Settings</b><br><sub>Notifications off</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/shared/error--default.png" width="180" alt="Error — Default"><br><b>Error</b><br><sub>Default</sub></td></tr>
</table>
</details>

<details>
<summary><b>Passenger</b> — 9 screens, 29 states</summary>

<table>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/passenger-home--empty-state.png" width="180" alt="Passenger Home — Empty state"><br><b>Passenger Home</b><br><sub>Empty state</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/passenger-home--loading.png" width="180" alt="Passenger Home — Loading"><br><b>Passenger Home</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/search-results--empty.png" width="180" alt="Search Results — Empty"><br><b>Search Results</b><br><sub>Empty</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/search-results--loading.png" width="180" alt="Search Results — Loading"><br><b>Search Results</b><br><sub>Loading</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/search-results--with-results.png" width="180" alt="Search Results — With Results"><br><b>Search Results</b><br><sub>With Results</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/trip-detail--loading.png" width="180" alt="Trip Detail — Loading"><br><b>Trip Detail</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/trip-detail--model-a.png" width="180" alt="Trip Detail — Model A"><br><b>Trip Detail</b><br><sub>Model A</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/trip-detail--model-b.png" width="180" alt="Trip Detail — Model B"><br><b>Trip Detail</b><br><sub>Model B</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/trip-detail--one-seat-left.png" width="180" alt="Trip Detail — One Seat Left"><br><b>Trip Detail</b><br><sub>One Seat Left</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/booking-confirmation--loading.png" width="180" alt="Booking Confirmation — Loading"><br><b>Booking Confirmation</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/booking-confirmation--model-a.png" width="180" alt="Booking Confirmation — Model A"><br><b>Booking Confirmation</b><br><sub>Model A</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/booking-confirmation--model-b.png" width="180" alt="Booking Confirmation — Model B"><br><b>Booking Confirmation</b><br><sub>Model B</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/booking-confirmation--model-b-threshold-met.png" width="180" alt="Booking Confirmation — Model B Threshold Met"><br><b>Booking Confirmation</b><br><sub>Model B Threshold Met</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/booking-confirmation--processing.png" width="180" alt="Booking Confirmation — Processing"><br><b>Booking Confirmation</b><br><sub>Processing</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/booking-success--loading.png" width="180" alt="Booking Success — Loading"><br><b>Booking Success</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/booking-success--model-a-confirmed.png" width="180" alt="Booking Success — Model A — Confirmed"><br><b>Booking Success</b><br><sub>Model A — Confirmed</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/booking-success--model-b-pending.png" width="180" alt="Booking Success — Model B — Pending"><br><b>Booking Success</b><br><sub>Model B — Pending</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/my-trips-passenger--empty-upcoming.png" width="180" alt="My Trips Passenger — Empty upcoming"><br><b>My Trips Passenger</b><br><sub>Empty upcoming</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/my-trips-passenger--loading.png" width="180" alt="My Trips Passenger — Loading"><br><b>My Trips Passenger</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/my-trips-passenger--past-trips.png" width="180" alt="My Trips Passenger — Past trips"><br><b>My Trips Passenger</b><br><sub>Past trips</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/my-trips-passenger--upcoming-trips.png" width="180" alt="My Trips Passenger — Upcoming trips"><br><b>My Trips Passenger</b><br><sub>Upcoming trips</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/trip-detail-active--confirmed.png" width="180" alt="Trip Detail Active — Confirmed"><br><b>Trip Detail Active</b><br><sub>Confirmed</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/trip-detail-active--loading.png" width="180" alt="Trip Detail Active — Loading"><br><b>Trip Detail Active</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/trip-detail-active--model-b-pending.png" width="180" alt="Trip Detail Active — Model B Pending"><br><b>Trip Detail Active</b><br><sub>Model B Pending</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/cancellation-confirmation--loading.png" width="180" alt="Cancellation Confirmation — Loading"><br><b>Cancellation Confirmation</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/cancellation-confirmation--model-a.png" width="180" alt="Cancellation Confirmation — Model A"><br><b>Cancellation Confirmation</b><br><sub>Model A</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/cancellation-confirmation--model-b.png" width="180" alt="Cancellation Confirmation — Model B"><br><b>Cancellation Confirmation</b><br><sub>Model B</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/rate-driver--default.png" width="180" alt="Rate Driver — Default"><br><b>Rate Driver</b><br><sub>Default</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/passenger/rate-driver--stars-selected-comment.png" width="180" alt="Rate Driver — Stars selected + comment"><br><b>Rate Driver</b><br><sub>Stars selected + comment</sub></td></tr>
</table>
</details>

<details>
<summary><b>Driver</b> — 14 screens, 31 states</summary>

<table>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/driver-home--earnings-banner.png" width="180" alt="Driver Home — Earnings Banner"><br><b>Driver Home</b><br><sub>Earnings Banner</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/driver-home--empty.png" width="180" alt="Driver Home — Empty"><br><b>Driver Home</b><br><sub>Empty</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/driver-home--loading.png" width="180" alt="Driver Home — Loading"><br><b>Driver Home</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/driver-home--non-driver.png" width="180" alt="Driver Home — Non Driver"><br><b>Driver Home</b><br><sub>Non Driver</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/driver-home--with-trips.png" width="180" alt="Driver Home — With Trips"><br><b>Driver Home</b><br><sub>With Trips</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/review-pending--default.png" width="180" alt="Review Pending — Default"><br><b>Review Pending</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/car-details--default.png" width="180" alt="Car Details — Default"><br><b>Car Details</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/enable-driver-mobilepay--default.png" width="180" alt="Enable Driver MobilePay — Default"><br><b>Enable Driver MobilePay</b><br><sub>Default</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/post-trip-model-select--default.png" width="180" alt="Post Trip Model Select — Default"><br><b>Post Trip Model Select</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/post-trip-model-a--default.png" width="180" alt="Post Trip Model A — Default"><br><b>Post Trip Model A</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/post-trip-model-b--default.png" width="180" alt="Post Trip Model B — Default"><br><b>Post Trip Model B</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/price-review--model-a.png" width="180" alt="Price Review — Model A"><br><b>Price Review</b><br><sub>Model A</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/price-review--model-b.png" width="180" alt="Price Review — Model B"><br><b>Price Review</b><br><sub>Model B</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/price-review--price-not-yet-resolved.png" width="180" alt="Price Review — Price not yet resolved"><br><b>Price Review</b><br><sub>Price not yet resolved</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/my-trips-driver--empty-upcoming.png" width="180" alt="My Trips Driver — Empty upcoming"><br><b>My Trips Driver</b><br><sub>Empty upcoming</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/my-trips-driver--loading.png" width="180" alt="My Trips Driver — Loading"><br><b>My Trips Driver</b><br><sub>Loading</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/my-trips-driver--model-b-threshold-met.png" width="180" alt="My Trips Driver — Model B threshold met"><br><b>My Trips Driver</b><br><sub>Model B threshold met</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/my-trips-driver--past-trips.png" width="180" alt="My Trips Driver — Past trips"><br><b>My Trips Driver</b><br><sub>Past trips</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/my-trips-driver--upcoming-trips-model-a-b.png" width="180" alt="My Trips Driver — Upcoming trips (Model A + B)"><br><b>My Trips Driver</b><br><sub>Upcoming trips (Model A + B)</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/trip-detail-active-driver--default.png" width="180" alt="Trip Detail Active Driver — Default"><br><b>Trip Detail Active Driver</b><br><sub>Default</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/trip-detail-active-driver--loading.png" width="180" alt="Trip Detail Active Driver — Loading"><br><b>Trip Detail Active Driver</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/mark-trip-complete--default.png" width="180" alt="Mark Trip Complete — Default"><br><b>Mark Trip Complete</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/mark-trip-complete--loading.png" width="180" alt="Mark Trip Complete — Loading"><br><b>Mark Trip Complete</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/rate-passenger--default.png" width="180" alt="Rate Passenger — Default"><br><b>Rate Passenger</b><br><sub>Default</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/rate-passenger--stars-selected.png" width="180" alt="Rate Passenger — Stars selected"><br><b>Rate Passenger</b><br><sub>Stars selected</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/tax-dashboard--default.png" width="180" alt="Tax Dashboard — Default"><br><b>Tax Dashboard</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/tax-dashboard--error.png" width="180" alt="Tax Dashboard — Error"><br><b>Tax Dashboard</b><br><sub>Error</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/tax-dashboard--loading.png" width="180" alt="Tax Dashboard — Loading"><br><b>Tax Dashboard</b><br><sub>Loading</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/tax-report-download--error.png" width="180" alt="Tax Report Download — Error"><br><b>Tax Report Download</b><br><sub>Error</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/tax-report-download--loading.png" width="180" alt="Tax Report Download — Loading"><br><b>Tax Report Download</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/driver/tax-report-download--ready.png" width="180" alt="Tax Report Download — Ready"><br><b>Tax Report Download</b><br><sub>Ready</sub></td></tr>
</table>
</details>

<details>
<summary><b>After the ride: payment</b> — 3 screens, 8 states</summary>

<table>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/settlement/passenger-settlement--awaiting-payment.png" width="180" alt="Passenger Settlement — Awaiting payment"><br><b>Passenger Settlement</b><br><sub>Awaiting payment</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/settlement/passenger-settlement--loading.png" width="180" alt="Passenger Settlement — Loading"><br><b>Passenger Settlement</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/settlement/passenger-settlement--marked-as-paid.png" width="180" alt="Passenger Settlement — Marked as paid"><br><b>Passenger Settlement</b><br><sub>Marked as paid</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/settlement/driver-settlement--all-waiting.png" width="180" alt="Driver Settlement — All waiting"><br><b>Driver Settlement</b><br><sub>All waiting</sub></td></tr>
<tr><td align="center" valign="top" width="25%"><img src="docs/screenshots/settlement/driver-settlement--loading.png" width="180" alt="Driver Settlement — Loading"><br><b>Driver Settlement</b><br><sub>Loading</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/settlement/driver-settlement--mixed-payment-status.png" width="180" alt="Driver Settlement — Mixed payment status"><br><b>Driver Settlement</b><br><sub>Mixed payment status</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/settlement/past-trip-detail-driver--default.png" width="180" alt="Past Trip Detail Driver — Default"><br><b>Past Trip Detail Driver</b><br><sub>Default</sub></td><td align="center" valign="top" width="25%"><img src="docs/screenshots/settlement/past-trip-detail-driver--loading.png" width="180" alt="Past Trip Detail Driver — Loading"><br><b>Past Trip Detail Driver</b><br><sub>Loading</sub></td></tr>
</table>
</details>

<!-- screenshots:end -->

---

## Tech Stack

| Layer | Technology |
|---|---|
| Android UI | Compose Multiplatform 1.10.3 |
| iOS UI | SwiftUI (NavigationStack) |
| Shared logic | Kotlin Multiplatform, Kotlin 2.3.20 |
| Networking | Ktor 3.1.3 |
| Dependency injection | Koin |
| Serialization | kotlinx.serialization 1.8.1 |
| Async | kotlinx.coroutines 1.10.1 |
| Backend | NestJS 11, TypeScript |
| ORM | Prisma 7.7 + PostgreSQL |
| Auth | Supabase |
| Realtime | Socket.IO 4.8 |
| Job queue | BullMQ |
| Observability | Sentry (Android, iOS, API) |
| Min Android SDK | 24 (Android 7.0) |
| Target Android SDK | 36 |

---

## Getting Started

### Prerequisites

- Android Studio Meerkat or newer
- Xcode 16+
- Node.js 20+
- PostgreSQL 15+
- A Supabase project

### Environment Setup

**Backend** — copy and populate `apps/api/.env`:
```env
DATABASE_URL=postgresql://...
SUPABASE_URL=https://<project>.supabase.co
SUPABASE_SERVICE_KEY=...
GOOGLE_PLACES_API_KEY=...
SENTRY_DSN=...
```

**Android** — `local.properties`:
```
MAPS_API_KEY=...
SENTRY_DSN=...
```

**iOS** — `iosApp/Configuration/Config.xcconfig`:
```
SENTRY_DSN = ...
```

### Running the API

```bash
cd apps/api
npm install
npx prisma migrate deploy
npm run start:dev
```

### Building the Android App

```bash
# Debug APK
./gradlew :composeApp:assembleDebug

# Release bundle (requires signing config in local.properties)
./gradlew :composeApp:bundleRelease
```

### Building the iOS App

Open `iosApp/iosApp.xcodeproj` in Xcode and run, or build from the terminal:

```bash
xcodebuild build \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'generic/platform=iOS'
```

> The KMP shared XCFramework is built automatically via the Gradle task `:shared:assembleSharedXCFramework`.

---

## Architecture

The app follows MVI (Model-View-Intent) with a clean separation of concerns:

```
UI Layer  ──►  ViewModel (shared KMP)  ──►  Repository Interface
                    │                              │
              emits UiState                  Repository Impl (Ktor)
              emits Effects                        │
                                             NestJS API
```

- **State** — immutable data class; UI observes and renders it
- **Events** — user actions sent into the ViewModel
- **Effects** — one-shot side-effects (navigation, toasts, deep links)

All API responses are wrapped in `{ "data": T }` by a global NestJS `TransformInterceptor`. The KMP layer unwraps them via `ApiEnvelope<T>` and `safeEnvelopeCall`.

---

## Learn More

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [NestJS](https://nestjs.com/)
- [Ktor](https://ktor.io/)
- [Koin](https://insert-koin.io/)
