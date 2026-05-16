package com.example.hop.presentation.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.RideSettlement
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
    val isMarkingPaid: Boolean = false,
    val isConfirming: Boolean = false,
    val isDisputing: Boolean = false,
    val disputeReason: String = "",
    val error: String? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface SettlementEvent {
    data class Load(val bookingId: String) : SettlementEvent
    /** Passenger taps "Open MobilePay" — triggers deeplink effect, no backend call. */
    data object OpenMobilepay : SettlementEvent
    /** Passenger taps "I have paid". */
    data object MarkPaid : SettlementEvent
    /** Driver taps "Confirm received". */
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

    fun onEvent(event: SettlementEvent) {
        when (event) {
            is SettlementEvent.Load -> load(event.bookingId)
            is SettlementEvent.OpenMobilepay -> openMobilepay()
            is SettlementEvent.MarkPaid -> markPaid()
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

    private fun openMobilepay() {
        val s = _state.value.settlement ?: return
        val amountDkk = s.suggestedAmountOere / 100
        val uri = "mobilepay://send?phone=${s.mobilepayNumber}&amount=$amountDkk&comment=Ridly%20${s.bookingId}"
        viewModelScope.launch { _effect.send(SettlementEffect.OpenMobilepayDeeplink(uri)) }
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
