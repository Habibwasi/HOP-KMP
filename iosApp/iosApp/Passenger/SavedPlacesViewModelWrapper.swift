import Combine
import Shared

@MainActor
final class SavedPlacesViewModelWrapper: ObservableObject {

    let viewModel: SavedPlacesViewModel

    @Published var state: SavedPlacesUiState

    init() {
        let vm = KoinIOSKt.getSavedPlacesViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any SavedPlacesEffect) -> Void) {
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
        viewModel.onEvent(event: SavedPlacesEventLoad.shared)
    }

    func add(label: String, address: String, kind: SavedPlaceKind?) {
        viewModel.onEvent(event: SavedPlacesEventAdd(
            label: label,
            address: address,
            lat: nil,
            lng: nil,
            kind: kind
        ))
    }

    func delete(id: String) {
        viewModel.onEvent(event: SavedPlacesEventDelete(id: id))
    }
}
