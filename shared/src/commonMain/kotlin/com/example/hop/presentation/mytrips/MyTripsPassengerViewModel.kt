package com.example.hop.presentation.mytrips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.TripStatus
import com.example.hop.domain.repository.TripRepository
import com.example.hop.network.ApiResponse
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.presentation.model.toUiModels
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class MyTripsPassengerUiState(
    val isLoading: Boolean = false,
    val upcomingTrips: List<TripUiModel> = emptyList(),
    val pastTrips: List<TripUiModel> = emptyList(),
    val error: String? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface MyTripsPassengerEvent {
    data object LoadTrips : MyTripsPassengerEvent
    /**
     * [bookingId] is the Booking.id for the active trip.
     * The screen resolves it from TripUiModel.bookingId ?: TripUiModel.id.
     */
    data class SelectUpcomingTrip(val bookingId: String) : MyTripsPassengerEvent
    data class SelectPastTrip(val tripId: String) : MyTripsPassengerEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface MyTripsPassengerEffect {
    /** Navigate to the live/active trip view (PA-08). Carries the Booking.id. */
    data class NavigateToTripDetailActive(val bookingId: String) : MyTripsPassengerEffect
    /** Navigate to the read-only trip detail for review of past trips. */
    data class NavigateToTripDetail(val tripId: String) : MyTripsPassengerEffect
    data class ShowSnackbar(val message: String) : MyTripsPassengerEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class MyTripsPassengerViewModel(
    private val tripRepository: TripRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MyTripsPassengerUiState())
    val state: StateFlow<MyTripsPassengerUiState> = _state.asStateFlow()

    private val _effect = Channel<MyTripsPassengerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: MyTripsPassengerEvent) {
        when (event) {
            is MyTripsPassengerEvent.LoadTrips -> loadTrips()
            is MyTripsPassengerEvent.SelectUpcomingTrip -> selectUpcomingTrip(event.bookingId)
            is MyTripsPassengerEvent.SelectPastTrip -> selectPastTrip(event.tripId)
        }
    }

    private fun loadTrips() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = tripRepository.getMyTripsAsPassenger()) {
                is ApiResponse.Success -> {
                    val all = response.data.toUiModels()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        upcomingTrips = all.filter { it.status.isUpcoming() },
                        pastTrips = all.filter { it.status.isPast() },
                    )
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(MyTripsPassengerEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

private fun selectUpcomingTrip(bookingId: String) {
        viewModelScope.launch {
            _effect.send(MyTripsPassengerEffect.NavigateToTripDetailActive(bookingId))
        }
    }

    private fun selectPastTrip(tripId: String) {
        viewModelScope.launch {
            _effect.send(MyTripsPassengerEffect.NavigateToTripDetail(tripId))
        }
    }
}

// ─ Helpers ────────────────────────────────────────────────────────────────────

private fun TripStatus.isUpcoming(): Boolean = this == TripStatus.ACTIVE || this == TripStatus.CONFIRMED

private fun TripStatus.isPast(): Boolean = this == TripStatus.COMPLETED || this == TripStatus.CANCELLED
