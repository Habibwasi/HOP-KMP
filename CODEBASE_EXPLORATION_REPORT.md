# HOP-KMP Codebase Exploration Report

Date: 17 May 2026

Scope: frontend, backend, and DB exploration of authored files in the workspace. Generated/dependency output such as `build/`, `.gradle/`, `.git/`, `apps/api/dist/`, and `node_modules/` was excluded from the authored-code inventory.

Authored-file inventory used for exploration:

| Area | Count |
| --- | ---: |
| Total authored files | 714 |
| `composeApp/` | 107 |
| `shared/` | 132 |
| `iosApp/` | 101 |
| `apps/api/` | 105 |
| Other config/docs/assets/project files | 269 |

## Executive Summary

HOP-KMP is a Kotlin Multiplatform carpooling product with Android Compose UI, iOS SwiftUI UI, shared KMP domain/data/presentation code, and a NestJS API backed by PostgreSQL through Prisma. The main app shape is strong: clean layers, Koin DI, typed navigation, Supabase auth, Mapbox routing/geocoding, Redis/BullMQ background jobs, push notifications, chat, recurring trips, threshold trips, settlement flow, ratings, saved places, recent searches, and admin/reporting surfaces.

The frontend is the healthiest layer. Koin ViewModel registration, phone propagation, idempotency guards, effect handling, and monetary display are mostly correct. The main frontend risks are unfinished placeholder routes/screens and some fragile deep-link/settlement UX edges.

The backend and DB contain the most important risks: booking concurrency can overbook, settlement creation can partially fail after completing a trip, several payment flows are manual-only, DB relations lack cascade policies where the business model expects lifecycle coupling, CI can hide iOS build failures, and some migration/schema choices are destructive or semantically confusing.

## Step 1: Frontend Exploration

### Frontend Architecture

The frontend is split into three layers:

| Layer | Responsibility |
| --- | --- |
| `composeApp/` | Android/Compose Multiplatform app shell, navigation graph, screens, reusable UI components, Android-specific push/deep-link entry points. |
| `shared/` | KMP shared domain models, repository interfaces/implementations, DTOs, networking, token storage abstractions, Koin modules, ViewModels and state/effects. |
| `iosApp/` | SwiftUI app shell, navigation stack, screen implementations, wrapper objects around shared KMP ViewModels. |

Key files:

- [composeApp/src/androidMain/kotlin/com/example/hop/MainActivity.kt](composeApp/src/androidMain/kotlin/com/example/hop/MainActivity.kt): Android entry point and deep-link intent handling.
- [composeApp/src/androidMain/kotlin/com/example/hop/App.kt](composeApp/src/androidMain/kotlin/com/example/hop/App.kt): Android app composition, Koin/ViewModel wiring, auth/navigation coordination.
- [composeApp/src/commonMain/kotlin/com/example/hop/ui/navigation/HopNavGraph.kt](composeApp/src/commonMain/kotlin/com/example/hop/ui/navigation/HopNavGraph.kt): main Compose navigation graph.
- [composeApp/src/commonMain/kotlin/com/example/hop/ui/navigation/HopRoutes.kt](composeApp/src/commonMain/kotlin/com/example/hop/ui/navigation/HopRoutes.kt): typed route definitions.
- [composeApp/src/commonMain/kotlin/com/example/hop/ui/navigation/AuthEffectHandler.kt](composeApp/src/commonMain/kotlin/com/example/hop/ui/navigation/AuthEffectHandler.kt): global auth effects to navigation.
- [shared/src/commonMain/kotlin/com/example/hop/di/PresentationModule.kt](shared/src/commonMain/kotlin/com/example/hop/di/PresentationModule.kt): Koin ViewModel registration.
- [shared/src/commonMain/kotlin/com/example/hop/network/HttpClientFactory.kt](shared/src/commonMain/kotlin/com/example/hop/network/HttpClientFactory.kt): shared Ktor client configuration.
- [shared/src/commonMain/kotlin/com/example/hop/network/NetworkConstants.kt](shared/src/commonMain/kotlin/com/example/hop/network/NetworkConstants.kt): API base URLs and timeouts.
- [iosApp/iosApp/iOSApp.swift](iosApp/iosApp/iOSApp.swift): iOS entry point and Koin initialization.
- [iosApp/iosApp/ContentView.swift](iosApp/iosApp/ContentView.swift): iOS auth/main app split.
- [iosApp/iosApp/Navigation/HopNavigationStack.swift](iosApp/iosApp/Navigation/HopNavigationStack.swift): iOS navigation mapping.

