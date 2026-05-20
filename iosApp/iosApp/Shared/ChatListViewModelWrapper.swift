import Combine
import Shared

@MainActor
final class ChatListViewModelWrapper: ObservableObject {

    let viewModel: ChatListViewModel

    @Published var state: ChatListUiState

    init() {
        let vm = KoinIOSKt.getChatListViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any ChatListEffect) -> Void) {
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

    func load() {
        viewModel.onEvent(event: ChatListEventLoad.shared)
    }

    func openChat(bookingId: String) {
        viewModel.onEvent(event: ChatListEventOpenChat(bookingId: bookingId))
    }
}
