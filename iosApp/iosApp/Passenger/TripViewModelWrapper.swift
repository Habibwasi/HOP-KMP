import SwiftUI
import Combine
import Shared

// ── TripViewModelWrapper ──────────────────────────────────────────────────────
//
// @StateObject bridge for shared TripViewModel (KMP/SKIE).
//
// SKIE flat names for TripEvent:
//   TripEvent.LoadMyTripsPassenger → TripEventLoadMyTripsPassenger (data object → .shared)
//   TripEvent.LoadMyTripsDriver    → TripEventLoadMyTripsDriver    (data object → .shared)
//   TripEvent.SelectTrip           → TripEventSelectTrip
//   TripEvent.CompleteTrip         → TripEventCompleteTrip
//
// SKIE flat names for TripEffect:
//   TripEffect.NavigateToTripDetail → TripEffectNavigateToTripDetail
//   TripEffect.ShowSnackbar         → TripEffectShowSnackbar

@MainActor
final class TripViewModelWrapper: ObservableObject {

    let viewModel: TripViewModel

    @Published var state: TripUiState

    private var stateTask: Task<Void, Never>?
    private var effectTask: Task<Void, Never>?

    init() {
        let vm = KoinIOSKt.getTripViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any TripEffect) -> Void) {
        // Cancel any previous observation tasks before spawning new ones.
        // Without this guard, each .onAppear re-entry spawns additional Tasks
        // that both consume from the same single-consumer Kotlin Channel, causing
        // non-deterministic effect delivery and state duplication.
        stateTask?.cancel()
        effectTask?.cancel()
        stateTask = Task { @MainActor [weak self] in
            guard let self else { return }
            for await newState in self.viewModel.state {
                guard !Task.isCancelled else { return }
                self.state = newState
            }
        }
        effectTask = Task { @MainActor [weak self] in
            guard let self else { return }
            for await effect in self.viewModel.effect {
                guard !Task.isCancelled else { return }
                onEffect(effect)
            }
        }
    }

    // ── Convenience dispatchers ────────────────────────────────────────────────

    func loadMyTripsPassenger() {
        viewModel.onEvent(event: TripEventLoadMyTripsPassenger.shared)
    }

    func selectTrip(id: String) {
        viewModel.onEvent(event: TripEventSelectTrip(id: id))
    }

    func completeTrip(id: String) {
        viewModel.onEvent(event: TripEventCompleteTrip(id: id))
    }
}
