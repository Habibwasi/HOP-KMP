import Combine
import Shared

@MainActor
final class ChatViewModelWrapper: ObservableObject {

    let viewModel: ChatViewModel

    @Published var state: ChatUiState

    // Held so that a second call to startObserving() (e.g. on view re-appear)
    // cancels the previous Tasks before spawning new ones.  Without this,
    // viewModel.effect is consumed by two concurrent Tasks — receiveAsFlow()
    // is single-consumer, so effects are dropped nondeterministically.
    private var stateTask: Task<Void, Never>?
    private var effectTask: Task<Void, Never>?

    init() {
        let vm = KoinIOSKt.getChatViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any ChatEffect) -> Void) {
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

    func connect(bookingId: String, token: String) {
        viewModel.onEvent(event: ChatEventConnect(bookingId: bookingId, token: token))
    }

    func disconnect() { viewModel.onEvent(event: ChatEventDisconnect.shared) }
    func send()       { viewModel.onEvent(event: ChatEventSendMessage.shared) }

    func inputChanged(_ text: String) {
        viewModel.onEvent(event: ChatEventInputChanged(text: text))
    }
}
