import Foundation
import Combine
import Shared

// ── DriverViewModelWrapper ────────────────────────────────────────────────────
// Bridges the shared `DriverViewModel` into SwiftUI.
//
// Why a singleton?
// ----------------
// `DriverViewModel` carries cross-screen flow state (the pending trip draft,
// active-trip detail, licence status, etc.). The Android side relies on a
// single VM living across screens — but on iOS each `KoinPlatform.get()`
// returns a fresh instance (Koin `viewModelOf` resolves as a factory without
// an Android `ViewModelStoreOwner`). Without a shared instance:
//   • PostTripModelA submits a draft → PriceReview shows "No trip draft".
//   • TripDetailActive loads passengers → MarkTripComplete sees an empty
//     activeTripDetail, so completeTrip falls through to the "MyTrips" branch
//     and never opens RatePassenger.
//
// The fix:
//   • Hold the Kotlin `DriverViewModel` in a shared instance (`shared`) that
//     every screen pulls from.
//   • Collect `viewModel.state` once and republish via `@Published`.
//   • Collect `viewModel.effect` once and rebroadcast through `AsyncStream`s
//     so multiple SwiftUI screens can each consume effects without the Kotlin
//     `Channel`'s single-consumer semantics dropping events on the floor.

@MainActor
final class DriverViewModelWrapper: ObservableObject {

    static let shared = DriverViewModelWrapper()

    let viewModel: DriverViewModel

    @Published var state: DriverUiState

    private var effectHandlers: [UUID: (any DriverEffect) -> Void] = [:]

    private init() {
        let vm = KoinIOSKt.getDriverViewModel()
        self.viewModel = vm
        self.state = vm.state.value

        // Single state observer → republished via @Published.
        Task { @MainActor [weak self] in
            guard let self else { return }
            for await newState in self.viewModel.state {
                self.state = newState
            }
        }

        // Single effect observer → broadcast to all registered handlers.
        Task { @MainActor [weak self] in
            guard let self else { return }
            for await effect in self.viewModel.effect {
                let snapshot = self.effectHandlers.values
                for handler in snapshot { handler(effect) }
            }
        }
    }

    // MARK: — Effect observation ──────────────────────────────────────────────

    /// Returns an AsyncStream that yields every `DriverEffect` until the
    /// consuming Task is cancelled (e.g. SwiftUI view disappears). Use as
    /// `.task { for await effect in wrapper.effects { … } }`.
    var effects: AsyncStream<any DriverEffect> {
        AsyncStream { continuation in
            let id = UUID()
            Task { @MainActor in
                self.effectHandlers[id] = { effect in
                    continuation.yield(effect)
                }
            }
            continuation.onTermination = { @Sendable _ in
                Task { @MainActor in
                    self.effectHandlers.removeValue(forKey: id)
                }
            }
        }
    }

    // MARK: — Convenience dispatchers ─────────────────────────────────────────

    func loadDriverHome()    { viewModel.onEvent(event: DriverEventLoadDriverHome.shared) }
    func refreshDriverHome() { viewModel.onEvent(event: DriverEventRefreshDriverHome.shared) }
    func requestPostTrip()   { viewModel.onEvent(event: DriverEventRequestPostTrip.shared) }
    func selectModelA()      { viewModel.onEvent(event: DriverEventSelectModelA.shared) }
    func selectModelB()      { viewModel.onEvent(event: DriverEventSelectModelB.shared) }
    func confirmAndPostTrip() { viewModel.onEvent(event: DriverEventConfirmAndPostTrip.shared) }
    func loadLicenceStatus() { viewModel.onEvent(event: DriverEventLoadLicenceStatus.shared) }
    func tapEarningsBanner() { viewModel.onEvent(event: DriverEventTapEarningsBanner.shared) }

    func selectTrip(tripId: String) {
        viewModel.onEvent(event: DriverEventSelectTrip(tripId: tripId))
    }

    func loadActiveTripDetail(tripId: String) {
        viewModel.onEvent(event: DriverEventLoadActiveTripDetail(tripId: tripId))
    }

    func completeTrip(tripId: String) {
        viewModel.onEvent(event: DriverEventCompleteTrip(tripId: tripId))
    }

    func submitModelADraft(_ draft: ModelADraft) {
        viewModel.onEvent(event: DriverEventSubmitModelADraft(draft: draft))
    }

    func submitModelBDraft(_ draft: ModelBDraft) {
        viewModel.onEvent(event: DriverEventSubmitModelBDraft(draft: draft))
    }

    func calculateRouteDistance(originName: String, destName: String) {
        viewModel.onEvent(event: DriverEventCalculateRouteDistance(originName: originName, destName: destName))
    }

    func saveCarDetails(_ carDetails: CarDetails) {
        viewModel.onEvent(event: DriverEventSaveCarDetails(carDetails: carDetails))
    }
}