### Main Frontend Flows

Auth flow:

1. Splash/onboarding screens lead to login/signup.
2. Signup collects first name, last name, email, phone, and password.
3. Shared `AuthViewModel` calls Supabase auth and posts/auto-creates a Prisma profile.
4. Auth effects navigate to home, reset password, or show snackbars.

Passenger flow:

1. Passenger home shows saved places, recent searches, active booking, and search entry points.
2. Search flow sends route/d 

Driver flow:

1. Driver home shows stats, trips, demand/aggregates, and posting entry points.
2. Driver onboarding collects car details and MobilePay number.
3. Posting supports Model A recurring trips and Model B threshold trips.
4. Route distance is calculated before posting; backend stores system price.
5. Driver completes trip, backend creates settlements, and driver confirms payment receipt.

### Frontend Checks

| Check | Result |
| --- | --- |
| Koin ViewModel registration | Pass. `koinViewModel` usages have matching `viewModelOf` registrations. |
| Auth phone propagation | Pass. Phone exists in signup UI, event, shared repository metadata/profile creation. |
| Monetary display | Pass for true money fields. Amounts remain integer øre upstream; `.toFloat()` hits found were progress/rating/chart values. |
| ViewModel double-tap guards | Mostly pass. Booking, posting, settlement, rating, cancel, and complete actions have state guards before launches. |
| Effect handling | Pass in reviewed Compose routes; effects are generally exhaustively handled. |
| `LaunchedEffect(true)` | No issue found. |
| Cross-module smart-cast risk | Low. Nullable thresholds are used safely in current files, but should be captured into local vals if refactored across modules. |

### Frontend Issues And Risks

| ID | Severity | File | Finding | Suggested fix |
| --- | --- | --- | --- | --- |
| FE-001 | Medium | [composeApp/src/commonMain/kotlin/com/example/hop/ui/navigation/HopNavGraph.kt](composeApp/src/commonMain/kotlin/com/example/hop/ui/navigation/HopNavGraph.kt), [iosApp/iosApp/Navigation/HopNavigationStack.swift](iosApp/iosApp/Navigation/HopNavigationStack.swift) | Placeholder/TODO route support remains. Users may hit unfinished screens for some routes/workflows. | Replace placeholder route mappings with real screens or hide routes until implemented. |
| FE-002 | Medium | [composeApp/src/commonMain/kotlin/com/example/hop/ui/components/home/MapPreviewCard.kt](composeApp/src/commonMain/kotlin/com/example/hop/ui/components/home/MapPreviewCard.kt) | Map preview is explicitly a placeholder even though Android dependencies still include Google Maps/Places while backend recently switched to Mapbox. | Decide on one map provider for client UI, remove stale Google dependencies if unused, and implement actual map preview. |
| FE-003 | Low | [shared/src/commonMain/kotlin/com/example/hop/presentation/settlement/SettlementViewModel.kt](shared/src/commonMain/kotlin/com/example/hop/presentation/settlement/SettlementViewModel.kt) | MobilePay deep link uses integer DKK via `suggestedAmountOere / 100`, dropping øre remainder if any total is not divisible by 100. | Format decimal DKK or ensure backend totals are always whole DKK before settlement. |
| FE-004 | Low | [shared/src/commonMain/kotlin/com/example/hop/presentation/tripdetail/TripDetailViewModel.kt](shared/src/commonMain/kotlin/com/example/hop/presentation/tripdetail/TripDetailViewModel.kt), [shared/src/commonMain/kotlin/com/example/hop/presentation/tripdetailactive/TripDetailActiveViewModel.kt](shared/src/commonMain/kotlin/com/example/hop/presentation/tripdetailactive/TripDetailActiveViewModel.kt) | Model B progress uses current booked seats. For confirmation UX, passenger-facing progress after booking is handled elsewhere, but this distinction is easy to regress. | Keep `seatsAfterBooking = seatsBooked + requestedSeats` in confirmation UI and document the difference between current and projected progress. |

