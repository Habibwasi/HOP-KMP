import Foundation
import Shared

@MainActor
final class DriverAggregatesViewModelWrapper: ObservableObject {

    static let shared = DriverAggregatesViewModelWrapper()

    let viewModel: DriverAggregatesViewModel

    @Published var state: DriverAggregatesUiState

    private init() {
        let vm = KoinIOSKt.getDriverAggregatesViewModel()
        self.viewModel = vm
        self.state = vm.state.value

        Task { @MainActor [weak self] in
            guard let self else { return }
            for await newState in self.viewModel.state {
                self.state = newState
            }
        }
    }

    func load() {
        viewModel.onEvent(event: DriverAggregatesEventLoad.shared)
    }
}
