import Combine
import Shared

@MainActor
final class TripDetailActiveViewModelWrapper: ObservableObject {

    let viewModel: TripDetailActiveViewModel

    @Published var state: TripDetailActiveUiState

    init() {
        let vm = KoinIOSKt.getTripDetailActiveViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any TripDetailActiveEffect) -> Void) {
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

    func load(bookingId: String) {
        viewModel.onEvent(event: TripDetailActiveEventLoad(bookingId: bookingId))
    }

    func messageDriver() {
        viewModel.onEvent(event: TripDetailActiveEventMessageDriver.shared)
    }
}