## Step 2: Backend Exploration

### Backend Architecture

The backend is a NestJS API under `apps/api/` with global validation, response wrapping, Supabase auth, Prisma, Redis/BullMQ queues, Socket.IO chat, and notification integrations.

Key files:

- [apps/api/src/main.ts](apps/api/src/main.ts): bootstrap, global prefix `/api/v1`, validation pipe, exception filter, transform interceptor, dynamic local port fallback.
- [apps/api/src/app.module.ts](apps/api/src/app.module.ts): top-level module, Redis/BullMQ setup, all feature modules.
- [apps/api/src/auth/supabase.guard.ts](apps/api/src/auth/supabase.guard.ts): bearer token validation via Supabase and Prisma profile auto-creation.
- [apps/api/src/trips/trips.service.ts](apps/api/src/trips/trips.service.ts): trip creation/search/cancel/complete, recurring expansion, settlement creation, alert queueing.
- [apps/api/src/bookings/bookings.service.ts](apps/api/src/bookings/bookings.service.ts): booking creation/confirmation/cancellation, Model B threshold checks.
- [apps/api/src/settlements/settlements.service.ts](apps/api/src/settlements/settlements.service.ts): manual MobilePay settlement state transitions.
- [apps/api/src/notifications/notifications.service.ts](apps/api/src/notifications/notifications.service.ts): APNs/FCM push and notification persistence support.
- [apps/api/src/chat/chat.gateway.ts](apps/api/src/chat/chat.gateway.ts): booking-scoped realtime chat.
- [apps/api/src/search-alerts/search-alerts.service.ts](apps/api/src/search-alerts/search-alerts.service.ts): route alert matching and notifications.
- [apps/api/src/places/places.service.ts](apps/api/src/places/places.service.ts): Mapbox geocoding/routing integration.

### Backend Modules

| Module | Responsibility |
| --- | --- |
| `AuthModule` | Supabase auth support and legacy JWT strategy files. |
| `UsersModule` | Profile creation/update, car details, MobilePay number, user lookup. |
| `TripsModule` | Driver trip lifecycle, search, recurring expansion, completion, settlement creation. |
| `BookingsModule` | Passenger booking lifecycle and Model B threshold queue processing. |
| `SettlementsModule` | Manual P2P MobilePay settlement marking, confirmation, disputes. |
| `RatingsModule` | Post-ride ratings and review constraints. |
| `NotificationsModule` | Push tokens, APNs/FCM delivery, in-app notification list/read state. |
| `PlacesModule` | Mapbox geocoding and directions. |
| `SearchHistoryModule` | Recent search persistence. |
| `SearchAlertsModule` | Driver/user route alerts via background jobs. |
| `AggregatesModule` | Home/driver aggregate stats and demand data. |
| `ChatModule` | Socket.IO chat scoped to booking participants. |
| `AdminModule` | Admin reports/bans and ban expiry handling. |

### Backend Integrations

| Integration | Status |
| --- | --- |
| Supabase auth | Active. Token verified on request; profile auto-created from metadata. |
| PostgreSQL/Prisma | Active. Prisma 7 with `@prisma/adapter-pg`. |
| Redis/BullMQ | Active. Supports `REDIS_URL` or `REDIS_HOST`/`REDIS_PORT`. |
| Mapbox | Active for places/routing backend. |
| APNs/FCM | Active when credentials exist; missing credentials log warnings and disable delivery. |
| MobilePay | Manual deep-link/P2P only; no server-side payment verification. |
| Stripe/Twilio | Dependencies/config traces exist, but no meaningful active product integration found. |

### Backend Issues And Risks

