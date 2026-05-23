# Hop-Specific Bug Patterns — Reference Catalogue

Loaded on demand by `hop-debug` skill during Phase 3 implementation.
Each entry describes the symptom, root cause, affected files, and the exact fix.

---

## 1. `sessionStatus.first{}` Hang (No Timeout Guard)

**Symptom**: App freezes on login/register/restore when the network drops mid-auth or the Supabase Auth plugin never transitions to `Authenticated`.

**Root cause**: `supabase.auth.sessionStatus.first { it is SessionStatus.Authenticated }` suspends indefinitely if the condition is never met. `withTimeout` propagates `CancellationException` which can corrupt coroutine state; `withTimeoutOrNull` is safe.

**Files**: `SupabaseAuthRepositoryImpl.kt` — `register`, `login`, `restoreSession`, `handleDeepLink`, `handleRecoveryDeepLink`.

**Fix**:
```kotlin
// Before
supabase.auth.sessionStatus.first { it is SessionStatus.Authenticated }

// After
withTimeoutOrNull(15_000L) {
    supabase.auth.sessionStatus.first { it is SessionStatus.Authenticated }
} ?: throw Exception("Authentication timed out — please try again.")
```
Import: `import kotlinx.coroutines.withTimeoutOrNull`

---

## 2. Empty `refreshToken` + `autoRefresh = true`

**Symptom**: Implicit-flow email confirmation links sometimes omit `refresh_token`. The session imports successfully but expires silently 1 h later → unexpected 401 → `SessionExpiryNotifier` → user sees "Session expired" with no context.

**Root cause**: `fragmentParams["refresh_token"] ?: ""` passes an empty string to `importAuthToken` while `autoRefresh = true`. The library tries to refresh with `""`, fails, and fires 401.

**Files**: `SupabaseAuthRepositoryImpl.kt` — `handleDeepLink` and `handleRecoveryDeepLink` implicit-flow branches.

**Fix**:
```kotlin
val refreshToken = fragmentParams["refresh_token"] ?: ""
supabase.auth.importAuthToken(
    accessToken,
    refreshToken,
    retrieveUser = false,
    autoRefresh = refreshToken.isNotEmpty(),   // ← key change
)
```

---

## 3. Stale `recoveryPending` Flag

**Symptom**: After a crash during password recovery, `recoveryPending = true` persists in `TokenStorage`. On next launch the app navigates to the "set new password" screen immediately, even though no recovery is in progress.

**Root cause**: `AuthViewModel` saves `recoveryPending = true` when a recovery deep-link arrives but only clears it in the `updatePassword` success path. Crash or logout before `updatePassword` leaves the flag set.

**Files**: `AuthViewModel.kt` — `handleAuthCallback`, `handleRecoveryCallback`, `logout`, `updatePassword`.

**Fix**: Call `tokenStorage.saveRecoveryPending(false)` + set `isPasswordRecoveryPending = false` in all four of the above. Make the storage call *synchronous* (not fire-and-forget `launch {}`) in `handleRecoveryCallback`.

---

## 4. `deleteUser` Failure Silently Swallowed

**Symptom**: If Prisma profile creation succeeds but the compensating Supabase `deleteUser` call fails during registration rollback, a dangling auth user is created. The user cannot re-register with the same email.

**Root cause**: `deleteUser` is called once with no retry or escalation. Any transient error (network blip, Supabase rate-limit) leaves a permanent orphan.

**Files**: `users.controller.ts` — `createUser` rollback path.

**Fix**: Wrap in a retry loop (3 attempts, 200 ms / 400 ms backoff). On exhaustion, log `DANGLING_AUTH_USER` with `supabaseId` and `email` at `error` level, then throw a client-facing error directing the user to contact support.

---

## 5. `safeApiCall` Cannot Distinguish Error Types

**Symptom**: The UI shows a generic "Something went wrong" for no-network, server error, and timeout — all the same. Retry logic cannot be applied selectively.

**Root cause**: `safeApiCall` has a single `catch (e: Exception)` that maps everything to `ApiResponse.Error(CODE_UNKNOWN)`.

**Files**: `ApiResponse.kt`.

**Fix**:
```kotlin
} catch (e: HttpRequestTimeoutException) {
    ApiResponse.Error(ApiResponse.CODE_TIMEOUT, e.message ?: "Request timed out")
} catch (e: ServerResponseException) {
    ApiResponse.Error(e.response.status.value, e.message ?: "Server error")
} catch (e: Exception) {
    val isNetwork = e.message?.contains("Unable to resolve host", ignoreCase = true) == true
                 || e.message?.contains("connect", ignoreCase = true) == true
    val code = if (isNetwork) ApiResponse.CODE_NO_NETWORK else ApiResponse.CODE_UNKNOWN
    ApiResponse.Error(code, e.message ?: "Unknown error")
}
```
Add `companion object { const val CODE_NO_NETWORK = -2; const val CODE_TIMEOUT = -3 }` to `ApiResponse`.

