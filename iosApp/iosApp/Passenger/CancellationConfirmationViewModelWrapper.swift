import Combine
import Shared

@MainActor
final class CancellationConfirmationViewModelWrapper: ObservableObject {

    let viewModel: CancellationConfirmationViewModel

    @Published var state: CancellationConfirmationUiState

    init() {
        let vm = KoinIOSKt.getCancellationConfirmationViewModel()
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

    func load(bookingId: String) {
        viewModel.onEvent(event: CancellationConfirmationEventLoad(bookingId: bookingId))
    }
}
