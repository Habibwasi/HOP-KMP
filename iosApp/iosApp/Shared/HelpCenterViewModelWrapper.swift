import Combine
import Shared

@MainActor
final class HelpCenterViewModelWrapper: ObservableObject {

    let viewModel: HelpCenterViewModel

    @Published var state: HelpCenterUiState

    init() {
        let vm = KoinIOSKt.getHelpCenterViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any HelpCenterEffect) -> Void) {
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

    func navigateBack()          { viewModel.onEvent(event: HelpCenterEventNavigateBack.shared) }
    func searchChanged(_ q: String) { viewModel.onEvent(event: HelpCenterEventSearchQueryChanged(query: q)) }
    func toggleFaq(_ id: String) { viewModel.onEvent(event: HelpCenterEventFaqToggled(id: id)) }
    func selectTab(_ tab: HelpCenterTab) { viewModel.onEvent(event: HelpCenterEventTabSelected(tab: tab)) }
    func reportProblem()         { viewModel.onEvent(event: HelpCenterEventReportProblemTapped.shared) }
    func contactUs()             { viewModel.onEvent(event: HelpCenterEventContactUsTapped.shared) }
    func retry()                 { viewModel.onEvent(event: HelpCenterEventRetryLoad.shared) }
}
