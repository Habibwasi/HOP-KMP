package com.example.hop.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.repository.SearchHistoryRepository
import com.example.hop.domain.repository.SearchAlertsRepository
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

// ─ Filter ─────────────────────────────────────────────────────────────────────

enum class SearchFilter {
    EARLIEST,
    CHEAPEST,
    // TOP_RATED: requires Trip.rating field — no-op until domain model is extended with driver rating
    TOP_RATED,
    DAILY_COMMUTE,   // Model A — recurring commute routes
    LONG_DISTANCE,   // Model B — one-off long-distance trips
}

// ─ State ──────────────────────────────────────────────────────────────────────

data class SearchUiState(
    val isLoading: Boolean = false,
    val results: List<TripUiModel> = emptyList(),
    val error: String? = null,
    val origin: String = "",
    val dest: String = "",
    val date: String = "",
    val seats: Int = 1,
    val activeFilters: Set<SearchFilter> = emptySet(),
    val isCreatingAlert: Boolean = false,
    val alertError: String? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface SearchEvent {
    data class Search(
        val origin: String,
        val dest: String,
        val date: String,
        val seats: Int,
    ) : SearchEvent
    data class ApplyFilter(val filter: SearchFilter) : SearchEvent
    data object ClearFilters : SearchEvent
    data class SelectTrip(val tripId: String) : SearchEvent
    data object AlertMe : SearchEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface SearchEffect {
    data class NavigateToTripDetail(val tripId: String) : SearchEffect
    data object AlertCreated : SearchEffect
    data class AlertError(val message: String) : SearchEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class SearchViewModel(
    private val tripRepository: TripRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val searchAlertsRepository: SearchAlertsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _effect = Channel<SearchEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.Search -> search(event.origin, event.dest, event.date, event.seats)
            is SearchEvent.ApplyFilter -> applyFilter(event.filter)
            is SearchEvent.ClearFilters -> _state.value = _state.value.copy(activeFilters = emptySet())
            is SearchEvent.SelectTrip -> viewModelScope.launch {
                _effect.send(SearchEffect.NavigateToTripDetail(event.tripId))
            }
            is SearchEvent.AlertMe -> createAlert()
        }
    }

    private fun search(origin: String, dest: String, date: String, seats: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                origin = origin,
                dest = dest,
                date = date,
                seats = seats,
            )
            when (val response = tripRepository.searchTrips(origin, dest, date, seats)) {
                is ApiResponse.Success -> {
                    val uiModels = response.data.toUiModels()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        results = uiModels.applyFilters(_state.value.activeFilters),
                    )
                    // Record this query for the home-screen "Recent" row.
                    // Fire-and-forget — never block the UI on history side-effects.
                    if (origin.isNotBlank() && dest.isNotBlank()) {
                        viewModelScope.launch {
                            searchHistoryRepository.record(origin.trim(), dest.trim())
                        }
                    }
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

    private fun createAlert() {
        val origin = _state.value.origin
        val dest = _state.value.dest
        if (origin.isBlank() || dest.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreatingAlert = true, alertError = null)
            when (val result = searchAlertsRepository.create(origin, dest, _state.value.seats)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isCreatingAlert = false)
                    _effect.send(SearchEffect.AlertCreated)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isCreatingAlert = false, alertError = result.message)
                    _effect.send(SearchEffect.AlertError(result.message))
                }
            }
        }
    }

    private fun applyFilter(filter: SearchFilter) {
        val current = _state.value.activeFilters
        val updated = if (filter in current) current - filter else current + filter
        _state.value = _state.value.copy(
            activeFilters = updated,
            results = _state.value.results.applyFilters(updated),
        )
    }

    private fun List<TripUiModel>.applyFilters(filters: Set<SearchFilter>): List<TripUiModel> {
        if (filters.isEmpty()) return this
        var filtered = this
        if (SearchFilter.DAILY_COMMUTE in filters) filtered = filtered.filter { it.model == TripModel.A }
        if (SearchFilter.LONG_DISTANCE in filters) filtered = filtered.filter { it.model == TripModel.B }
        val sortFilters = filters.intersect(setOf(SearchFilter.EARLIEST, SearchFilter.CHEAPEST, SearchFilter.TOP_RATED))
        if (sortFilters.isNotEmpty()) {
            filtered = when (sortFilters.first()) {
                SearchFilter.EARLIEST -> filtered.sortedBy { it.departsAt }
                SearchFilter.CHEAPEST -> filtered.sortedBy { it.priceOerePerSeat }
                // TOP_RATED: no-op — Trip.rating not yet in domain model
                else -> filtered
            }
        }
        return filtered
    }
}
