---
name: hop-debug
description: 'Expert bug-auditing and edge-case testing workflow for the Hop KMP carpooling app. Use when asked to "find bugs", "audit the codebase", "edge case test", "review for correctness", or "debug". Covers: Koin registration completeness, cross-module smart casts, ViewModel idempotency guards, auth phone propagation, monetary display safety, effect handler completeness, LaunchedEffect key correctness, Model B threshold math, and filter AND/OR semantics.'
argument-hint: 'subsystem to audit (e.g. "auth flow", "payment", "matching")'
---

# Hop Bug-Audit & Edge-Case Testing Workflow

## Core Principle

> **Follow the data end-to-end, file by file, without stopping until every layer has been read.**  
> Do NOT "check the skill catalogue and stop". Do NOT stop at a round number of findings.  
> Do NOT wait for the user to say "Continue" — keep going autonomously until every file is done.  
> The number of bugs found so far is never a ceiling.

This principle comes from a real failure in the search-flow audit: the audit stopped at 7, then 10, then 12 findings — each time incorrectly — because it relied on a catalogue checklist instead of reading every file exhaustively. The correct method was stated explicitly: *"follow the data end-to-end, file by file, without stopping until every layer has been read."*

---

## The Correct Audit Method (5 Rules)

These rules replace any checklist-based approach. Apply them to every audit:

### Rule 1 — Map every file first, using the diagram
Read `hop-dataflow.drawio` **before opening any code file**. This diagram shows the canonical flow — which modules are in scope, what each step does, and how data moves. Starting without it means guessing scope from grep results, which always misses layers.

```
hop-dataflow.drawio → UI → ViewModel → Repository → Ktor HTTP →
                      NestJS Controller → Service → Prisma → PostgreSQL
                      and the return path: response → DTO mapping → domain model → UI state
```

After reading the diagram, build an explicit checklist of every file the flow touches:
- `[ ]` Shared KMP: ViewModels, repositories, DTOs, DI modules, utilities
- `[ ]` Android Compose: screens, components, navigation graphs
- `[ ]` iOS Swift: screens, components, `*ViewModelWrapper` files, navigation stack
- `[ ]` Backend: NestJS controllers, services, DTOs, Prisma schema

Check off each file as you read it. **Never declare done until every box is checked.**

### Rule 2 — Read each file fully, not selectively
Every function in every file in the flow. Do not skim. The `ClearFilters` bug was missed because only part of the `onEvent` `when` block was read. The `PassengerHomeView` wasted API call was missed because only one section of the file was read.

### Rule 3 — Ask these questions at every line
At every non-trivial line, ask:
- Can this be `null`, blank, or empty unexpectedly?
- What happens if this is called twice in quick succession (idempotency)?
- What is the worst-case timing — race condition, cancellation, recomposition?
- Is any value from user input ever unsanitised?
- Does local time vs UTC matter here?
- Is integer arithmetic hiding precision loss? (`oere / 100` drops the remainder)
- Is this a factory-scoped VM — is the caller actually observing its results?

### Rule 4 — Never stop at "it looks fine" — grep to verify
Every assumption must be confirmed with a `grep_search`, not assumed from reading. If you think "that's probably fine", search for it.

### Rule 5 — Fix as you go, then re-read the fixed file
Fixes introduce new bugs. After every fix, re-read the surrounding 20+ lines for new issues before moving on.

---

## Phase 0 — Build the File Checklist

1. Open `hop-dataflow.drawio` and read the target flow's full path.
2. `grep_search` for every class name, composable name, screen name, and service name mentioned in the diagram.
3. Produce the explicit `[ ]` file checklist. Do not proceed until the list is complete.

---

## Phase 1 — Exhaustive File-by-File Reading

For each file on the checklist:
1. Read the **entire file** (not just the function you expect to find the bug in).
2. Apply Rules 3 and 4 to every function.
3. Add any finding immediately to the triage table (even if you plan to fix it later).
4. Check the file off the list.
5. Move to the next file — do not stop, do not wait for confirmation.

