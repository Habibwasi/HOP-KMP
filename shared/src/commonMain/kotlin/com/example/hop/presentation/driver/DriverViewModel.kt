package com.example.hop.presentation.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.LicenceStatus
import com.example.hop.domain.model.PassengerSummary
import com.example.hop.domain.repository.DriverRepository
import com.example.hop.domain.repository.PostTripRequest
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

// ─ Post-trip draft models ─────────────────────────────────────────────────────

/**
 * Mutable draft for DR-06 Model A form.
 * Submitted as a whole when the driver taps "Next: Review Price".
 */
data class ModelADraft(
    val originName: String = "",
    val destName: String = "",
    /** ISO day abbreviations e.g. "MON", "TUE", "WED", "THU", "FRI" */
    val recurrenceDays: List<String> = emptyList(),
    /** "HH:mm" 24-hour format */
    val departureTime: String = "",
    val seatsTotal: Int = 1,
    /** Metres — populated from a routing API; 0 until resolved. */
    val distanceMetres: Int = 0,
)

/**
 * Mutable draft for DR-07 Model B form.
 * Submitted as a whole when the driver taps "Next: Review Price".
 */
data class ModelBDraft(
    val originName: String = "",
    val destName: String = "",
    /** "YYYY-MM-DD" */
    val date: String = "",
    /** "HH:mm" 24-hour format */
    val departureTime: String = "",
    val seatsTotal: Int = 1,
    val minThreshold: Int = 1,
    /** Metres — populated from a routing API; 0 until resolved. */
    val distanceMetres: Int = 0,
)

// ─ Active trip detail sub-state ──────────────────────────────────────────────

/**
 * Holds the loaded detail for DR-10 TripDetailActiveDriverScreen.
 * Loaded by [DriverEvent.LoadActiveTripDetail] and consumed by the
 * TripDetailActiveDriverRoute.
 */
data class ActiveTripDetailUiState(
    val isLoading: Boolean = false,
    val trip: TripUiModel? = null,
    val passengers: List<PassengerSummary> = emptyList(),
    val error: String? = null,
) {
    /** First booking id — used to seed the RatePassenger flow. */
    val firstBookingId: String? get() = passengers.firstOrNull()?.bookingId
}

// ─ State ──────────────────────────────────────────────────────────────────────