| ID | Severity | File | Finding | Suggested fix |
| --- | --- | --- | --- | --- |
| BE-001 | Critical | [apps/api/src/bookings/bookings.service.ts](apps/api/src/bookings/bookings.service.ts) | Booking creation checks available seats inside a Prisma transaction, but it does not lock the trip or booking aggregate. Two concurrent requests can both see the same availability and overbook. The comment says SELECT FOR UPDATE, but no `SELECT ... FOR UPDATE` is executed. | Use a serializable transaction plus retry, explicit row lock via raw SQL, or a DB-backed seat counter/constraint. Also add a concurrent booking test. |
| BE-002 | High | [apps/api/src/trips/trips.service.ts](apps/api/src/trips/trips.service.ts) | `complete()` updates trip status before settlement creation. Settlement creation is then attempted per booking and errors are swallowed, so a trip can be completed while some confirmed bookings never get `RideSettlement` rows or `AWAITING_PAYMENT`. | Wrap trip completion, all settlement creates, and booking state updates in one transaction or return partial failure and keep trip active until fixed. |
| BE-003 | High | [apps/api/src/trips/trips.service.ts](apps/api/src/trips/trips.service.ts), [apps/api/src/settlements/settlements.service.ts](apps/api/src/settlements/settlements.service.ts) | MobilePay flow is purely manual: passenger can mark paid without payment provider confirmation, and driver confirmation is trust-based. | Keep as MVP only if product accepts this risk; otherwise add provider callbacks, proof/upload, reconciliation, or explicit manual-dispute/admin review. |
| BE-004 | High | [apps/api/src/trips/trips.service.ts](apps/api/src/trips/trips.service.ts) | Recurring Model A creation uses `createMany()` without a uniqueness constraint. A retry/double submit can duplicate the 30-day set. `extendRecurringWindow()` uses `skipDuplicates`, but schema has no matching unique key to make duplicates skippable. | Add a compound unique key such as driver/origin/dest/departureAt/model, then use `skipDuplicates` consistently. |
| BE-005 | Medium | [apps/api/src/bookings/bookings.service.ts](apps/api/src/bookings/bookings.service.ts), [apps/api/src/trips/trips.service.ts](apps/api/src/trips/trips.service.ts) | Model B threshold job scheduling can be skipped if deadline is already due or if Redis/job creation fails; bookings can sit pending/confirmed without a timely threshold decision. | On create/confirm, synchronously evaluate overdue thresholds or enqueue an immediate job when `delay <= 0`; add queue failure handling. |
| BE-006 | Medium | [apps/api/src/search-alerts/search-alerts.service.ts](apps/api/src/search-alerts/search-alerts.service.ts) | Alert matching uses substring matching in the DB query against only the first comma-separated address token. This can miss valid matches and create false positives. | Normalize both alert and trip addresses into route tokens or use geospatial/radius matching. |
| BE-007 | Medium | [apps/api/src/auth/supabase.guard.ts](apps/api/src/auth/supabase.guard.ts) | Legacy user migration creates a new Supabase-id user row and leaves the old row in place. Existing trips/bookings/ratings remain attached to the old ID. | Migrate related foreign keys to the new user ID in a controlled transaction or store Supabase ID separately from app user PK. |
| BE-008 | Medium | [apps/api/src/notifications/notifications.service.ts](apps/api/src/notifications/notifications.service.ts) | `sendToUser()` uses `Promise.allSettled()` and does not return failure status. Callers often swallow errors too, so critical notifications may silently fail. | Return delivery results and log/alert on critical notification failures. |
| BE-009 | Medium | [apps/api/src/trips/trips.service.ts](apps/api/src/trips/trips.service.ts) | Search fetches all active trips for a date and filters text/proximity in memory. This can become slow as trips grow. | Add geospatial indexing or bounded SQL predicates before in-memory scoring. |
| BE-010 | Medium | [apps/api/src/main.ts](apps/api/src/main.ts) | Local dev port fallback is useful, but clients are hard-coded to `:3000`; if API auto-starts on `3001`, Android local constants still point at `3000`. | Print/update local client config or avoid port fallback for workflows that require mobile clients. |

## Step 3: DB Exploration

### Database Architecture

The DB is PostgreSQL managed through Prisma in [apps/api/prisma/schema.prisma](apps/api/prisma/schema.prisma). It models users, trips, bookings, ratings, driver documents/car details, notifications, saved places, recent searches, chat messages, search alerts, user reports, and manual ride settlements.

Core relationship shape:

