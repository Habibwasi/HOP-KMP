package com.example.hop.presentation.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.LicenceStatus
import com.example.hop.domain.repository.DriverRepository
import com.example.hop.domain.repository.PostTripRequest
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

data class DriverUiState(
    val isLoading: Boolean = false,
    val trips: List<TripUiModel> = emptyList(),
    val error: String? = null,
    val licenceStatus: LicenceStatus? = null,
    val onboardingCarDetails: CarDetails? = null,
    val isSubmittingOnboarding: Boolean = false,
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
    data class SelectTrip(val tripId: String) : DriverEvent
    data object TapEarningsBanner : DriverEvent
    data class SaveCarDetails(val carDetails: CarDetails) : DriverEvent
    data class SubmitLicence(val photoUrl: String) : DriverEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface DriverEffect {
    data object NavigateToPostTrip : DriverEffect
    data class NavigateToTripDetail(val tripId: String) : DriverEffect
    data class ShowSnackbar(val message: String) : DriverEffect
    data object NavigateToTaxDashboard : DriverEffect
    data object NavigateToLicenceUpload : DriverEffect
    data object NavigateToReviewPending : DriverEffect
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

    // Prevents double-tap from queuing duplicate navigation effects
    private var isNavigating = false

    fun onEvent(event: DriverEvent) {
        when (event) {
            is DriverEvent.LoadDriverHome -> loadDriverHome()
            is DriverEvent.RequestPostTrip -> requestPostTrip()
            is DriverEvent.PostTripModelA -> postTrip(event.request)
            is DriverEvent.PostTripModelB -> postTrip(event.request)
            is DriverEvent.CompleteTrip -> completeTrip(event.tripId)
            is DriverEvent.LoadLicenceStatus -> loadLicenceStatus()
            is DriverEvent.SelectTrip -> selectTrip(event.tripId)
            is DriverEvent.TapEarningsBanner -> tapEarningsBanner()
            is DriverEvent.SaveCarDetails -> saveCarDetails(event.carDetails)
            is DriverEvent.SubmitLicence -> submitLicence(event.photoUrl)
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
                        trips = response.data.toUiModels(),
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
        if (isNavigating) return
        isNavigating = true
        viewModelScope.launch {
            _effect.send(DriverEffect.NavigateToPostTrip)
            isNavigating = false
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
                        trips = _state.value.trips.filterNot { it.id == tripId },
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

    private fun selectTrip(tripId: String) {
        viewModelScope.launch {
            _effect.send(DriverEffect.NavigateToTripDetail(tripId))
        }
    }

    private fun tapEarningsBanner() {
        viewModelScope.launch {
            _effect.send(DriverEffect.NavigateToTaxDashboard)
        }
    }

    private fun saveCarDetails(carDetails: CarDetails) {
        _state.value = _state.value.copy(onboardingCarDetails = carDetails)
        viewModelScope.launch {
            _effect.send(DriverEffect.NavigateToLicenceUpload)
        }
    }

    private fun submitLicence(photoUrl: String) {
        val carDetails = _state.value.onboardingCarDetails ?: return
        if (_state.value.isSubmittingOnboarding) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmittingOnboarding = true)
            when (val response = driverRepository.submitLicence(carDetails, photoUrl)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isSubmittingOnboarding = false)
                    _effect.send(DriverEffect.NavigateToReviewPending)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isSubmittingOnboarding = false)
                    _effect.send(DriverEffect.ShowSnackbar(response.message))
                }
            }
        }
    }
}
