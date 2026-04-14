package com.example.hop.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.Booking
import com.example.hop.domain.repository.BookingRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ Payment State ──────────────────────────────────────────────────────────────

enum class PaymentState {
    IDLE,
    PROCESSING,
    // Booking record created; user is in the MobilePay external flow.
    // SUCCESS is only set once the app receives confirmation via ConfirmPaymentSuccess.
    AWAITING_PAYMENT,
    SUCCESS,
    FAILED,
}

// ─ State ──────────────────────────────────────────────────────────────────────

data class BookingUiState(
    val isLoading: Boolean = false,
    val booking: Booking? = null,
    val error: String? = null,
    val paymentState: PaymentState = PaymentState.IDLE,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface BookingEvent {
    data class CreateBooking(val tripId: String, val seats: Int) : BookingEvent
    data class CancelBooking(val id: String) : BookingEvent
    data class SubmitRating(
        val bookingId: String,
        val stars: Int,
        val comment: String?,
    ) : BookingEvent
    // Fired when the app returns from MobilePay (via deep-link or onResume callback).
    // The UI layer is responsible for emitting this after confirming payment success.
    data class ConfirmPaymentSuccess(val bookingId: String) : BookingEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface BookingEffect {
    data class NavigateToMobilePay(val bookingId: String) : BookingEffect
    data class NavigateToSuccess(val bookingId: String) : BookingEffect
    data class ShowSnackbar(val message: String) : BookingEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class BookingViewModel(
    private val bookingRepository: BookingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state.asStateFlow()

    private val _effect = Channel<BookingEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: BookingEvent) {
        when (event) {
            is BookingEvent.CreateBooking -> createBooking(event.tripId, event.seats)
            is BookingEvent.CancelBooking -> cancelBooking(event.id)
            is BookingEvent.SubmitRating -> submitRating(event.bookingId, event.stars, event.comment)
            is BookingEvent.ConfirmPaymentSuccess -> confirmPaymentSuccess(event.bookingId)
        }
    }

    private fun createBooking(tripId: String, seats: Int) {
        // Idempotency guard: prevent double-booking if the user taps the button
        // before the PROCESSING state propagates back to the UI and disables it.
        if (_state.value.paymentState == PaymentState.PROCESSING) return
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                paymentState = PaymentState.PROCESSING,
            )
            when (val response = bookingRepository.createBooking(tripId, seats)) {
                is ApiResponse.Success -> {
                    // Booking record created. Payment is NOT complete yet.
                    // Transition to AWAITING_PAYMENT and hand off to MobilePay.
                    // SUCCESS is only set after ConfirmPaymentSuccess is received.
                    _state.value = _state.value.copy(
                        isLoading = false,
                        booking = response.data,
                        paymentState = PaymentState.AWAITING_PAYMENT,
                    )
                    _effect.send(BookingEffect.NavigateToMobilePay(response.data.id))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                        paymentState = PaymentState.FAILED,
                    )
                    _effect.send(BookingEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun confirmPaymentSuccess(bookingId: String) {
        // Idempotency guard: onResume can fire this multiple times during the MobilePay flow.
        // Only proceed if we are still waiting for payment confirmation.
        if (_state.value.paymentState != PaymentState.AWAITING_PAYMENT) return
        _state.value = _state.value.copy(paymentState = PaymentState.SUCCESS)
        viewModelScope.launch {
            _effect.send(BookingEffect.NavigateToSuccess(bookingId))
        }
    }

    private fun cancelBooking(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = bookingRepository.cancelBooking(id)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        booking = null,
                    )
                    _effect.send(BookingEffect.ShowSnackbar("Booking cancelled"))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(BookingEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun submitRating(bookingId: String, stars: Int, comment: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = bookingRepository.rateBooking(bookingId, stars, comment)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _effect.send(BookingEffect.NavigateToSuccess(bookingId))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(BookingEffect.ShowSnackbar(response.message))
                }
            }
        }
    }
}
