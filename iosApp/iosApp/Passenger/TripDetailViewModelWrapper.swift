import Combine
import Shared

@MainActor
final class TripDetailViewModelWrapper: ObservableObject {

    let viewModel: TripDetailViewModel

    @Published var state: TripDetailUiState

    private var stateTask: Task<Void, Never>?
    private var effectTask: Task<Void, Never>?

    init() {
        let vm = KoinIOSKt.getTripDetailViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any TripDetailEffect) -> Void) {
        stateTask?.cancel()
        effectTask?.cancel()
        stateTask = Task {
            for await newState in viewModel.state {
                self.state = newState
            }
        }
        effectTask = Task {
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
