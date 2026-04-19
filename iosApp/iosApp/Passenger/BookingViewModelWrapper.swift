import SwiftUI
import Combine
import Shared

// ── BookingViewModelWrapper ───────────────────────────────────────────────────
//
// @StateObject bridge for shared BookingViewModel (KMP/SKIE).
//
// SKIE flat names for BookingEvent:
//   BookingEvent.CreateBooking         → BookingEventCreateBooking
//   BookingEvent.CancelBooking         → BookingEventCancelBooking
//   BookingEvent.SubmitRating          → BookingEventSubmitRating
//   BookingEvent.BeginHandoff          → BookingEventBeginHandoff
//   BookingEvent.ConfirmPaymentSuccess → BookingEventConfirmPaymentSuccess
//
// SKIE flat names for BookingEffect:
//   BookingEffect.NavigateToMobilePay              → BookingEffectNavigateToMobilePay
//   BookingEffect.NavigateToSuccess                → BookingEffectNavigateToSuccess
//   BookingEffect.NavigateToCancellationConfirmation → BookingEffectNavigateToCancellationConfirmation
//   BookingEffect.NavigateToMyTripsPassenger       → BookingEffectNavigateToMyTripsPassenger (data object → .shared)
//   BookingEffect.ShowSnackbar                     → BookingEffectShowSnackbar

@MainActor
final class BookingViewModelWrapper: ObservableObject {

    let viewModel: BookingViewModel

    @Published var state: BookingUiState

    init() {
        let vm = KoinIOSKt.getBookingViewModel()
        self.viewModel = vm
        self.state = vm.state.value
    }

    func startObserving(onEffect: @escaping (any BookingEffect) -> Void) {
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

    // ── Convenience dispatchers ────────────────────────────────────────────────

    func createBooking(tripId: String, seats: Int) {
        viewModel.onEvent(event: BookingEventCreateBooking(tripId: tripId, seats: Int32(seats)))
    }

    func cancelBooking(id: String) {
        viewModel.onEvent(event: BookingEventCancelBooking(id: id))
    }

    func submitRating(bookingId: String, stars: Int, comment: String?) {
        viewModel.onEvent(event: BookingEventSubmitRating(
            bookingId: bookingId,
            stars: Int32(stars),
            comment: comment
        ))
    }

    func beginHandoff(bookingId: String) {
        viewModel.onEvent(event: BookingEventBeginHandoff(bookingId: bookingId))
    }

    func confirmPaymentSuccess(bookingId: String) {
        viewModel.onEvent(event: BookingEventConfirmPaymentSuccess(bookingId: bookingId))
    }
}
