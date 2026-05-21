import Combine
import Shared

@MainActor
final class SettlementViewModelWrapper: ObservableObject {

    let viewModel: SettlementViewModel

    @Published var state: SettlementUiState

    private var stateTask: Task<Void, Never>?
    private var effectTask: Task<Void, Never>?

    init() {
        let vm = KoinIOSKt.getSettlementViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any SettlementEffect) -> Void) {
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
        viewModel.onEvent(event: SettlementEventLoad(bookingId: bookingId))
    }

    func loadForTrip(tripId: String) {
        viewModel.onEvent(event: SettlementEventLoadForTrip(tripId: tripId))
    }

    func loadForTripByBooking(bookingId: String) {
        viewModel.onEvent(event: SettlementEventLoadForTripByBooking(bookingId: bookingId))
    }

    func refresh() {
        viewModel.onEvent(event: SettlementEventRefresh.shared)
    }

    func openMobilepay() {
        viewModel.onEvent(event: SettlementEventOpenMobilepay.shared)
    }

    func markPaid() {
        viewModel.onEvent(event: SettlementEventMarkPaid.shared)
    }

    func unmarkPaid() {
        viewModel.onEvent(event: SettlementEventUnmarkPaid.shared)
    }

    func confirmReceived() {
        viewModel.onEvent(event: SettlementEventConfirmReceived.shared)
    }

    func confirmReceivedForBooking(bookingId: String) {
        viewModel.onEvent(event: SettlementEventConfirmReceivedForBooking(bookingId: bookingId))
    }

    func submitDispute() {
        viewModel.onEvent(event: SettlementEventSubmitDispute.shared)
    }

    func disputeReasonChanged(_ reason: String) {
        viewModel.onEvent(event: SettlementEventDisputeReasonChanged(reason: reason))
    }
}
