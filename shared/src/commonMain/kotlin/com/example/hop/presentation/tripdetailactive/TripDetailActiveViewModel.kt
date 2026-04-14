package com.example.hop.presentation.tripdetailactive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.BookingStatus
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.repository.BookingRepository
import com.example.hop.domain.repository.TripRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class TripDetailActiveUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val bookingId: String = "",
    val tripId: String = "",
    val bookingStatus: BookingStatus = BookingStatus.UNKNOWN,
    val driverName: String = "",
    val driverInitials: String = "",
    val driverPhone: String = "",
    val driverRating: Float = 0f,
    val isDriverVerified: Boolean = false,
    val originName: String = "",
    val destName: String = "",
    val departsAt: String = "",
    val seatsBooked: Int = 0,
    val minThreshold: Int? = null,
    val tripModel: TripModel = TripModel.A,
) {
    /** Fraction of seats confirmed vs. the minimum threshold (Model B only). */
    val thresholdProgress: Float
        get() = if (tripModel == TripModel.B && minThreshold != null && minThreshold > 0)
            (seatsBooked.toFloat() / minThreshold.toFloat()).coerceIn(0f, 1f)
        else 0f
}

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface TripDetailActiveEvent {
    data class Load(val bookingId: String) : TripDetailActiveEvent
    data object MessageDriver : TripDetailActiveEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface TripDetailActiveEffect {
    data class NavigateToChat(val bookingId: String) : TripDetailActiveEffect
    data class ShowSnackbar(val message: String) : TripDetailActiveEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class TripDetailActiveViewModel(
    private val bookingRepository: BookingRepository,
    private val tripRepository: TripRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TripDetailActiveUiState())
    val state: StateFlow<TripDetailActiveUiState> = _state.asStateFlow()

    private val _effect = Channel<TripDetailActiveEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: TripDetailActiveEvent) {
        when (event) {
            is TripDetailActiveEvent.Load -> load(event.bookingId)
            TripDetailActiveEvent.MessageDriver -> messageDriver()
        }
    }

    private fun load(bookingId: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, bookingId = bookingId)
            when (val bookingResponse = bookingRepository.getBooking(bookingId)) {
                is ApiResponse.Success -> {
                    val booking = bookingResponse.data
                    when (val tripResponse = tripRepository.getTripById(booking.tripId)) {
                        is ApiResponse.Success -> {
                            val trip = tripResponse.data
                            val initials = trip.driverId.take(2).uppercase()
                            _state.value = _state.value.copy(
                                isLoading = false,
                                tripId = trip.id,
                                bookingStatus = booking.status,
                                driverName = "Driver",         // TODO: resolve via UserRepository
                                driverInitials = initials,
                                driverPhone = "",              // TODO: resolve via UserRepository
                                driverRating = 4.8f,           // TODO: resolve via UserRepository
                                isDriverVerified = true,       // TODO: resolve via UserRepository
                                originName = trip.originName,
                                destName = trip.destName,
                                departsAt = trip.departsAt,
                                seatsBooked = trip.seatsBooked,
                                minThreshold = trip.minThreshold,
                                tripModel = trip.model,
                            )
                        }
                        is ApiResponse.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = tripResponse.message,
                            )
                            _effect.send(TripDetailActiveEffect.ShowSnackbar(tripResponse.message))
                        }
                    }
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = bookingResponse.message,
                    )
                    _effect.send(TripDetailActiveEffect.ShowSnackbar(bookingResponse.message))
                }
            }
        }
    }

    private fun messageDriver() {
        val bookingId = _state.value.bookingId
        if (bookingId.isEmpty()) return
        viewModelScope.launch {
            _effect.send(TripDetailActiveEffect.NavigateToChat(bookingId))
        }
    }
}