The Three-Lens mental model (Happy Path → Failure Points → Worst Case) is useful **within** each file read, but it is not the primary methodology — exhaustive file coverage is.

---

## Phase 2 — Triage Table

Build as you read (do not wait until the end). One row per finding:

| # | File | Finding | Severity | Root Cause | Worst-Case Impact | Fix Summary |
|---|------|---------|----------|------------|-------------------|-------------|
| 1 | … | … | ❌/⚠️/✅ | … | … | … |

**Severity guide**:
- ❌ Bug — incorrect behaviour confirmed or near-certain in the field
- ⚠️ Warning — incorrect under specific conditions; needs hardening
- ✅ OK — correct as written, no action required

**Known false positives in the search flow** (confirmed correct, do not re-flag):
- `TOP_RATED` chip not in `ALL_FILTER_CHIPS` — intentionally excluded; it is a UI placeholder with no filter predicate. Not a bug.

---

## Phase 3 — Implement Fixes

Work through the triage table in order: ❌ items first, then ⚠️.

**Per-fix workflow**:
1. Read the target file with ≥ 20 lines of surrounding context before editing.
2. Apply the edit with `multi_replace_string_in_file` (batch same-file edits in one call).
3. Run `get_errors` on the edited file immediately.
4. Re-read the 20 lines around the fix for new issues introduced.
5. Do not add unrelated refactors, comments, or features.

### Hop-Specific Patterns to Fix

Load [hop-specific-checks.md](./references/hop-specific-checks.md) for
the full catalogue of known recurring bugs in this codebase.  
Key patterns encountered to date:

