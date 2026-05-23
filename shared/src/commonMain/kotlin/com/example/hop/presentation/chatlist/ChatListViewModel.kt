package com.example.hop.presentation.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.ChatThread
import com.example.hop.domain.repository.ChatListRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class ChatListUiState(
    val isLoading: Boolean = false,
    val threads: List<ChatThread> = emptyList(),
    val error: String? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface ChatListEvent {
    data object Load : ChatListEvent
    data class OpenChat(val bookingId: String) : ChatListEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface ChatListEffect {
    data class NavigateToChat(val bookingId: String) : ChatListEffect
    data class ShowError(val message: String) : ChatListEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class ChatListViewModel(
    private val chatListRepository: ChatListRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListUiState())
    val state: StateFlow<ChatListUiState> = _state.asStateFlow()

    private val _effect = Channel<ChatListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: ChatListEvent) {
        when (event) {
            is ChatListEvent.Load -> load()
            is ChatListEvent.OpenChat -> viewModelScope.launch {
                _effect.send(ChatListEffect.NavigateToChat(event.bookingId))
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = chatListRepository.getMyChats()) {
                is ApiResponse.Success -> {
                    // Group by otherPartyId so the same person only appears once.
                    // If otherPartyId is missing (legacy), fall back to otherPartyName.
                    // Keep the entry whose departureAt is the most recent in each group.
                    val deduped = result.data
                        .groupBy { t -> t.otherPartyId.ifEmpty { t.otherPartyName } }
                        .values
                        .map { group -> group.maxBy { it.departureAt } }
                        .sortedByDescending { it.departureAt }
                    _state.value = ChatListUiState(threads = deduped)
                }
                is ApiResponse.Error -> {
                    _state.value = ChatListUiState(error = result.message)
                    _effect.send(ChatListEffect.ShowError(result.message))
                }
            }
        }
    }
}
