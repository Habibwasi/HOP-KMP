import Combine
import Shared

@MainActor
final class BookingSuccessViewModelWrapper: ObservableObject {

    let viewModel: BookingSuccessViewModel

    @Published var state: BookingSuccessUiState

    init() {
        let vm = KoinIOSKt.getBookingSuccessViewModel()
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
        viewModel.onEvent(event: BookingSuccessEventLoad(bookingId: bookingId))
    }
}