---

## 6. Koin Registration Completeness

**Symptom**: `NoBeanDefFoundException` at runtime, only visible in specific navigation paths.

**Audit approach**:
1. List every `class Foo(val dep: Bar)` constructor in `shared/commonMain`.
2. For each `Bar`, verify a matching `single { Bar(...) }` or `factory { Bar(...) }` exists in a Koin module.
3. Verify the module is included in the `startKoin { modules(...) }` call.

**Common misses**: New repositories added to a feature module but not wired into the Koin module; platform-specific implementations provided only for one platform.

---

## 7. `LaunchedEffect` Key Correctness

**Symptom**: UI does not update when state changes, or effect re-runs every recomposition.

**Rules**:
- `LaunchedEffect(Unit)` — runs once per composition entry. Correct for one-time initialization.
- `LaunchedEffect(someState)` — re-runs whenever `someState` changes. Use when the effect is tied to state.
- `LaunchedEffect(viewModel)` — effectively `Unit` for `remember`-ed VMs; fine for long-running collectors.

**Bug**: `LaunchedEffect(Unit)` used where re-running on state change is required → effect closes over a stale value.

---

## 8. Monetary Display Safety — Integer Øre Truncation

**Symptom**: Fares show as `DKK 49` when the correct value is `DKK 49,50`.

**Root cause**: The entire Hop codebase stores monetary values as integer øre (1/100 DKK). `priceOere / 100` performs integer division, silently discarding the remainder. This is **not** a Float precision issue — it's integer truncation.

**This bug appears independently in multiple files and must be audited in every file in the flow:**
- `TripCard.kt` (Compose)
- `TripDetailScreen.kt` (Compose)
- `BookingConfirmationScreen.kt` (Compose)
- `TripCard.swift` (iOS)
- `TripCardLight.swift` (iOS)
- `DriverAddressPickerComponents.swift` (iOS)
- `PriceReviewView.swift` (iOS)

**Fix (Kotlin)**:
```kotlin
val priceKr  = priceOere / 100
val priceRem = priceOere % 100
val priceText = if (priceRem == 0) "DKK $priceKr"
                else "DKK $priceKr,${priceRem.toString().padStart(2, '0')}"
```

**Fix (Swift)**:
```swift
let kr  = oere / 100
let rem = oere % 100
let text = rem == 0 ? "DKK \(kr)" : "DKK \(kr),\(String(format: "%02d", rem))"
```

**Note**: `TripDetailView.swift` already has a correct `dkk(_ oere: Int)` helper — use that as the reference implementation.

**Do NOT** use `Float`/`Double` division for øre display; integer arithmetic is correct and precise.

---

## 9. Auth Phone Propagation

**Symptom**: Supabase Auth profile has a phone number but the Prisma `User` row does not, or vice versa.

**Audit**: After any auth operation that sets `phone`, verify both:
1. `supabase.auth.updateUser { phone = ... }` is called.
2. `PATCH /users/me` with `{ phone }` is called immediately after.

Both calls must succeed or neither change should be visible to the UI.

---

## 10. Effect Handler Completeness

**Symptom**: A `AuthEffect` (or other one-shot effect) emitted from `AuthViewModel` is never handled by the UI — no navigation, no snackbar, silent no-op.

**Audit approach**:
1. List every `sealed class AuthEffect` subclass.
2. List every `is AuthEffect.X ->` branch in every `LaunchedEffect` collector in Compose screens.
3. Any missing branch is a bug — at minimum it should log a warning.

---

## 11. `safeEnvelopeCall` in Individual Repositories — Incomplete Exception Hierarchy

**Symptom**: Timeout and server errors are silently swallowed in repository calls even though the global `safeApiCall` was already fixed.

**Root cause**: `TripRepositoryImpl`, `SearchHistoryRepositoryImpl`, `PlacesRepositoryImpl` and potentially others each have their own **private** `safeEnvelopeCall` helper with a single `catch (e: Exception)` block. Fixing `ApiResponse.kt` alone does not fix these.

**Audit rule**: After fixing `safeApiCall`, grep every `*RepositoryImpl.kt` for private `safeEnvelopeCall` (or any private `try/catch` wrapper) and apply the same three-branch exception hierarchy to each:

```kotlin
} catch (e: HttpRequestTimeoutException) {
    ApiResponse.Error(ApiResponse.CODE_TIMEOUT, e.message ?: "Request timed out")
} catch (e: ServerResponseException) {
    ApiResponse.Error(e.response.status.value, e.message ?: "Server error")
} catch (e: ClientRequestException) {
    ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
} catch (e: Exception) {
    ApiResponse.Error(-1, e.message ?: "Unknown error")
}
```

