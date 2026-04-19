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

    init() {
        let vm = KoinIOSKt.getTripViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any TripEffect) -> Void) {
        Task {
            for await newState in viewModel.state {
                self.state = newState
            }
        }
        Task {
            for await effect in viewModel.effect {
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
