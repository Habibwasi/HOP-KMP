import Combine
import Shared

@MainActor
final class TaxViewModelWrapper: ObservableObject {

    let viewModel: TaxViewModel

    @Published var state: TaxUiState

    init() {
        let vm = KoinIOSKt.getTaxViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any TaxEffect) -> Void) {
        Task {
            for await newState in viewModel.state {
                self.state = newState
            }
        }
        Task {
            for await effect in viewModel.effects {
                onEffect(effect)
            }
        }
    }

    func loadDashboard(year: Int, month: Int) {
        viewModel.onEvent(event: TaxEventLoadDashboard(year: Int32(year), month: Int32(month)))
    }

    func previousMonth() { viewModel.onEvent(event: TaxEventPreviousMonth.shared) }
    func nextMonth()     { viewModel.onEvent(event: TaxEventNextMonth.shared) }

    func loadTaxReport(year: Int) {
        viewModel.onEvent(event: TaxEventLoadTaxReport(year: Int32(year)))
    }

    func retry() { viewModel.onEvent(event: TaxEventRetry.shared) }

    func getReportUrl(year: Int) {
        viewModel.onEvent(event: TaxEventGetReportUrl(year: Int32(year)))
    }
}
