import Foundation
import Combine
import Shared

// ── EditTripViewModelWrapper ──────────────────────────────────────────────────
// Bridges the shared `EditTripViewModel` into SwiftUI.
// Uses a singleton (same reasoning as DriverViewModelWrapper) so the
// pending draft survives navigation from EditTripView → EditTripPriceReviewView.

@MainActor
final class EditTripViewModelWrapper: ObservableObject {

    static let shared = EditTripViewModelWrapper()

    let viewModel: EditTripViewModel

    @Published var state: EditTripUiState

    private var effectHandlers: [UUID: (any EditTripEffect) -> Void] = [:]

    private init() {
        let vm = KoinIOSKt.getEditTripViewModel()
        self.viewModel = vm
        self.state = vm.state.value

        Task { @MainActor [weak self] in
            guard let self else { return }
            for await newState in self.viewModel.state {
                self.state = newState
            }
        }

        Task { @MainActor [weak self] in
            guard let self else { return }
            for await effect in self.viewModel.effect {
                let snapshot = self.effectHandlers.values
                for handler in snapshot { handler(effect) }
            }
        }
    }

    // MARK: — Effect observation ──────────────────────────────────────────────

    var effects: AsyncStream<any EditTripEffect> {
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

    func loadTrip(tripId: String) {
        viewModel.onEvent(event: EditTripEventLoadTrip(tripId: tripId))
    }

    func calculateRoute(originName: String, destName: String) {
        viewModel.onEvent(event: EditTripEventCalculateRoute(originName: originName, destName: destName))
    }

    func submitDraft(draft: EditTripDraft) {
        viewModel.onEvent(event: EditTripEventSubmitDraft(draft: draft))
    }

    func confirmAndUpdate() {
        viewModel.onEvent(event: EditTripEventConfirmAndUpdate.shared)
    }
}
