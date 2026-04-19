import Shared

// ── SearchTripsViewModelWrapper ───────────────────────────────────────────────
//
// @StateObject bridge between shared SearchTripsViewModel (KMP/SKIE) and SwiftUI.
// Use @StateObject — NEVER @ObservedObject.
//
// SKIE converts:
//   StateFlow<SearchTripsUiState> → AsyncSequence  (for await in viewModel.state)
//
// SearchTripsEvent SKIE flat names:
//   SearchTripsEvent.Search      → SearchTripsEventSearch
//   SearchTripsEvent.ClearError  → SearchTripsEventClearError  (data object → .shared)

import SwiftUI
import Combine

@MainActor
final class SearchTripsViewModelWrapper: ObservableObject {

    let viewModel: SearchTripsViewModel

    @Published var state: SearchTripsUiState

    init() {
        let vm = KoinIOSKt.getSearchTripsViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving() {
        Task {
            for await newState in viewModel.state {
                self.state = newState
            }
        }
    }

    // ── Convenience dispatchers ────────────────────────────────────────────────

    func search(origin: String, dest: String, date: String, seats: Int) {
        viewModel.onEvent(event: SearchTripsEventSearch(
            origin: origin,
            dest: dest,
            date: date,
            seats: Int32(seats)
        ))
    }

    func clearError() {
        viewModel.onEvent(event: SearchTripsEventClearError.shared)
    }
}
