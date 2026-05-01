package com.example.hop.presentation.tripdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.repository.TripRepository
import com.example.hop.domain.repository.UserRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class TripDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tripId: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val driverInitials: String = "",
    val driverRating: Float = 0f,
    val isDriverVerified: Boolean = false,
    val originName: String = "",
    val destName: String = "",
    val departsAt: String = "",
    val distanceMetres: Int = 0,
    val seatsTotal: Int = 0,
    val seatsBooked: Int = 0,
    val minThreshold: Int? = null,
    val model: TripModel = TripModel.A,
    val priceOerePerSeat: Int = 0,
    val platformFeeOere: Int = 0,
) {
    val seatsAvailable: Int
        get() = (seatsTotal - seatsBooked).coerceAtLeast(0)

    /** Rough duration estimate at avg 90 km/h (highway + urban mix). */
    val estimatedDurationMinutes: Int
        get() = ((distanceMetres / 1000f) / 90f * 60f).toInt()

    val thresholdProgress: Float
        get() = if (model == TripModel.B && minThreshold != null && minThreshold > 0)
            (seatsBooked.toFloat() / minThreshold.toFloat()).coerceIn(0f, 1f)
        else 0f
}

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface TripDetailEvent {
    data class LoadTrip(val tripId: String) : TripDetailEvent
    data object BookSeat : TripDetailEvent
    data object ViewDriverProfile : TripDetailEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface TripDetailEffect {
    data class NavigateToBookingConfirmation(val tripId: String) : TripDetailEffect
    data class NavigateToOtherProfile(val driverId: String) : TripDetailEffect
    data class ShowSnackbar(val message: String) : TripDetailEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class TripDetailViewModel(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TripDetailUiState())
    val state: StateFlow<TripDetailUiState> = _state.asStateFlow()

    private val _effect = Channel<TripDetailEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: TripDetailEvent) {
        when (event) {
            is TripDetailEvent.LoadTrip -> loadTrip(event.tripId)
            TripDetailEvent.BookSeat -> bookSeat()
            TripDetailEvent.ViewDriverProfile -> viewDriverProfile()
        }
    }

    private fun loadTrip(tripId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = tripRepository.getTripById(tripId)) {
                is ApiResponse.Success -> {
                    val trip = response.data
                    val platformFeeOere = (trip.priceOerePerSeat - trip.driverNetOere).coerceAtLeast(0)

                    // Fetch driver profile in parallel — best-effort; failures degrade gracefully.
                    val driverUser = coroutineScope {
                        async { userRepository.getUserProfile(trip.driverId) }.await()
                    }
                    val driverName = when (driverUser) {
                        is ApiResponse.Success -> driverUser.data.fullName
                        is ApiResponse.Error -> ""
                    }
                    val driverRating = when (driverUser) {
                        is ApiResponse.Success -> driverUser.data.ratingDriver?.toFloat() ?: 0f
                        is ApiResponse.Error -> 0f
                    }
                    val isDriverVerified = when (driverUser) {
                        is ApiResponse.Success -> driverUser.data.roles.contains(
                            com.example.hop.domain.model.UserRole.DRIVER
                        )
                        is ApiResponse.Error -> false
                    }
                    val initials = driverName
                        .split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .take(2)
                        .joinToString("")
                        .ifEmpty { trip.driverId.take(2).uppercase() }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        tripId = trip.id,
                        driverId = trip.driverId,
                        driverName = driverName,
                        driverInitials = initials,
                        driverRating = driverRating,
                        isDriverVerified = isDriverVerified,
                        originName = trip.originName,
                        destName = trip.destName,
                        departsAt = trip.departsAt,
                        distanceMetres = trip.distanceMetres,
                        seatsTotal = trip.seatsTotal,
                        seatsBooked = trip.seatsBooked,
                        minThreshold = trip.minThreshold,
                        model = trip.model,
                        priceOerePerSeat = trip.priceOerePerSeat,
                        platformFeeOere = platformFeeOere,
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(TripDetailEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun bookSeat() {
        val tripId = _state.value.tripId
        if (tripId.isEmpty()) return
        viewModelScope.launch {
            _effect.send(TripDetailEffect.NavigateToBookingConfirmation(tripId))
        }
    }

    private fun viewDriverProfile() {
        val driverId = _state.value.driverId
        if (driverId.isEmpty()) return
        viewModelScope.launch {
            _effect.send(TripDetailEffect.NavigateToOtherProfile(driverId))
        }
    }
}
