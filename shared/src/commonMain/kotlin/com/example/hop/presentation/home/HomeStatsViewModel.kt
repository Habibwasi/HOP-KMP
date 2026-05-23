package com.example.hop.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.ActiveBooking
import com.example.hop.domain.model.RecentSearch
import com.example.hop.domain.model.UserStats
import com.example.hop.domain.repository.HomeStatsRepository
import com.example.hop.domain.repository.SearchHistoryRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class HomeStatsUiState(
    val isLoading: Boolean = false,
    val isMutating: Boolean = false,
    val stats: UserStats? = null,
    val unreadCount: Int = 0,
    val recentSearches: List<RecentSearch> = emptyList(),
    val activeBooking: ActiveBooking? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface HomeStatsEvent {
    /** Fan-out load — triggers all three home aggregates in parallel. */
    data object Load : HomeStatsEvent
    /** Optimistic delete of a recent search entry. Rolls back on API failure. */
    data class DeleteRecentSearch(val id: String) : HomeStatsEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface HomeStatsEffect {
    data class ShowError(val message: String) : HomeStatsEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

/**
 * Combined home-screen aggregator. Keeps one VM instead of three so the
 * Passenger home only depends on a single state subscription.
 */
class HomeStatsViewModel(
    private val homeStatsRepository: HomeStatsRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeStatsUiState())
    val state: StateFlow<HomeStatsUiState> = _state.asStateFlow()

    private val _effect = Channel<HomeStatsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: HomeStatsEvent) {
        when (event) {
            is HomeStatsEvent.Load -> load()
            is HomeStatsEvent.DeleteRecentSearch -> deleteRecentSearch(event.id)
        }
    }

    private fun load() {
        if (_state.value.isLoading) return
        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            val response = homeStatsRepository.getMyStats()
            if (response is ApiResponse.Success) {
                _state.value = _state.value.copy(stats = response.data)
            }
        }
        viewModelScope.launch {
            val response = homeStatsRepository.getUnreadNotificationsCount()
            if (response is ApiResponse.Success) {
                _state.value = _state.value.copy(unreadCount = response.data)
            }
        }
        viewModelScope.launch {
            // Authoritative "next active trip" — sourced from the server so the
            // banner stays accurate after refunds, cancellations or device sync.
            val response = homeStatsRepository.getActiveBooking()
            if (response is ApiResponse.Success) {
                _state.value = _state.value.copy(activeBooking = response.data)
            }
        }
        viewModelScope.launch {
            val response = searchHistoryRepository.list(limit = 5)
            if (response is ApiResponse.Success) {
                _state.value = _state.value.copy(recentSearches = response.data)
            }
            // Always clear loading after the last (slowest) call.
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private fun deleteRecentSearch(id: String) {
        if (_state.value.isMutating) return
        // Snapshot for rollback if the API rejects the delete.
        val previous = _state.value.recentSearches
        _state.value = _state.value.copy(
            isMutating = true,
            recentSearches = previous.filterNot { it.id == id },
        )
        viewModelScope.launch {
            when (val response = searchHistoryRepository.delete(id)) {
                is ApiResponse.Success -> _state.value = _state.value.copy(isMutating = false)
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isMutating = false, recentSearches = previous)
                    _effect.trySend(HomeStatsEffect.ShowError(response.message))
                }
            }
        }
    }
}
