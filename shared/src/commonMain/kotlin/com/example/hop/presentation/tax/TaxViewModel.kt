package com.example.hop.presentation.tax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.TaxMonthlySummary
import com.example.hop.domain.model.TaxRecord
import com.example.hop.domain.repository.TaxRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class TaxUiState(
    // DR-14: annual report fields
    val isLoading: Boolean = false,       // DR-14 report load guard (getTaxReport)
    val error: String? = null,
    val year: Int = 0,
    val totalEarningsOere: Int = 0,
    val totalDeductionOere: Int = 0,
    val totalTaxableOere: Int = 0,
    val reportUrl: String? = null,
    // DR-13: monthly dashboard fields — separate guard so DR-14 is never blocked
    val isLoadingDashboard: Boolean = false,
    val selectedYear: Int = 0,
    val selectedMonth: Int = 0,
    val summary: TaxMonthlySummary? = null,
    val records: List<TaxRecord> = emptyList(),
    val isLoadingReportUrl: Boolean = false,
) {
    /** DR-14: screen is ready to show the download UI. */
    val isReady: Boolean get() = !isLoading && reportUrl != null && error == null
}

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface TaxEvent {
    // DR-14
    data class LoadTaxReport(val year: Int) : TaxEvent
    data object Retry : TaxEvent
    // DR-13
    data class LoadDashboard(val year: Int, val month: Int) : TaxEvent
    data object PreviousMonth : TaxEvent
    data object NextMonth : TaxEvent
    data class GetReportUrl(val year: Int) : TaxEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface TaxEffect {
    data class OpenReportUrl(val url: String) : TaxEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class TaxViewModel(
    private val taxRepository: TaxRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TaxUiState())
    val state: StateFlow<TaxUiState> = _state.asStateFlow()

    private val _effects = Channel<TaxEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var lastYear: Int = 0

    fun onEvent(event: TaxEvent) {
        when (event) {
            is TaxEvent.LoadTaxReport -> loadTaxReport(event.year)
            is TaxEvent.Retry -> loadTaxReport(lastYear)
            is TaxEvent.LoadDashboard -> loadDashboard(event.year, event.month)
            is TaxEvent.PreviousMonth -> stepMonth(delta = -1)
            is TaxEvent.NextMonth -> stepMonth(delta = +1)
            is TaxEvent.GetReportUrl -> fetchReportUrl(event.year)
        }
    }

    // ── DR-14 ──────────────────────────────────────────────────────────────────

    private fun loadTaxReport(year: Int) {
        if (_state.value.isLoading) return
        lastYear = year
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, year = year)
            when (val response = taxRepository.getTaxReport(year)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        totalEarningsOere = response.data.totalEarningsOere,
                        totalDeductionOere = response.data.totalDeductionOere,
                        totalTaxableOere = response.data.totalTaxableOere,
                        reportUrl = response.data.presignedUrl,
                        error = null,
                    )
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                }
            }
        }
    }

    // ── DR-13 ──────────────────────────────────────────────────────────────────

    private fun loadDashboard(year: Int, month: Int) {
        if (_state.value.isLoadingDashboard) return
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoadingDashboard = true,
                error = null,
                selectedYear = year,
                selectedMonth = month,
            )
            // Fire both API calls in parallel to halve perceived latency
            val summaryDeferred = async { taxRepository.getTaxSummary(year, month) }
            val recordsDeferred = async { taxRepository.getTaxRecords(year) }
            val summaryResponse = summaryDeferred.await()
            val recordsResponse = recordsDeferred.await()

            if (summaryResponse is ApiResponse.Error) {
                _state.value = _state.value.copy(isLoadingDashboard = false, error = summaryResponse.message)
                return@launch
            }
            if (recordsResponse is ApiResponse.Error) {
                _state.value = _state.value.copy(isLoadingDashboard = false, error = recordsResponse.message)
                return@launch
            }

            _state.value = _state.value.copy(
                isLoadingDashboard = false,
                summary = (summaryResponse as ApiResponse.Success).data,
                records = (recordsResponse as ApiResponse.Success).data,
                error = null,
            )
        }
    }

    private fun stepMonth(delta: Int) {
        val current = _state.value
        var m = current.selectedMonth + delta
        var y = current.selectedYear
        if (m < 1) { m = 12; y-- }
        if (m > 12) { m = 1; y++ }
        loadDashboard(y, m)
    }

    private fun fetchReportUrl(year: Int) {
        if (_state.value.isLoadingReportUrl) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingReportUrl = true)
            when (val response = taxRepository.getTaxReportUrl(year)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isLoadingReportUrl = false)
                    _effects.send(TaxEffect.OpenReportUrl(response.data))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoadingReportUrl = false,
                        error = response.message,
                    )
                }
            }
        }
    }
}