```text
User
  -> Trip as driver
  -> Booking as passenger
  -> Rating as rater/ratee
  -> PushToken, Notification, SavedPlace, RecentSearch, SearchAlert, UserReport
  -> DriverLicence, CarDetails

Trip
  -> Booking
  -> driver User
  -> Model A recurring or Model B threshold semantics

Booking
  -> Trip
  -> passenger User
  -> RideSettlement optional 1:1
  -> ChatMessage

RideSettlement
  -> Booking
  -> manual passenger paid / driver confirmed / disputed timestamps
```

Important enums:

| Enum | Values |
| --- | --- |
| `Role` | `PASSENGER`, `DRIVER`, `BOTH` |
| `TripModel` | `A`, `B` |
| `TripStatus` | `ACTIVE`, `CONFIRMED`, `CANCELLED`, `COMPLETED`, `THRESHOLD_NOT_MET` |
| `BookingStatus` | `PENDING`, `CONFIRMED`, `CANCELLED`, `REFUNDED`, `AWAITING_PAYMENT`, `COMPLETED`, `DISPUTED` |
| `NotificationType` | booking, rating, threshold, chat, search alert, payment, general notification types |
| `SavedPlaceKind` | `HOME`, `WORK`, `CUSTOM` |

Current migration set: 12 migrations from `20260420094308_init` through `20260515175648_p2p_mobilepay_pivot`.

### DB Issues And Risks

| ID | Severity | File | Finding | Suggested fix |
| --- | --- | --- | --- | --- |
| DB-001 | Critical | [apps/api/prisma/schema.prisma](apps/api/prisma/schema.prisma) | `Booking.trip` lacks an explicit `onDelete` policy. Prisma migration defaults to restrictive behavior, which may block trip deletion or conflict with app assumptions. | Decide lifecycle: restrict deletes intentionally or use soft delete; if cascading is intended, add `onDelete: Cascade`. |
| DB-002 | Critical | [apps/api/prisma/schema.prisma](apps/api/prisma/schema.prisma), [apps/api/prisma/migrations/20260515175648_p2p_mobilepay_pivot/migration.sql](apps/api/prisma/migrations/20260515175648_p2p_mobilepay_pivot/migration.sql) | `RideSettlement.booking` is generated as `ON DELETE RESTRICT`. Deleting/cleaning bookings can leave settlement lifecycle awkward or block operations. | Usually use `onDelete: Cascade` for strict child records, or enforce no booking deletion and move to soft deletes. |
| DB-003 | High | [apps/api/prisma/schema.prisma](apps/api/prisma/schema.prisma) | `DriverLicence.status` reuses `BookingStatus`. Values like `AWAITING_PAYMENT`, `COMPLETED`, and `DISPUTED` make no sense for license review. | Create a dedicated `LicenceStatus` enum with `PENDING`, `APPROVED`, `REJECTED`. |
| DB-004 | High | [apps/api/prisma/migrations/20260501141940_recurring_days_string/migration.sql](apps/api/prisma/migrations/20260501141940_recurring_days_string/migration.sql) | Migration drops `SearchAlert`, losing existing alert data; a later migration recreates it. | If production has data, replace with data-preserving migration/backfill or document intentional data loss. |
| DB-005 | High | [apps/api/prisma/migrations/20260501141940_recurring_days_string/migration.sql](apps/api/prisma/migrations/20260501141940_recurring_days_string/migration.sql) | `recurringDays` changes type to `TEXT[]` without visible semantic backfill from old int day values to day codes. | Add a backfill mapping `0..6` to `SUN..SAT` or verify no production rows existed. |
| DB-006 | Medium | [apps/api/prisma/schema.prisma](apps/api/prisma/schema.prisma) | `TripStatus.CONFIRMED` exists but active code mostly uses booking confirmation, not trip confirmation. | Remove if unused or define exactly when a trip becomes confirmed. |
| DB-007 | Medium | [apps/api/prisma/schema.prisma](apps/api/prisma/schema.prisma), [apps/api/src/trips/trips.service.ts](apps/api/src/trips/trips.service.ts) | `minPassengers` and `thresholdDeadline` are nullable although Model B requires them. | Keep service validation, and consider separate model-specific table or DB check constraint. |
| DB-008 | Medium | [apps/api/prisma/schema.prisma](apps/api/prisma/schema.prisma) | No soft deletes/audit trail for trips/bookings/settlements/reports. This is risky for disputes and support. | Add `deletedAt` and/or audit event tables for user-visible transactional entities. |
| DB-009 | Medium | [apps/api/prisma/schema.prisma](apps/api/prisma/schema.prisma) | Rating lookup likely needs indexes on `raterId`/`rateeId`; schema only defines FK relations, no explicit query-oriented indexes. | Add indexes for profile/rating summary queries. |
| DB-010 | Medium | [apps/api/prisma/schema.prisma](apps/api/prisma/schema.prisma) | `mobilepayNumber` is nullable by design, but trips and settlement completion require it at runtime. Driver can remove/change it after trip creation. | Consider storing settlement number snapshot at trip creation or preventing nulling while active trips exist. |

