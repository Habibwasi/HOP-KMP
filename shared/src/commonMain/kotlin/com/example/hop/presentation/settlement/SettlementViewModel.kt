package com.example.hop.presentation.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.RideSettlement
import com.example.hop.domain.model.TripSettlementEntry
import com.example.hop.domain.repository.SettlementRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class SettlementUiState(
    val isLoading: Boolean = false,
    val settlement: RideSettlement? = null,
    /** All passengers for the trip — populated by LoadForTrip (driver view). */
    val entries: List<TripSettlementEntry> = emptyList(),
    val isMarkingPaid: Boolean = false,
    val isUnmarkingPaid: Boolean = false,
    val isConfirming: Boolean = false,
    /** bookingId currently being confirmed (per-entry driver flow). */
    val confirmingBookingId: String? = null,
    val isDisputing: Boolean = false,
    val disputeReason: String = "",
    val error: String? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface SettlementEvent {
    data class Load(val bookingId: String) : SettlementEvent
    /** Driver view: loads all passenger settlements for the whole trip. */
    data class LoadForTrip(val tripId: String) : SettlementEvent
    /** Notification deep-link: resolve tripId from bookingId then load for trip. */
    data class LoadForTripByBooking(val bookingId: String) : SettlementEvent
    /** Silent background re-fetch (e.g. on resume) — does not show loading indicator. */
    data object Refresh : SettlementEvent
    /** Passenger taps "Open MobilePay" — triggers deeplink effect, no backend call. */
    data object OpenMobilepay : SettlementEvent
    /** Passenger taps "I have paid". */
    data object MarkPaid : SettlementEvent
    /** Passenger taps "I haven't paid yet" — undoes their mark-paid. */
    data object UnmarkPaid : SettlementEvent
    /** Driver taps "Confirm received" on a specific booking in the trip-level list. */
    data class ConfirmReceivedForBooking(val bookingId: String) : SettlementEvent
    /** Driver taps "Confirm received" (legacy single-booking flow). */
    data object ConfirmReceived : SettlementEvent
    /** Dispute reason text changed in UI. */
    data class DisputeReasonChanged(val reason: String) : SettlementEvent
    /** Driver/passenger taps "Submit dispute". */
    data object SubmitDispute : SettlementEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface SettlementEffect {
    /** Open `mobilepay://send?phone=<number>&amount=<DKK>&comment=Ridly%20<bookingId>` */
    data class OpenMobilepayDeeplink(val uri: String) : SettlementEffect
    data object PaymentMarkedSuccess : SettlementEffect
    data object UnmarkPaidSuccess : SettlementEffect
    data object ConfirmReceivedSuccess : SettlementEffect
    data object DisputeSubmittedSuccess : SettlementEffect
    data class ShowSnackbar(val message: String) : SettlementEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class SettlementViewModel(
    private val settlementRepository: SettlementRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettlementUiState())
    val state: StateFlow<SettlementUiState> = _state.asStateFlow()

    private val _effect = Channel<SettlementEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var currentBookingId: String = ""
    private var currentTripId: String = ""
    /** Set when the passenger opens MobilePay; suppresses the very next Refresh from
     *  closing the screen (ON_RESUME fires immediately when returning from external app). */
    private var suppressNextConfirmEffect = false

    fun onEvent(event: SettlementEvent) {
        when (event) {
            is SettlementEvent.Load -> load(event.bookingId)
            is SettlementEvent.LoadForTrip -> loadForTrip(event.tripId)
            is SettlementEvent.LoadForTripByBooking -> loadForTripByBooking(event.bookingId)
            is SettlementEvent.Refresh -> refresh()
            is SettlementEvent.OpenMobilepay -> openMobilepay()
            is SettlementEvent.MarkPaid -> markPaid()
            is SettlementEvent.UnmarkPaid -> unmarkPaid()
            is SettlementEvent.ConfirmReceivedForBooking -> confirmReceivedForBooking(event.bookingId)
            is SettlementEvent.ConfirmReceived -> confirmReceived()
            is SettlementEvent.DisputeReasonChanged -> _state.value = _state.value.copy(disputeReason = event.reason)
            is SettlementEvent.SubmitDispute -> submitDispute()
        }
    }

    private fun load(bookingId: String) {
        if (_state.value.isLoading) return
        currentBookingId = bookingId
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = settlementRepository.getSettlement(bookingId)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isLoading = false, settlement = response.data)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = response.message)
                    _effect.send(SettlementEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    /** Resolve tripId from a bookingId, then load all passengers for the trip.
     *  NOTE: does NOT delegate to loadForTrip() to avoid the isLoading guard
     *  blocking the inner fetch while the outer fetch is still running. */
    private fun loadForTripByBooking(bookingId: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val settlementResponse = settlementRepository.getSettlement(bookingId)) {
                is ApiResponse.Success -> {
                    val tripId = settlementResponse.data.tripId
                    if (tripId != null) {
                        currentTripId = tripId
                        when (val tripResponse = settlementRepository.getSettlementsForTrip(tripId)) {
                            is ApiResponse.Success -> {
                                _state.value = _state.value.copy(isLoading = false, entries = tripResponse.data)
                            }
                            is ApiResponse.Error -> {
                                _state.value = _state.value.copy(isLoading = false, error = tripResponse.message)
                                _effect.send(SettlementEffect.ShowSnackbar(tripResponse.message))
                            }
                        }
                    } else {
                        _state.value = _state.value.copy(isLoading = false, error = "Trip not found")
                    }
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = settlementResponse.message)
                    _effect.send(SettlementEffect.ShowSnackbar(settlementResponse.message))
                }
            }
        }
    }

    private fun loadForTrip(tripId: String) {
        if (_state.value.isLoading) return
        currentTripId = tripId
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = settlementRepository.getSettlementsForTrip(tripId)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isLoading = false, entries = response.data)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = response.message)
                    _effect.send(SettlementEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    /** Background refresh — does not toggle isLoading so the UI doesn't flash. */
    private fun refresh() {
        val bookingId = currentBookingId
        val tripId = currentTripId
        viewModelScope.launch {
            if (bookingId.isNotEmpty()) {
                when (val response = settlementRepository.getSettlement(bookingId)) {
                    is ApiResponse.Success -> {
                        val prev = _state.value.settlement
                        val updated = response.data
                        _state.value = _state.value.copy(settlement = updated)
                        if (prev?.driverConfirmedAt == null && updated.driverConfirmedAt != null) {
                            if (suppressNextConfirmEffect) {
                                suppressNextConfirmEffect = false
                            } else {
                                _effect.send(SettlementEffect.ConfirmReceivedSuccess)
                            }
                        } else {
                            suppressNextConfirmEffect = false
                        }
                    }
                    is ApiResponse.Error -> { /* silently ignore */ }
                }
            } else if (tripId.isNotEmpty()) {
                when (val response = settlementRepository.getSettlementsForTrip(tripId)) {
                    is ApiResponse.Success -> {
                        // Only navigate away when transitioning from at-least-one-pending
                        // to all-confirmed. Avoids repeated navigation on every refresh
                        // when the trip was already fully settled on screen open.
                        val prevHadPending = _state.value.entries.any { it.driverConfirmedAt == null }
                        _state.value = _state.value.copy(entries = response.data)
                        if (prevHadPending && response.data.isNotEmpty() && response.data.all { it.driverConfirmedAt != null }) {
                            _effect.send(SettlementEffect.ConfirmReceivedSuccess)
                        }
                    }
                    is ApiResponse.Error -> { /* silently ignore */ }
                }
            }
        }
    }

    private fun openMobilepay() {
        _state.value.settlement ?: return
        // Just open the MobilePay app home screen. The UI shows the phone number
        // with a copy button so the passenger can paste it inside the app.
        // Personal-number deep-link schemes (mobilepay://send?phone=…) are not
        // supported by the MobilePay ePayment SDK for P2P transfers.
        suppressNextConfirmEffect = true
        viewModelScope.launch { _effect.send(SettlementEffect.OpenMobilepayDeeplink("mobilepay://")) }
    }

    private fun markPaid() {
        if (_state.value.isMarkingPaid) return
        val bookingId = currentBookingId.ifEmpty { return }
        viewModelScope.launch {
            _state.value = _state.value.copy(isMarkingPaid = true)
            when (val response = settlementRepository.markPaid(bookingId)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isMarkingPaid = false, settlement = response.data)
                    _effect.send(SettlementEffect.PaymentMarkedSuccess)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isMarkingPaid = false)
                    _effect.send(SettlementEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun unmarkPaid() {
        if (_state.value.isUnmarkingPaid) return
        val bookingId = currentBookingId.ifEmpty { return }
        viewModelScope.launch {
            _state.value = _state.value.copy(isUnmarkingPaid = true)
            when (val response = settlementRepository.unmarkPaid(bookingId)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isUnmarkingPaid = false, settlement = response.data)
                    _effect.send(SettlementEffect.UnmarkPaidSuccess)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isUnmarkingPaid = false)
                    _effect.send(SettlementEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun confirmReceivedForBooking(bookingId: String) {
        if (_state.value.confirmingBookingId != null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(confirmingBookingId = bookingId)
            when (val response = settlementRepository.confirmReceived(bookingId)) {
                is ApiResponse.Success -> {
                    // Refresh the full entries list
                    val tripId = currentTripId
                    if (tripId.isNotEmpty()) {
                        when (val refreshed = settlementRepository.getSettlementsForTrip(tripId)) {
                            is ApiResponse.Success -> {
                                val updatedEntries = refreshed.data
                                _state.value = _state.value.copy(
                                    confirmingBookingId = null,
                                    entries = updatedEntries,
                                )
                                if (updatedEntries.isNotEmpty() && updatedEntries.all { it.driverConfirmedAt != null }) {
                                    _effect.send(SettlementEffect.ConfirmReceivedSuccess)
                                }
                            }
                            is ApiResponse.Error -> {
                                _state.value = _state.value.copy(confirmingBookingId = null)
                            }
                        }
                    } else {
                        _state.value = _state.value.copy(confirmingBookingId = null)
                        _effect.send(SettlementEffect.ConfirmReceivedSuccess)
                    }
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(confirmingBookingId = null)
                    _effect.send(SettlementEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun confirmReceived() {
        if (_state.value.isConfirming) return
        val bookingId = currentBookingId.ifEmpty { return }
        viewModelScope.launch {
            _state.value = _state.value.copy(isConfirming = true)
            when (val response = settlementRepository.confirmReceived(bookingId)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isConfirming = false, settlement = response.data)
                    _effect.send(SettlementEffect.ConfirmReceivedSuccess)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isConfirming = false)
                    _effect.send(SettlementEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun submitDispute() {
        if (_state.value.isDisputing) return
        val bookingId = currentBookingId.ifEmpty { return }
        val reason = _state.value.disputeReason.trim()
        if (reason.length < 10) {
            viewModelScope.launch {
                _effect.send(SettlementEffect.ShowSnackbar("Please provide at least 10 characters for the dispute reason"))
            }
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isDisputing = true)
            when (val response = settlementRepository.dispute(bookingId, reason)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isDisputing = false, settlement = response.data)
                    _effect.send(SettlementEffect.DisputeSubmittedSuccess)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isDisputing = false)
                    _effect.send(SettlementEffect.ShowSnackbar(response.message))
                }
            }
        }
    }
}
