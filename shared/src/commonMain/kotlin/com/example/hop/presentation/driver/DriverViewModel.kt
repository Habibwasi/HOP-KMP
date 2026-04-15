package com.example.hop.presentation.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.LicenceStatus
import com.example.hop.domain.model.Trip
import com.example.hop.domain.repository.DriverRepository
import com.example.hop.domain.repository.PostTripRequest
import com.example.hop.domain.repository.TripRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class DriverUiState(
    val isLoading: Boolean = false,
    val trips: List<Trip> = emptyList(),
    val error: String? = null,
    val licenceStatus: LicenceStatus? = null,
    /** Total earnings for current month in øre (1 DKK = 100 øre). */
    val monthlyEarningsOere: Int = 0,
    /** Platform-estimated tax for current month in øre. */
    val estimatedTaxOere: Int = 0,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface DriverEvent {
    data object LoadDriverHome : DriverEvent
    data object RequestPostTrip : DriverEvent
    data class PostTripModelA(val request: PostTripRequest) : DriverEvent
    data class PostTripModelB(val request: PostTripRequest) : DriverEvent
    data class CompleteTrip(val tripId: String) : DriverEvent
    data object LoadLicenceStatus : DriverEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface DriverEffect {
    data object NavigateToPostTrip : DriverEffect
    data class NavigateToTripDetail(val tripId: String) : DriverEffect
    data class ShowSnackbar(val message: String) : DriverEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class DriverViewModel(
    private val tripRepository: TripRepository,
    private val driverRepository: DriverRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DriverUiState())
    val state: StateFlow<DriverUiState> = _state.asStateFlow()

    private val _effect = Channel<DriverEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: DriverEvent) {
        when (event) {
            is DriverEvent.LoadDriverHome -> loadDriverHome()
            is DriverEvent.RequestPostTrip -> requestPostTrip()
            is DriverEvent.PostTripModelA -> postTrip(event.request)
            is DriverEvent.PostTripModelB -> postTrip(event.request)
            is DriverEvent.CompleteTrip -> completeTrip(event.tripId)
            is DriverEvent.LoadLicenceStatus -> loadLicenceStatus()
        }
    }

    private fun loadDriverHome() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = tripRepository.getMyTripsAsDriver()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        trips = response.data,
                    )
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(DriverEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun requestPostTrip() {
        viewModelScope.launch {
            _effect.send(DriverEffect.NavigateToPostTrip)
        }
    }

    private fun postTrip(request: PostTripRequest) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = tripRepository.postTrip(request)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _effect.send(DriverEffect.NavigateToTripDetail(response.data.id))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(DriverEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun completeTrip(tripId: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = tripRepository.completeTrip(tripId)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        trips = _state.value.trips.filter { it.id != tripId },
                    )
                    _effect.send(DriverEffect.NavigateToTripDetail(tripId))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(DriverEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun loadLicenceStatus() {
        viewModelScope.launch {
            when (val response = driverRepository.getLicenceStatus()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(licenceStatus = response.data)
                }
                is ApiResponse.Error -> {
                    _effect.send(DriverEffect.ShowSnackbar(response.message))
                }
            }
        }
    }
}