## Build, CI, And Tooling Findings

| ID | Severity | File | Finding | Suggested fix |
| --- | --- | --- | --- | --- |
| CI-001 | High | [.github/workflows/ci.yml](.github/workflows/ci.yml) | iOS build step ends with `| xcpretty || true`, which can make a failing `xcodebuild` pass CI. | Remove `|| true`; install `xcpretty` explicitly or use `set -o pipefail`. |
| CI-002 | Medium | [.github/workflows/ci.yml](.github/workflows/ci.yml) | CI builds Android/iOS shared but does not run backend lint/tests/build. Backend regressions can merge unnoticed. | Add `npm ci`, `npm test`, and `npm run build` under `apps/api`. |
| TOOL-001 | Low | Local environment | `rg` and `gh` are not installed in the current shell. Exploration used `find` and VS Code search instead; GitHub audit issues were not created. | Install ripgrep and GitHub CLI if you want faster audits and automatic issue creation. |

## What Is Working Well

- Shared KMP architecture is coherent: domain models, repository contracts, DTO mappings, networking, ViewModels, and DI are separated cleanly.
- Compose and SwiftUI frontends generally mirror the same product flows using shared ViewModels where practical.
- Supabase auth migration is handled in guard logic and profile auto-creation is thoughtfully accounted for.
- Backend modules are feature-focused and mostly easy to trace.
- Redis fallback config was fixed to support `REDIS_HOST`/`REDIS_PORT` when `REDIS_URL` is absent.
- Mapbox has replaced Google Maps on backend routing/geocoding, reducing server-side Google dependency.
- Integer øre is consistently used for money in core data models.
- Push token cleanup exists for invalid APNs/FCM tokens.
- ViewModel write actions generally use local loading flags to prevent repeated taps.

## Highest-Priority Fix Order

1. Fix booking concurrency/overbooking in [apps/api/src/bookings/bookings.service.ts](apps/api/src/bookings/bookings.service.ts).
2. Make trip completion and settlement creation atomic in [apps/api/src/trips/trips.service.ts](apps/api/src/trips/trips.service.ts).
3. Remove `|| true` from iOS CI in [.github/workflows/ci.yml](.github/workflows/ci.yml).
4. Add backend CI for `apps/api` tests/build.
5. Decide DB deletion policy: soft deletes vs cascade/restrict, especially bookings and settlements.
6. Replace `DriverLicence.status: BookingStatus` with a dedicated enum.
7. Add recurring-trip uniqueness/idempotency.
8. Clarify whether manual MobilePay is acceptable for MVP or needs provider-backed verification.
9. Fix/descope placeholder screens before release.
10. Add migration checks/backfills for old `SearchAlert` and `recurringDays` data.

## Verification Performed

- Loaded Hop-specific audit guidance and existing architecture memory.
- Inventoried authored files with generated/dependency folders excluded.
- Used read-only sub-exploration for frontend, backend, and DB layers.
- Opened and verified key source files for the strongest findings.
- Ran VS Code diagnostics on `apps/api/src`, `shared/src/commonMain`, and `composeApp/src/commonMain`: no editor diagnostics were reported.
- Checked GitHub CLI availability for optional audit issue creation: `gh` is not installed, so no GitHub issues were created.