data class DriverUiState(
    val isLoading: Boolean = false,
    /** True only while a user-initiated pull-to-refresh is in flight. */
    val isRefreshing: Boolean = false,
    val trips: List<TripUiModel> = emptyList(),
    val error: String? = null,
    val licenceStatus: LicenceStatus? = null,
    val onboardingCarDetails: CarDetails? = null,
    val isSubmittingOnboarding: Boolean = false,
    /** Total earnings for current month in øre (1 DKK = 100 øre). */
    val monthlyEarningsOere: Int = 0,
    /** Platform-estimated tax for current month in øre. */
    val estimatedTaxOere: Int = 0,
    // ── Post-trip flow ──────────────────────────────────────────────────────
    val pendingModelADraft: ModelADraft? = null,
    val pendingModelBDraft: ModelBDraft? = null,
    val isPostingTrip: Boolean = false,
    val activeTripDetail: ActiveTripDetailUiState = ActiveTripDetailUiState(),
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface DriverEvent {
    data object LoadDriverHome : DriverEvent
    data object RefreshDriverHome : DriverEvent
    data object RequestPostTrip : DriverEvent
    data object SelectModelA : DriverEvent
    data object SelectModelB : DriverEvent
    data class SubmitModelADraft(val draft: ModelADraft) : DriverEvent
    data class SubmitModelBDraft(val draft: ModelBDraft) : DriverEvent
    data object ConfirmAndPostTrip : DriverEvent
    data class PostTripModelA(val request: PostTripRequest) : DriverEvent
    data class PostTripModelB(val request: PostTripRequest) : DriverEvent
    data class CompleteTrip(val tripId: String) : DriverEvent
    data class LoadActiveTripDetail(val tripId: String) : DriverEvent
    data object LoadLicenceStatus : DriverEvent
    data class SelectTrip(val tripId: String) : DriverEvent
    data object TapEarningsBanner : DriverEvent
    data class SaveCarDetails(val carDetails: CarDetails) : DriverEvent
    data class SubmitLicence(val photoUrl: String) : DriverEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface DriverEffect {
    data object NavigateToPostTrip : DriverEffect
    data object NavigateToModelAForm : DriverEffect
    data object NavigateToModelBForm : DriverEffect
    data object NavigateToPriceReview : DriverEffect
    data object NavigateToMyTrips : DriverEffect
    data class NavigateToTripDetail(val tripId: String) : DriverEffect
    data class NavigateToRatePassenger(val bookingId: String) : DriverEffect
    data class NavigateToMarkTripComplete(val tripId: String, val driverNetOere: Int) : DriverEffect
    data class ShowSnackbar(val message: String) : DriverEffect
    data object NavigateToTaxDashboard : DriverEffect
    data object NavigateToLicenceUpload : DriverEffect
    data object NavigateToReviewPending : DriverEffect
}

// ─ Error message mapper ───────────────────────────────────────────────────────

/**
 * Converts a raw [ApiResponse.Error] into a short, friendly message suitable
 * for display in a snackbar. Never exposes HTTP status codes or stack traces.
 *
 * @param context Optional hint (e.g. "loading trips") used only for the
 *   generic fallback so the message stays actionable without being technical.
 */
private fun ApiResponse.Error.toUserMessage(context: String = "completing your request"): String =
    when {
        // Network / connectivity
        code == -1 && (
            message.contains("Unable to resolve host", ignoreCase = true) ||
            message.contains("Network is unreachable", ignoreCase = true) ||
            message.contains("No address associated", ignoreCase = true) ||
            message.contains("Failed to connect", ignoreCase = true)
        ) -> "No internet connection. Please check your network and try again."

        code == -1 && (
            message.contains("timeout", ignoreCase = true) ||
            message.contains("timed out", ignoreCase = true)
        ) -> "The request took too long. Please try again."

        // Auth
        code == 401 -> "Your session has expired. Please log in again."
        code == 403 -> "You don't have permission to do that."

        // Client / server
        code == 404 -> "The information couldn't be found."
        code == 409 -> "This action conflicts with an existing record. Please refresh and try again."
        code == 422 -> "Some details were invalid. Please check your input and try again."
        code in 500..599 -> "Something went wrong on our end. Please try again in a moment."

        // Generic fallback — still friendly, still actionable
        else -> "Something went wrong while $context. Please try again."
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
            is DriverEvent.RefreshDriverHome -> loadDriverHome(refresh = true)
            is DriverEvent.RequestPostTrip -> requestPostTrip()
            is DriverEvent.SelectModelA -> selectModel(modelA = true)
            is DriverEvent.SelectModelB -> selectModel(modelA = false)
            is DriverEvent.SubmitModelADraft -> submitModelADraft(event.draft)
            is DriverEvent.SubmitModelBDraft -> submitModelBDraft(event.draft)
            is DriverEvent.ConfirmAndPostTrip -> confirmAndPostTrip()
            is DriverEvent.PostTripModelA -> postTrip(event.request)
            is DriverEvent.PostTripModelB -> postTrip(event.request)
            is DriverEvent.CompleteTrip -> completeTrip(event.tripId)
            is DriverEvent.LoadActiveTripDetail -> loadActiveTripDetail(event.tripId)
            is DriverEvent.LoadLicenceStatus -> loadLicenceStatus()
            is DriverEvent.SelectTrip -> selectTrip(event.tripId)
            is DriverEvent.TapEarningsBanner -> tapEarningsBanner()
            is DriverEvent.SaveCarDetails -> saveCarDetails(event.carDetails)
            is DriverEvent.SubmitLicence -> submitLicence(event.photoUrl)
        }
    }

    private fun loadDriverHome(refresh: Boolean = false) {
        if (_state.value.isLoading || _state.value.isRefreshing) return
        viewModelScope.launch {
            _state.value = if (refresh) {
                _state.value.copy(isRefreshing = true, error = null)
            } else {
                _state.value.copy(isLoading = true, error = null)
            }
            when (val response = tripRepository.getMyTripsAsDriver()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        trips = response.data.toUiModels(),
                    )
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = response.toUserMessage("loading your trips"),
                    )
                    _effect.send(DriverEffect.ShowSnackbar(response.toUserMessage("loading your trips")))
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

    private fun selectModel(modelA: Boolean) {
        if (isNavigating) return
        isNavigating = true
        viewModelScope.launch {
            _effect.send(
                if (modelA) DriverEffect.NavigateToModelAForm
                else DriverEffect.NavigateToModelBForm
            )
            isNavigating = false
        }
    }

    private fun submitModelADraft(draft: ModelADraft) {
        _state.value = _state.value.copy(
            pendingModelADraft = draft,
            pendingModelBDraft = null,
        )
        viewModelScope.launch { _effect.send(DriverEffect.NavigateToPriceReview) }
    }

    private fun submitModelBDraft(draft: ModelBDraft) {
        _state.value = _state.value.copy(
            pendingModelBDraft = draft,
            pendingModelADraft = null,
        )
        viewModelScope.launch { _effect.send(DriverEffect.NavigateToPriceReview) }
    }

    private fun confirmAndPostTrip() {
        if (_state.value.isPostingTrip) return
        val modelADraft = _state.value.pendingModelADraft
        val modelBDraft = _state.value.pendingModelBDraft
        val request = when {
            modelADraft != null -> PostTripRequest(
                model = "A",
                originName = modelADraft.originName,
                originLat = 0.0,
                originLng = 0.0,
                destName = modelADraft.destName,
                destLat = 0.0,
                destLng = 0.0,
                distanceMetres = modelADraft.distanceMetres,
                departsAt = "${modelADraft.departureTime}:00Z",
                seatsTotal = modelADraft.seatsTotal,
                recurrenceDays = modelADraft.recurrenceDays,
            )
            modelBDraft != null -> PostTripRequest(
                model = "B",
                originName = modelBDraft.originName,
                originLat = 0.0,
                originLng = 0.0,
                destName = modelBDraft.destName,
                destLat = 0.0,
                destLng = 0.0,
                distanceMetres = modelBDraft.distanceMetres,
                departsAt = "${modelBDraft.date}T${modelBDraft.departureTime}:00Z",
                seatsTotal = modelBDraft.seatsTotal,
                minThreshold = modelBDraft.minThreshold,
            )
            else -> return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isPostingTrip = true, error = null)
            when (val response = tripRepository.postTrip(request)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isPostingTrip = false,
                        pendingModelADraft = null,
                        pendingModelBDraft = null,
                    )
                    _effect.send(DriverEffect.NavigateToMyTrips)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isPostingTrip = false, error = response.toUserMessage("posting your trip"))
                    _effect.send(DriverEffect.ShowSnackbar(response.toUserMessage("posting your trip")))
                }
            }
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
                        error = response.toUserMessage("posting your trip"),
                    )
                    _effect.send(DriverEffect.ShowSnackbar(response.toUserMessage("posting your trip")))
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
                    val firstBookingId = _state.value.activeTripDetail.firstBookingId
                    _state.value = _state.value.copy(
                        isLoading = false,
                        trips = _state.value.trips.filterNot { it.id == tripId },
                    )
                    if (firstBookingId != null) {
                        _effect.send(DriverEffect.NavigateToRatePassenger(firstBookingId))
                    } else {
                        _effect.send(DriverEffect.NavigateToMyTrips)
                    }
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.toUserMessage("completing the trip"),
                    )
                    _effect.send(DriverEffect.ShowSnackbar(response.toUserMessage("completing the trip")))
                }
            }
        }
    }

    private fun loadActiveTripDetail(tripId: String) {
        if (_state.value.activeTripDetail.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(
                activeTripDetail = _state.value.activeTripDetail.copy(
                    isLoading = true,
                    error = null,
                )
            )
            val tripResponse = tripRepository.getTripById(tripId)
            val passengersResponse = tripRepository.getTripPassengers(tripId)

            if (tripResponse is ApiResponse.Error) {
                _state.value = _state.value.copy(
                    activeTripDetail = _state.value.activeTripDetail.copy(
                        isLoading = false,
                        error = tripResponse.toUserMessage("loading trip details"),
                    )
                )
                return@launch
            }

            val trip = (tripResponse as ApiResponse.Success).data
            val passengers = when (passengersResponse) {
                is ApiResponse.Success -> passengersResponse.data
                is ApiResponse.Error -> emptyList()
            }
            _state.value = _state.value.copy(
                activeTripDetail = ActiveTripDetailUiState(
                    isLoading = false,
                    trip = trip.toUiModel(),
                    passengers = passengers,
                    error = null,
                )
            )
        }
    }

    private fun loadLicenceStatus() {
        viewModelScope.launch {
            when (val response = driverRepository.getLicenceStatus()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(licenceStatus = response.data)
                }
                is ApiResponse.Error -> {
                    // Silently ignore — licence status is advisory; a snackbar
                    // on every Home open would be intrusive for non-drivers.
                }
            }
        }
    }

    private fun selectTrip(tripId: String) {
        if (isNavigating) return
        isNavigating = true
        viewModelScope.launch {
            _effect.send(DriverEffect.NavigateToTripDetail(tripId))
            isNavigating = false
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
                    _effect.send(DriverEffect.ShowSnackbar(response.toUserMessage("submitting your application")))
                }
            }
        }
    }
}