| Pattern | Symptom | Fix |
|---------|---------|-----|
| `sessionStatus.first{}` with no timeout | Coroutine hangs forever on cold start or network drop | Wrap with `withTimeoutOrNull(15_000L)` |
| `?: ""` fallback on `refreshToken` + `autoRefresh = true` | Session silently expires → unexpected 401-driven logout | `autoRefresh = refreshToken.isNotEmpty()` |
| `recoveryPending` flag not cleared on all exit paths | Flag survives logout/crash → wrong screen shown on next launch | Clear in: success, logout, error, and app resume paths in `AuthViewModel` |
| `deleteUser` call not retried | Dangling Supabase auth user if Prisma write fails | Retry loop (3 attempts, exponential backoff); log `DANGLING_AUTH_USER` on exhaustion |
| `safeApiCall` catches all exceptions as `ApiError` | UI cannot distinguish "no network" from "server error" from "timeout" | Catch `HttpRequestTimeoutException`, `ServerResponseException`, and network heuristics separately |
| **`safeEnvelopeCall` in individual repos also incomplete** | Same silent swallowing — `TripRepositoryImpl`, `SearchHistoryRepositoryImpl`, `PlacesRepositoryImpl` each have their own private `safeEnvelopeCall` that also lacks the full exception hierarchy | Apply the same three-branch catch to every private `safeEnvelopeCall` in every repository, not just the global `safeApiCall` |
| Koin `single {}` missing for a dependency | `NoBeanDefFoundException` at runtime | Verify every constructor parameter has a corresponding Koin registration |
| **Koin `factory {}` ViewModel scope** | `viewModelOf(::SearchViewModel)` uses factory scope — each call site gets a *separate* VM instance. A wrapper on Screen A calling `.search()` will NOT share results with the VM on Screen B. Results are silently discarded. | Never call `.search()` or mutate state on a factory-scoped VM that will not be shown on screen. Remove the wrapper entirely if the results are never observed. |
| `LaunchedEffect(Unit)` instead of keyed effect | Effect does not re-run when state changes (stale closure) | Key on the relevant state variable |
| **iOS `*ViewModelWrapper` re-entrancy** | `startObserving()` has no guard — each `.onAppear` spawns an additional `Task {}` against the same `Channel.receiveAsFlow()`. `receiveAsFlow()` is **single-consumer**: multiple Tasks consume events non-deterministically, causing effects to be silently dropped. | Add `private var stateTask: Task<Void, Never>?` and `effectTask` fields; call `.cancel()` on each before creating a new one. Apply to all `*ViewModelWrapper` files in the codebase. |
| **Integer øre truncation** (`oere / 100`) | `pricePerSeatOere / 100` silently drops the øre remainder — e.g., 4950 øre shows as "DKK 49" instead of "DKK 49,50" | Split: `val kr = oere / 100; val rem = oere % 100; if (rem == 0) "DKK $kr" else "DKK $kr,${rem.toString().padStart(2,'0')}"`. Audit **every** file in the flow — this pattern appears independently in TripCard.kt, TripDetailScreen.kt, TripCard.swift, TripCardLight.swift, DriverAddressPickerComponents.swift, PriceReviewView.swift, BookingConfirmationScreen.kt — not just one place. |
| **Filter AND vs OR semantics** | Selecting two model filters (e.g., `DAILY_COMMUTE` + `LONG_DISTANCE`) produces empty results because AND requires a trip to match *both* simultaneously | Use OR: `_allResults.filter { trip -> modelFilters.isEmpty() \|\| trip.model in modelFilters }`. For multi-type filter groups always default to OR within a group. |
| **`sortFilters.first()` non-determinism** | When user applies multiple sort chips, `.first()` returns an arbitrary element depending on `Set` iteration order | Use `.last()` to reflect the user's most recent sort selection |
| **`ClearFilters` not restoring backing results** | `ClearFilters` only clears `activeFilters` but leaves `results` as the filtered subset → re-filtering on the already-filtered list produces progressively fewer results | `ClearFilters` must set `results = _allResults` alongside clearing `activeFilters`. Maintain `_allResults` as an immutable copy from the last successful search. |
| **Stale results behind spinner** | Previous search results remain visible during a new search → user sees wrong data while loading | Set `results = emptyList()` at the start of every new search, before the API call. |
| **Concurrent search race** | Rapid search taps fire overlapping coroutines; whichever finishes last wins, not the most recent | Track `searchJob: Job?`; call `searchJob?.cancel()` before launching a new search coroutine. |
| **Shadow API call** | A ViewModel wrapper calls `.search()` on a factory-scoped VM whose results are observed by no one (e.g., `searchWrapper` in `PassengerHomeView` before nav) | Delete the wrapper and its `.search()` / `.startObserving { _ in }` calls entirely. |

---

## Phase 4 — Cross-Check

After all fixes are applied, systematically verify completeness:

1. Copy the original triage table.
2. For each row:
   - `grep_search` the relevant file(s) for the fix (function name, constant, pattern).
   - If found and correct → mark ✅ Fixed.
   - If not found → apply the missing fix now.
   - If the review said "let me verify this is wired" → grep to confirm wiring, mark ✅ Already correct if confirmed.
3. A row is not done until the fix is confirmed in the source, not just in memory.

---

## Phase 5 — Final Scorecard

Output a table reproducing the triage table with every row now showing ✅ and a one-line explanation of the resolution:

| # | Finding | Status | Resolution |
|---|---------|--------|------------|
| 1 | … | ✅ Fixed | Applied `withTimeoutOrNull(15_000L)` on all 5 call sites |
| … | … | ✅ Already correct | Confirmed wired in `HttpClientFactory.kt` line 64–68 |

**Done condition**: every file on the Phase 0 checklist has been read AND every row in the scorecard is ✅. A count of N bugs found is never itself a done condition.

---

## Tool Usage Guidelines

- Use `grep_search` (not `semantic_search`) for exact-pattern verification of fixes.
- Use `read_file` with ≥ 20 lines of surrounding context before editing.
- Use `multi_replace_string_in_file` to batch all edits in one file into a single call.
- Run `get_errors` after every file edit before proceeding.
- Never apply speculative refactors outside the scope of a triage finding.
