import Combine
import Shared

@MainActor
final class TripDetailViewModelWrapper: ObservableObject {

    let viewModel: TripDetailViewModel

    @Published var state: TripDetailUiState

    init() {
        let vm = KoinIOSKt.getTripDetailViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any TripDetailEffect) -> Void) {
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

    func loadTrip(tripId: String) {
        viewModel.onEvent(event: TripDetailEventLoadTrip(tripId: tripId))
    }

    func bookSeat() {
        viewModel.onEvent(event: TripDetailEventBookSeat.shared)
    }

    func viewDriverProfile() {
        viewModel.onEvent(event: TripDetailEventViewDriverProfile.shared)
    }
}
