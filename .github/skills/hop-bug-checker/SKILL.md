# Hop Bug Checker Skill

Expert bug-auditing workflow for the Hop KMP carpooling app. Use when asked to "find bugs", "audit the codebase", "edge case test", or "review for correctness".

---

## 1. Koin Registration Completeness

Every `koinViewModel<XViewModel>()` call in `composeApp/` must have a matching `viewModelOf(::XViewModel)` in `shared/.../di/PresentationModule.kt`. Missing entries crash at runtime with `NoBeanDefinitionException`.

**Audit steps:**
```
grep -r "koinViewModel<" composeApp/src --include="*.kt" | sed 's/.*koinViewModel<\([^>]*\)>.*/\1/'
grep "viewModelOf" shared/src/commonMain/.../di/PresentationModule.kt
```
Diff the two lists. Any ViewModel used in UI but not registered in Koin is a crash.

---

## 2. Cross-Module Smart Cast (Kotlin Compiler)

**Pattern that fails:**
```kotlin
// In a different module from where TripDetailUiState is defined:
if (tripState.minThreshold != null && tripState.minThreshold > 0) { ... }
// Error: Smart cast to 'Int' is impossible — public API property in different module
```

**Fix — always capture public nullable props in a local val first:**
```kotlin
val minThreshold = tripState.minThreshold
if (minThreshold != null && minThreshold > 0) { ... }
```

Search for this pattern:
```
grep -rn "\.([a-zA-Z]+) != null && \.\1" composeApp/src --include="*.kt"
```

---

## 3. ViewModel Action Idempotency

Any `private fun` that fires a network call must guard against double-invocation. The button `enabled = !isProcessing` is not enough because state propagation is async — a fast double-tap fires two calls before the first recomposition.

**Required guard pattern:**
```kotlin
private fun createBooking(tripId: String, seats: Int) {
    if (_state.value.paymentState == PaymentState.PROCESSING) return  // idempotency guard
    viewModelScope.launch { ... }
}
```

Audit all `viewModelScope.launch` blocks inside event handlers — every one that mutates server state (create, confirm, cancel, rate) needs a guard.

---

## 4. Auth Flow Phone Propagation

**Bug pattern:** Registration collects fullName/email/password but omits phone. `AuthViewModel.register()` then navigates to OTP with `phone = ""`, causing OTP screen to say "We sent a code to " (blank) and the Twilio backend call to fail.

**Checklist:**
- `AuthEvent.Register` must include `phone: String`
- `SignUpScreen` must have a `KeyboardType.Phone` field for phone
- `AuthViewModel.register()` must pass `phone = event.phone` to `NavigateToOtpVerification`
- `AuthUiState.pendingOtpPhone` stores the value if needed across state updates

---

## 5. Monetary Display Safety

Amounts are **always `Int` in øre** (`seatCostOre`, `platformFeeOre`, etc.). Never store or pass as Float. Only divide at render time:

```kotlin
// CORRECT
Text("${amount / 100} DKK")

// WRONG — loses precision, introduces floating-point errors
Text("${amount.toFloat() / 100f} DKK")
```

Audit: `grep -rn "\.toFloat()" shared/src --include="*.kt"` — any conversion on a monetary field is a bug.

---

## 6. Effect Handler Completeness

Every `sealed interface XEffect` branch must be handled in the Route's `LaunchedEffect(effect)` `when` block. Missing branches are compile warnings but silent no-ops at runtime.

```kotlin
// In XRoute:
LaunchedEffect(Unit) {
    viewModel.effect.collect { effect ->
        when (effect) {
            is XEffect.NavigateToY -> onNavigateToY()
            is XEffect.ShowError  -> { /* handle */ }
            // Missing branch = silent no-op
        }
    }
}
```

Audit: open each `*Effect.kt`, list all subclasses, cross-check against the Route's `when` block.

---

## 7. LaunchedEffect Key Correctness

| Key | Behavior |
|-----|----------|
| `LaunchedEffect(Unit)` | Runs once on first composition — correct for load-once actions |
| `LaunchedEffect(someVar)` | Re-runs whenever `someVar` changes — correct for reactive side effects |
| `LaunchedEffect(true)` | Same as `Unit` but misleading — avoid |

Two independent `LaunchedEffect(sameKey)` blocks are **fine** — they run in separate coroutines.

---

## 8. Trip Model B Threshold Math

```kotlin
// seatsBooked = current paid seats (before this booking)
val seatsAfterBooking = seatsBooked + 1          // includes THIS booking
val seatsNeeded = (minThreshold - seatsAfterBooking).coerceAtLeast(0)
```

`coerceAtLeast(0)` prevents negative "0 more needed" display when threshold is already met.  
Progress bar: `progress = seatsAfterBooking.toFloat() / minThreshold.toFloat()`, clamped to `[0f, 1f]`.

---

## 9. SearchViewModel Filter AND vs OR

`applyFilters()` currently ANDs `DAILY_COMMUTE + LONG_DISTANCE` model filters, producing an empty list when both are selected. This is acceptable UX (deselect one to see results) but differs from typical multi-select OR semantics. If product requires OR, the fix is:

```kotlin
val modelMatch = activeModels.isEmpty() || trip.model in activeModels
```

---

## Common File Locations

| Concern | File |
|---------|------|
| Koin DI | `shared/src/commonMain/.../di/PresentationModule.kt` |
| Auth ViewModel | `shared/src/commonMain/.../presentation/auth/AuthViewModel.kt` |
| Booking ViewModel | `shared/src/commonMain/.../presentation/booking/BookingViewModel.kt` |
| Navigation routes | `composeApp/src/commonMain/.../ui/navigation/HopRoutes.kt` |
| NavGraph wiring | `composeApp/src/commonMain/.../ui/navigation/HopNavGraph.kt` |
| Design tokens | `composeApp/src/commonMain/.../ui/theme/HopColors.kt` |
