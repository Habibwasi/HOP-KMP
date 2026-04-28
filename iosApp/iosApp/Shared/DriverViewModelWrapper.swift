import Combine
import Shared

// ── DriverViewModelWrapper ────────────────────────────────────────────────────
// Bridges shared DriverViewModel into SwiftUI.
// Used by every driver screen (DR-01 … DR-14).

@MainActor
final class DriverViewModelWrapper: ObservableObject {

    let viewModel: DriverViewModel

    @Published var state: DriverUiState

    init() {
        let vm = KoinIOSKt.getDriverViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any DriverEffect) -> Void) {
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

    func saveCarDetails(_ carDetails: CarDetails) {
        viewModel.onEvent(event: DriverEventSaveCarDetails(carDetails: carDetails))
    }
}
