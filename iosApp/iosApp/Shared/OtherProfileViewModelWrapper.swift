import Combine
import Shared

@MainActor
final class OtherProfileViewModelWrapper: ObservableObject {

    let viewModel: OtherProfileViewModel

    @Published var state: OtherProfileUiState

    init() {
        let vm = KoinIOSKt.getOtherProfileViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any OtherProfileEffect) -> Void) {
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

    func load(userId: String) {
        viewModel.onEvent(event: OtherProfileEventLoad(userId: userId))
    }

    func showReportDialog()    { viewModel.onEvent(event: OtherProfileEventShowReportDialog.shared) }
    func dismissReportDialog() { viewModel.onEvent(event: OtherProfileEventDismissReportDialog.shared) }

    func submitReport(reason: String) {
        viewModel.onEvent(event: OtherProfileEventSubmitReport(reason: reason))
    }
}
