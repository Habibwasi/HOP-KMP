import Combine
import Shared

@MainActor
final class SearchViewModelWrapper: ObservableObject {

    let viewModel: SearchViewModel

    @Published var state: SearchUiState
    @Published var alertToast: String? = nil

    private var stateTask: Task<Void, Never>?
    private var effectTask: Task<Void, Never>?

    init() {
        let vm = KoinIOSKt.getSearchViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any SearchEffect) -> Void) {
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

    func search(origin: String, dest: String, date: String, seats: Int) {
        viewModel.onEvent(event: SearchEventSearch(
            origin: origin,
            dest: dest,
            date: date,
            seats: Int32(seats)
        ))
    }

    func applyFilter(_ filter: SearchFilter) {
        viewModel.onEvent(event: SearchEventApplyFilter(filter: filter))
    }

    func clearFilters() {
        viewModel.onEvent(event: SearchEventClearFilters.shared)
    }

    func selectTrip(tripId: String) {
        viewModel.onEvent(event: SearchEventSelectTrip(tripId: tripId))
    }

    func createAlert() {
        viewModel.onEvent(event: SearchEventAlertMe.shared)
    }
}
