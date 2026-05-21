package com.example.hop.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.DemandHotspot
import com.example.hop.domain.model.EarningsPoint
import com.example.hop.domain.repository.AggregatesRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class DriverAggregatesUiState(
    val isLoading: Boolean = false,
    val earningsSeries: List<EarningsPoint> = emptyList(),
    val demandHotspots: List<DemandHotspot> = emptyList(),
    val co2SavedKg: Int = 0,
    val error: String? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface DriverAggregatesEvent {
    data object Load : DriverAggregatesEvent
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

/**
 * Aggregates the driver-home read-side data: earnings series, demand
 * hotspots, CO₂ savings. Fans out to three endpoints in parallel.
 */
class DriverAggregatesViewModel(
    private val aggregatesRepository: AggregatesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DriverAggregatesUiState())
    val state: StateFlow<DriverAggregatesUiState> = _state.asStateFlow()

    fun onEvent(event: DriverAggregatesEvent) {
        when (event) {
            is DriverAggregatesEvent.Load -> load()
        }
    }

    private fun load() {
        // Idempotency guard — prevents double-load on rapid recompose.
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val earningsDeferred = async { aggregatesRepository.getDriverEarningsSeries(days = 30) }
            val hotspotsDeferred = async { aggregatesRepository.getDemandHotspots() }
            val co2Deferred = async { aggregatesRepository.getUserCo2Saved() }

            val earningsResp = earningsDeferred.await()
            val hotspotsResp = hotspotsDeferred.await()
            val co2Resp = co2Deferred.await()

            val series = (earningsResp as? ApiResponse.Success)?.data ?: emptyList()
            val hotspots = (hotspotsResp as? ApiResponse.Success)?.data ?: emptyList()
            val co2 = (co2Resp as? ApiResponse.Success)?.data ?: 0
            val firstError = listOfNotNull(
                earningsResp as? ApiResponse.Error,
                hotspotsResp as? ApiResponse.Error,
                co2Resp as? ApiResponse.Error,
            ).firstOrNull()

            _state.value = _state.value.copy(
                isLoading = false,
                earningsSeries = series,
                demandHotspots = hotspots,
                co2SavedKg = co2,
                error = firstError?.message,
            )
        }
    }
}
