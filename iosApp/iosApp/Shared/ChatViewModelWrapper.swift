import Combine
import Shared

@MainActor
final class ChatViewModelWrapper: ObservableObject {

    let viewModel: ChatViewModel

    @Published var state: ChatUiState

    init() {
        let vm = KoinIOSKt.getChatViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any ChatEffect) -> Void) {
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

    func connect(bookingId: String, token: String) {
        viewModel.onEvent(event: ChatEventConnect(bookingId: bookingId, token: token))
    }

    func disconnect() { viewModel.onEvent(event: ChatEventDisconnect.shared) }
    func send()       { viewModel.onEvent(event: ChatEventSendMessage.shared) }

    func inputChanged(_ text: String) {
        viewModel.onEvent(event: ChatEventInputChanged(text: text))
    }
}
