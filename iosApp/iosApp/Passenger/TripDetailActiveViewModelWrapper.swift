import Combine
import Shared

@MainActor
final class TripDetailActiveViewModelWrapper: ObservableObject {

    let viewModel: TripDetailActiveViewModel

    @Published var state: TripDetailActiveUiState

    private var stateTask: Task<Void, Never>?
    private var effectTask: Task<Void, Never>?

    init() {
        let vm = KoinIOSKt.getTripDetailActiveViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any TripDetailActiveEffect) -> Void) {
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

    func load(bookingId: String) {
        viewModel.onEvent(event: TripDetailActiveEventLoad(bookingId: bookingId))
    }

    func messageDriver() {
        viewModel.onEvent(event: TripDetailActiveEventMessageDriver.shared)
    }
}