Required imports: `io.ktor.client.plugins.HttpRequestTimeoutException`, `io.ktor.client.plugins.ServerResponseException`, `io.ktor.client.plugins.ClientRequestException`.

---

## 12. Koin Factory Scope — ViewModel Instance Isolation

**Symptom**: A ViewModel wrapper on Screen A calls `.search()` or mutates state, but Screen B (which actually renders the results) never sees any change. The API call is real — it just goes nowhere.

**Root cause**: `viewModelOf(::SearchViewModel)` uses Koin **factory** scope, not singleton. Every `koinViewModel()` call on Android and every `@StateObject ... = SearchViewModelWrapper()` on iOS creates a **fresh, isolated instance**. A home screen wrapper and the results screen wrapper are completely separate objects.

**Files**: Any `@StateObject private var xWrapper = SomeViewModelWrapper()` that calls methods on the VM but navigates away before displaying results.

**Fix**: Remove the wrapper from the screen that does not display the results. The trigger screen should only call navigation; the destination screen owns the VM.

**Detection**: If you see `someWrapper.startObserving { _ in }` (effects swallowed) alongside a real data-fetching call, that is a dead wrapper — delete it.

---

## 13. iOS `*ViewModelWrapper` Re-entrancy — Competing Task Consumers

**Symptom**: Navigation effects (e.g., navigate to search results) are dropped non-deterministically after a view re-appears.

**Root cause**: `startObserving()` is called in `.onAppear` with no guard. Each call creates a new `Task {}` that collects from `viewModel.effect` (which is a SKIE `Channel.receiveAsFlow()`). `receiveAsFlow()` is **single-consumer** — only one collector receives each emission. With two competing Tasks alive, effects are consumed by whichever Task happens to be scheduled first, so roughly half are silently dropped.

**Affected files**: Every `*ViewModelWrapper.swift` in the project shares this pattern. Confirmed affected: `SearchViewModelWrapper`, and all others by structural equivalence.

**Fix**:
```swift
private var stateTask: Task<Void, Never>?
private var effectTask: Task<Void, Never>?

func startObserving(onEffect: @escaping (SomeEffect) -> Void) {
    stateTask?.cancel()
    effectTask?.cancel()
    stateTask = Task { @MainActor in
        for await newState in viewModel.state { self.state = newState }
    }
    effectTask = Task { @MainActor in
        for await effect in viewModel.effect { onEffect(effect) }
    }
}
```

**Audit rule**: After fixing any one wrapper, grep all other `*ViewModelWrapper.swift` files for `Task {` without a prior `.cancel()` call and apply the same fix.

---

## 14. SearchViewModel Filter Correctness

### 14a. AND vs OR semantics for model filters

**Symptom**: Selecting `DAILY_COMMUTE` + `LONG_DISTANCE` filter chips returns zero results even when trips of both types exist.

**Root cause**: Filter logic uses `modelFilters.all { trip.model == it }` (AND), requiring a trip to match *every* selected filter simultaneously — impossible for a single-valued field.

**Fix**: Use OR — a trip passes if its model is in the selected set:
```kotlin
val modelFilters = activeFilters.filterIsInstance<TripFilter.Model>().map { it.model }.toSet()
filtered = if (modelFilters.isEmpty()) filtered
           else filtered.filter { it.model in modelFilters }
```

### 14b. `sortFilters.first()` non-determinism

**Symptom**: Sort order is unpredictable when user applies multiple sort chips.

**Root cause**: `Set` iteration order is undefined; `.first()` returns an arbitrary element.

**Fix**: Use `.last()` to reflect the most recently added sort selection.

### 14c. `ClearFilters` not restoring results

**Symptom**: After clearing filters, fewer results than the original search are shown.

**Root cause**: `ClearFilters` only sets `activeFilters = emptySet()` but leaves `results` as the currently-filtered subset. Subsequent filter operations re-filter the already-filtered list.

**Fix**: Maintain a `private var _allResults: List<Trip>` set on every successful search. `ClearFilters` must restore `results = _allResults` alongside clearing `activeFilters`.

### 14d. Stale results behind spinner

**Symptom**: Previous search results remain visible while a new search is loading.

**Fix**: Set `results = emptyList()` (and show loading state) *before* the API call, not after.

### 14e. Concurrent search race condition

**Symptom**: If user taps search twice rapidly, whichever coroutine finishes last wins — not necessarily the most recent request.

**Fix**:
```kotlin
private var searchJob: Job? = null

fun search(...) {
    searchJob?.cancel()
    searchJob = viewModelScope.launch { /* API call */ }
}
```
