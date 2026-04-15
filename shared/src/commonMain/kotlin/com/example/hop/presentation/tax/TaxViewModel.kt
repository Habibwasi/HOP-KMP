package com.example.hop.presentation.tax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.repository.TaxRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class TaxUiState(
    val isLoading: Boolean = false,
    val year: Int = 0,
    val totalEarningsOere: Int = 0,
    val totalDeductionOere: Int = 0,
    val totalTaxableOere: Int = 0,
    val reportUrl: String? = null,
    val error: String? = null,
) {
    val isReady: Boolean get() = !isLoading && reportUrl != null && error == null
}

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface TaxEvent {
    data class LoadTaxReport(val year: Int) : TaxEvent
    data object Retry : TaxEvent
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class TaxViewModel(
    private val taxRepository: TaxRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TaxUiState())
    val state: StateFlow<TaxUiState> = _state.asStateFlow()

    private var lastYear: Int = 0

    fun onEvent(event: TaxEvent) {
        when (event) {
            is TaxEvent.LoadTaxReport -> loadTaxReport(event.year)
            is TaxEvent.Retry -> loadTaxReport(lastYear)
        }
    }

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
}
