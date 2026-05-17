import Combine
import Shared

@MainActor
final class SettlementViewModelWrapper: ObservableObject {

    let viewModel: SettlementViewModel

    @Published var state: SettlementUiState

    init() {
        let vm = KoinIOSKt.getSettlementViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any SettlementEffect) -> Void) {
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
        viewModel.onEvent(event: SettlementEventLoad(bookingId: bookingId))
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

    func confirmReceived() {
        viewModel.onEvent(event: SettlementEventConfirmReceived.shared)
    }

    func submitDispute() {
        viewModel.onEvent(event: SettlementEventSubmitDispute.shared)
    }

    func disputeReasonChanged(_ reason: String) {
        viewModel.onEvent(event: SettlementEventDisputeReasonChanged(reason: reason))
    }
}
