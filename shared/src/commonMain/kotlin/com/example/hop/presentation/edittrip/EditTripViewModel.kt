package com.example.hop.presentation.edittrip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.repository.RoutingRepository
import com.example.hop.domain.repository.TripRepository
import com.example.hop.domain.repository.UpdateTripRequest
import com.example.hop.network.ApiResponse
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.presentation.model.toUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ── Draft ─────────────────────────────────────────────────────────────────────

/**
 * Mutable draft holding all editable fields for the edit-trip form.
 * Mirrors the existing trip's values on load, then mutated by the driver.
 */
data class EditTripDraft(
    val tripId: String = "",
    val model: TripModel = TripModel.B,
    val originName: String = "",
    val originLat: Double = 0.0,
    val originLng: Double = 0.0,
    val destName: String = "",
    val destLat: Double = 0.0,
    val destLng: Double = 0.0,
    /** Full ISO-8601 departure datetime (used for Model B). */
    val departsAt: String = "",
    /** "HH:mm" 24-hour departure time component (pre-filled for both models). */
    val departureTime: String = "",
    /** "YYYY-MM-DD" date component (Model B only). */
    val date: String = "",
    val distanceMetres: Int = 0,
    val seatsTotal: Int = 1,
)

// ── State ─────────────────────────────────────────────────────────────────────

data class EditTripUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val trip: TripUiModel? = null,
    /** Non-null once the driver taps "Review changes" — holds the draft to display in the price review. */
    val pendingDraft: EditTripDraft? = null,
    val isCalculatingRoute: Boolean = false,
    val routeDistanceMetres: Int = 0,
    val routeOriginLat: Double = 0.0,
    val routeOriginLng: Double = 0.0,
    val routeDestLat: Double = 0.0,
    val routeDestLng: Double = 0.0,
)

// ── Events ────────────────────────────────────────────────────────────────────

sealed interface EditTripEvent {
    data class LoadTrip(val tripId: String) : EditTripEvent
    data class SubmitDraft(val draft: EditTripDraft) : EditTripEvent
    data class CalculateRoute(val originName: String, val destName: String) : EditTripEvent
    data object ConfirmAndUpdate : EditTripEvent
}

// ── Effects ───────────────────────────────────────────────────────────────────

sealed interface EditTripEffect {
    data object NavigateToPriceReview : EditTripEffect
    data object NavigateBack : EditTripEffect
    data object NavigateToMyTrips : EditTripEffect
    data class ShowSnackbar(val message: String) : EditTripEffect
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class EditTripViewModel(
    private val tripRepository: TripRepository,
    private val routingRepository: RoutingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditTripUiState())
    val state: StateFlow<EditTripUiState> = _state.asStateFlow()

    private val _effect = Channel<EditTripEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: EditTripEvent) {
        when (event) {
            is EditTripEvent.LoadTrip -> loadTrip(event.tripId)
            is EditTripEvent.CalculateRoute -> calculateRoute(event.originName, event.destName)
            is EditTripEvent.SubmitDraft -> submitDraft(event.draft)
            is EditTripEvent.ConfirmAndUpdate -> confirmAndUpdate()
        }
    }

    private fun loadTrip(tripId: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = tripRepository.getTripById(tripId)) {
                is ApiResponse.Success -> {
                    val uiModel = result.data.toUiModel()
                    _state.value = _state.value.copy(isLoading = false, trip = uiModel)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Could not load trip.")
                }
            }
        }
    }

    private fun calculateRoute(originName: String, destName: String) {
        if (_state.value.isCalculatingRoute) return
        _state.value = _state.value.copy(isCalculatingRoute = true)
        viewModelScope.launch {
            when (val result = routingRepository.getRouteInfo(originName, destName)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isCalculatingRoute = false,
                        routeDistanceMetres = result.data.distanceMetres,
                        routeOriginLat = result.data.originLat,
                        routeOriginLng = result.data.originLng,
                        routeDestLat = result.data.destLat,
                        routeDestLng = result.data.destLng,
                    )
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isCalculatingRoute = false)
                }
            }
        }
    }

    private fun submitDraft(draft: EditTripDraft) {
        val s = _state.value
        val distanceMetres = if (s.routeDistanceMetres > 0) s.routeDistanceMetres else draft.distanceMetres
        val originLat = if (s.routeOriginLat != 0.0) s.routeOriginLat else draft.originLat
        val originLng = if (s.routeOriginLng != 0.0) s.routeOriginLng else draft.originLng
        val destLat = if (s.routeDestLat != 0.0) s.routeDestLat else draft.destLat
        val destLng = if (s.routeDestLng != 0.0) s.routeDestLng else draft.destLng

        val enrichedDraft = draft.copy(
            distanceMetres = distanceMetres,
            originLat = originLat,
            originLng = originLng,
            destLat = destLat,
            destLng = destLng,
        )
        _state.value = _state.value.copy(pendingDraft = enrichedDraft)
        viewModelScope.launch {
            _effect.send(EditTripEffect.NavigateToPriceReview)
        }
    }

    private fun confirmAndUpdate() {
        val draft = _state.value.pendingDraft ?: return
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val request = UpdateTripRequest(
                originName = draft.originName.takeIf { it.isNotBlank() },
                originLat = draft.originLat.takeIf { it != 0.0 },
                originLng = draft.originLng.takeIf { it != 0.0 },
                destName = draft.destName.takeIf { it.isNotBlank() },
                destLat = draft.destLat.takeIf { it != 0.0 },
                destLng = draft.destLng.takeIf { it != 0.0 },
                departsAt = draft.departsAt.takeIf { it.isNotBlank() },
                distanceMetres = draft.distanceMetres.takeIf { it > 0 },
            )
            when (val result = tripRepository.updateTrip(draft.tripId, request)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isSaving = false, pendingDraft = null)
                    _effect.send(EditTripEffect.ShowSnackbar("Trip updated."))
                    _effect.send(EditTripEffect.NavigateToMyTrips)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isSaving = false)
                    val msg = if (result.errorCode == "TRIP_HAS_BOOKINGS") {
                        "This trip has passengers booked and can no longer be edited."
                    } else {
                        "Could not update trip. Please try again."
                    }
                    _effect.send(EditTripEffect.ShowSnackbar(msg))
                }
            }
        }
    }
}
