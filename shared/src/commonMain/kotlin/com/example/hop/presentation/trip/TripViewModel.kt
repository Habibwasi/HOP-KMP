package com.example.hop.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.repository.TripRepository
import com.example.hop.network.ApiResponse
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.presentation.model.toUiModel
import com.example.hop.presentation.model.toUiModels
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class TripUiState(
    val isLoading: Boolean = false,
    val trips: List<TripUiModel> = emptyList(),
    val error: String? = null,
    val selectedTrip: TripUiModel? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface TripEvent {
    data object LoadMyTripsPassenger : TripEvent
    data object LoadMyTripsDriver : TripEvent
    data class SelectTrip(val id: String) : TripEvent
    data class CompleteTrip(val id: String) : TripEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface TripEffect {
    data class NavigateToTripDetail(val tripId: String) : TripEffect
    data class ShowSnackbar(val message: String) : TripEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class TripViewModel(
    private val tripRepository: TripRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TripUiState())
    val state: StateFlow<TripUiState> = _state.asStateFlow()

    private val _effect = Channel<TripEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: TripEvent) {
        when (event) {
            is TripEvent.LoadMyTripsPassenger -> loadMyTripsPassenger()
            is TripEvent.LoadMyTripsDriver -> loadMyTripsDriver()
            is TripEvent.SelectTrip -> selectTrip(event.id)
            is TripEvent.CompleteTrip -> completeTrip(event.id)
        }
    }

    private fun loadMyTripsPassenger() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = tripRepository.getMyTripsAsPassenger()) {
                is ApiResponse.Success -> {
                    val uiModels = response.data.toUiModels()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        trips = uiModels,
                    )
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(TripEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun loadMyTripsDriver() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = tripRepository.getMyTripsAsDriver()) {
                is ApiResponse.Success -> {
                    val uiModels = response.data.toUiModels()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        trips = uiModels,
                    )
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(TripEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun selectTrip(id: String) {
        val cached = _state.value.trips.firstOrNull { it.id == id }
        if (cached != null) {
            _state.value = _state.value.copy(selectedTrip = cached)
            viewModelScope.launch { _effect.send(TripEffect.NavigateToTripDetail(id)) }
        } else {
            // Cache miss: deep-link or cross-screen navigation before list is loaded.
            // Fetch the trip directly so navigation always succeeds.
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true, error = null)
                when (val response = tripRepository.getTripById(id)) {
                    is ApiResponse.Success -> {
                        val uiModel = response.data.toUiModel()
                        _state.value = _state.value.copy(isLoading = false, selectedTrip = uiModel)
                        _effect.send(TripEffect.NavigateToTripDetail(id))
                    }
                    is ApiResponse.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = response.message,
                        )
                        _effect.send(TripEffect.ShowSnackbar(response.message))
                    }
                }
            }
        }
    }

    private fun completeTrip(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = tripRepository.completeTrip(id)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _effect.send(TripEffect.ShowSnackbar("Trip completed successfully"))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(TripEffect.ShowSnackbar(response.message))
                }
            }
        }
    }
}
