package com.example.hop.presentation.bookingsuccess

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.repository.BookingRepository
import com.example.hop.domain.repository.TripRepository
import com.example.hop.domain.repository.UserRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class BookingSuccessUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val tripModel: TripModel = TripModel.A,
    val originName: String = "",
    val destName: String = "",
    val departsAt: String = "",
    val driverName: String = "",
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface BookingSuccessEvent {
    data class Load(val bookingId: String) : BookingSuccessEvent
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

/**
 * PA-06 — Booking Success ViewModel.
 *
 * Loads the booking (to get [tripId]) then the trip (to get route, date, driver,
 * and model type). Navigation is handled entirely by the Route layer via callbacks;
 * no effects are emitted here.
 */
class BookingSuccessViewModel(
    private val bookingRepository: BookingRepository,
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BookingSuccessUiState())
    val state: StateFlow<BookingSuccessUiState> = _state.asStateFlow()

    fun onEvent(event: BookingSuccessEvent) {
        when (event) {
            is BookingSuccessEvent.Load -> load(event.bookingId)
        }
    }

    private fun load(bookingId: String) {
        if (!_state.value.isLoading && _state.value.error == null && _state.value.originName.isNotEmpty()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val bookingResponse = bookingRepository.getBooking(bookingId)) {
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = bookingResponse.message,
                    )
                }
                is ApiResponse.Success -> {
                    val tripId = bookingResponse.data.tripId
                    when (val tripResponse = tripRepository.getTripById(tripId)) {
                        is ApiResponse.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = tripResponse.message,
                            )
                        }
                        is ApiResponse.Success -> {
                            val trip = tripResponse.data
                            val driverUser = coroutineScope {
                                async { userRepository.getUserProfile(trip.driverId) }.await()
                            }
                            val driverName = when (driverUser) {
                                is ApiResponse.Success -> driverUser.data.fullName
                                is ApiResponse.Error -> ""
                            }
                            _state.value = _state.value.copy(
                                isLoading = false,
                                tripModel = trip.model,
                                originName = trip.originName,
                                destName = trip.destName,
                                departsAt = trip.departsAt,
                                driverName = driverName,
                            )
                        }
                    }
                }
            }
        }
    }
}
