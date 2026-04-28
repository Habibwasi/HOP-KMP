import Combine
import Shared

@MainActor
final class NotificationsViewModelWrapper: ObservableObject {

    let viewModel: NotificationsViewModel

    @Published var state: NotificationsUiState

    init() {
        let vm = KoinIOSKt.getNotificationsViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any NotificationsEffect) -> Void) {
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

    func load() { viewModel.onEvent(event: NotificationsEventLoad.shared) }
    func goToSearchTapped() { viewModel.onEvent(event: NotificationsEventGoToSearchTapped.shared) }
    func markRead(id: String) {
        viewModel.onEvent(event: NotificationsEventMarkRead(notificationId: id))
    }
}
