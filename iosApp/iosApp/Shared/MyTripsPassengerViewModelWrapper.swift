import Combine
import Shared

@MainActor
final class MyTripsPassengerViewModelWrapper: ObservableObject {

    let viewModel: MyTripsPassengerViewModel

    @Published var state: MyTripsPassengerUiState

    init() {
        let vm = KoinIOSKt.getMyTripsPassengerViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any MyTripsPassengerEffect) -> Void) {
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

    func load() { viewModel.onEvent(event: MyTripsPassengerEventLoadTrips.shared) }

    func selectUpcoming(bookingId: String) {
        viewModel.onEvent(event: MyTripsPassengerEventSelectUpcomingTrip(bookingId: bookingId))
    }

    func selectPast(tripId: String) {
        viewModel.onEvent(event: MyTripsPassengerEventSelectPastTrip(tripId: tripId))
    }
}
