import Combine
import Shared

@MainActor
final class HomeStatsViewModelWrapper: ObservableObject {

    let viewModel: HomeStatsViewModel

    @Published var state: HomeStatsUiState

    init() {
        let vm = KoinIOSKt.getHomeStatsViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any HomeStatsEffect) -> Void) {
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

    func load() { viewModel.onEvent(event: HomeStatsEventLoad.shared) }

    func deleteRecentSearch(id: String) {
        viewModel.onEvent(event: HomeStatsEventDeleteRecentSearch(id: id))
    }
}
