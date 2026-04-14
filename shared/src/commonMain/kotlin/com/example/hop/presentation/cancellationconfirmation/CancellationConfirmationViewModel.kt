package com.example.hop.presentation.cancellationconfirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.repository.BookingRepository
import com.example.hop.domain.repository.TripRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class CancellationConfirmationUiState(
    val isLoading: Boolean = false,
    /** Total refund in øre (seats × pricePerSeat). Null while loading. */
    val refundAmountOere: Int? = null,
    /** True if the trip was Model B — affects the refund note copy. */
    val isModelB: Boolean = false,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface CancellationConfirmationEvent {
    data class Load(val bookingId: String) : CancellationConfirmationEvent
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class CancellationConfirmationViewModel(
    private val bookingRepository: BookingRepository,
    private val tripRepository: TripRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CancellationConfirmationUiState())
    val state: StateFlow<CancellationConfirmationUiState> = _state.asStateFlow()

    fun onEvent(event: CancellationConfirmationEvent) {
        when (event) {
            is CancellationConfirmationEvent.Load -> load(event.bookingId)
        }
    }

    private fun load(bookingId: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val bookingResponse = bookingRepository.getBooking(bookingId)) {
                is ApiResponse.Success -> {
                    val booking = bookingResponse.data
                    when (val tripResponse = tripRepository.getTripById(booking.tripId)) {
                        is ApiResponse.Success -> {
                            val trip = tripResponse.data
                            _state.value = _state.value.copy(
                                isLoading = false,
                                refundAmountOere = trip.priceOerePerSeat * booking.seats,
                                isModelB = trip.model == TripModel.B,
                            )
                        }
                        is ApiResponse.Error -> {
                            // Non-fatal: screen still shows confirmation, just without amount.
                            _state.value = _state.value.copy(isLoading = false)
                        }
                    }
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }
}
