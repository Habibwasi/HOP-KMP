package com.example.hop.presentation.helpcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.FaqItem
import com.example.hop.domain.repository.HelpCenterRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ Model ──────────────────────────────────────────────────────────────────────

enum class HelpCenterTab { Passenger, Driver }

// ─ State ──────────────────────────────────────────────────────────────────────

data class HelpCenterUiState(
    val allFaqs: List<FaqItem> = emptyList(),
    val searchQuery: String = "",
    val expandedFaqId: String? = null,
    val activeTab: HelpCenterTab = HelpCenterTab.Passenger,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val filteredFaqs: List<FaqItem>
        get() {
            val q = searchQuery.trim().lowercase()
            return if (q.isEmpty()) allFaqs
            else allFaqs.filter { faq ->
                faq.question.lowercase().contains(q) ||
                    faq.answer.lowercase().contains(q) ||
                    faq.topic.lowercase().contains(q)
            }
        }

    /** FAQ items grouped by topic, in insertion order. */
    val faqsByTopic: Map<String, List<FaqItem>>
        get() = filteredFaqs.groupBy { it.topic }
}

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface HelpCenterEvent {
    data object NavigateBack : HelpCenterEvent
    data class SearchQueryChanged(val query: String) : HelpCenterEvent
    data class FaqToggled(val id: String) : HelpCenterEvent
    data class TabSelected(val tab: HelpCenterTab) : HelpCenterEvent
    data object ReportProblemTapped : HelpCenterEvent
    data object ContactUsTapped : HelpCenterEvent
    data object RetryLoad : HelpCenterEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface HelpCenterEffect {
    data object NavigateBack : HelpCenterEffect
    data class OpenUri(val uri: String) : HelpCenterEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class HelpCenterViewModel(
    private val repository: HelpCenterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HelpCenterUiState())
    val state: StateFlow<HelpCenterUiState> = _state.asStateFlow()

    private val _effect = Channel<HelpCenterEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadFaqs()
    }

    fun onEvent(event: HelpCenterEvent) {
        when (event) {
            is HelpCenterEvent.NavigateBack -> viewModelScope.launch {
                _effect.send(HelpCenterEffect.NavigateBack)
            }
            is HelpCenterEvent.SearchQueryChanged -> _state.value =
                _state.value.copy(searchQuery = event.query, expandedFaqId = null)
            is HelpCenterEvent.FaqToggled -> _state.value = _state.value.copy(
                expandedFaqId = if (_state.value.expandedFaqId == event.id) null else event.id,
            )
            is HelpCenterEvent.TabSelected -> _state.value =
                _state.value.copy(activeTab = event.tab)
            is HelpCenterEvent.ReportProblemTapped -> viewModelScope.launch {
                _effect.send(HelpCenterEffect.OpenUri("mailto:support@ridly.dk?subject=Problem%20Report"))
            }
            is HelpCenterEvent.ContactUsTapped -> viewModelScope.launch {
                _effect.send(HelpCenterEffect.OpenUri("mailto:support@ridly.dk"))
            }
            is HelpCenterEvent.RetryLoad -> loadFaqs()
        }
    }

    private fun loadFaqs() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = repository.getFaqs()) {
                is ApiResponse.Success -> _state.value = _state.value.copy(
                    allFaqs = result.data,
                    isLoading = false,
                )
                is ApiResponse.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message,
                )
            }
        }
    }
}
