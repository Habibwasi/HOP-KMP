package com.example.hop.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.Booking
import com.example.hop.domain.repository.BookingRepository
import com.example.hop.domain.repository.TripRepository
import com.example.hop.domain.repository.UserRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class BookingUiState(
    val isLoading: Boolean = false,
    val booking: Booking? = null,
    val error: String? = null,
    val driverName: String = "",
    val driverInitials: String = "",
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
    data class LoadDriverForRating(val bookingId: String) : BookingEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface BookingEffect {
    data class NavigateToSuccess(val bookingId: String) : BookingEffect
    data class NavigateToCancellationConfirmation(val bookingId: String) : BookingEffect
    data object NavigateToMyTripsPassenger : BookingEffect
    data class ShowSnackbar(val message: String) : BookingEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class BookingViewModel(
    private val bookingRepository: BookingRepository,
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
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
            is BookingEvent.LoadDriverForRating -> loadDriverForRating(event.bookingId)
        }
    }

    private fun createBooking(tripId: String, seats: Int) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = bookingRepository.createBooking(tripId, seats)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isLoading = false, booking = response.data)
                    _effect.send(BookingEffect.NavigateToSuccess(response.data.id))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = response.message)
                    _effect.send(BookingEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun cancelBooking(id: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = bookingRepository.cancelBooking(id)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isLoading = false, booking = null)
                    _effect.send(BookingEffect.NavigateToCancellationConfirmation(id))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = response.message)
                    _effect.send(BookingEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun submitRating(bookingId: String, stars: Int, comment: String?) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = bookingRepository.rateBooking(bookingId, stars, comment)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _effect.send(BookingEffect.NavigateToMyTripsPassenger)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = response.message)
                    _effect.send(BookingEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun loadDriverForRating(bookingId: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val bookingResponse = bookingRepository.getBooking(bookingId)) {
                is ApiResponse.Success -> {
                    val booking = bookingResponse.data
                    when (val tripResponse = tripRepository.getTripById(booking.tripId)) {
                        is ApiResponse.Success -> {
                            val trip = tripResponse.data
                            val driverUser = userRepository.getUserProfile(trip.driverId)
                            val driverName = when (driverUser) {
                                is ApiResponse.Success -> driverUser.data.fullName
                                is ApiResponse.Error -> ""
                            }
                            val driverInitials = driverName
                                .split(" ")
                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                .take(2)
                                .joinToString("")
                                .ifEmpty { trip.driverId.take(2).uppercase() }
                            _state.value = _state.value.copy(
                                isLoading = false,
                                driverName = driverName,
                                driverInitials = driverInitials,
                            )
                        }
                        is ApiResponse.Error -> {
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
