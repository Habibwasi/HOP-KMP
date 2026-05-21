import Combine
import Shared

@MainActor
final class ChatListViewModelWrapper: ObservableObject {

    let viewModel: ChatListViewModel

    @Published var state: ChatListUiState

    // Guards against multi-Task re-entry on view re-appear.
    // viewModel.effect is receiveAsFlow() — single-consumer.
    private var stateTask: Task<Void, Never>?
    private var effectTask: Task<Void, Never>?

    init() {
        let vm = KoinIOSKt.getChatListViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any ChatListEffect) -> Void) {
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

    func load() {
        viewModel.onEvent(event: ChatListEventLoad.shared)
    }

    func openChat(bookingId: String) {
        viewModel.onEvent(event: ChatListEventOpenChat(bookingId: bookingId))
    }
}
