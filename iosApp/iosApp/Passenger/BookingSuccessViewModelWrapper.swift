import Combine
import Shared

@MainActor
final class BookingSuccessViewModelWrapper: ObservableObject {

    let viewModel: BookingSuccessViewModel

    @Published var state: BookingSuccessUiState

    private var stateTask: Task<Void, Never>?

    init() {
        let vm = KoinIOSKt.getBookingSuccessViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving() {
        stateTask?.cancel()
        stateTask = Task {
            for await newState in viewModel.state {
                self.state = newState
            }
        }
    }

    func load(bookingId: String) {
        viewModel.onEvent(event: BookingSuccessEventLoad(bookingId: bookingId))
    }
}
