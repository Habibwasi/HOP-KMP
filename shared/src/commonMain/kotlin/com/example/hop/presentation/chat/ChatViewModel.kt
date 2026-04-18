package com.example.hop.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.chat.ChatRepository
import com.example.hop.chat.ConnectionState
import com.example.hop.chat.Message
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val connectionState: ConnectionState = ConnectionState.Disconnected,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface ChatEvent {
    data class Connect(val bookingId: String, val token: String) : ChatEvent
    data object Disconnect : ChatEvent
    data class InputChanged(val text: String) : ChatEvent
    data object SendMessage : ChatEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface ChatEffect {
    data object ScrollToBottom : ChatEffect
    data class ShowError(val message: String) : ChatEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class ChatViewModel(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private val _effect = Channel<ChatEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        collectMessages()
        collectConnectionState()
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.Connect -> chatRepository.connect(event.bookingId, event.token)
            is ChatEvent.Disconnect -> chatRepository.disconnect()
            is ChatEvent.InputChanged -> _state.update { it.copy(inputText = event.text) }
            is ChatEvent.SendMessage -> sendMessage()
        }
    }

    private fun collectMessages() {
        viewModelScope.launch {
            chatRepository.messages.collect { message ->
                _state.update { it.copy(messages = it.messages + message) }
                _effect.send(ChatEffect.ScrollToBottom)
            }
        }
    }

    private fun collectConnectionState() {
        viewModelScope.launch {
            chatRepository.connectionState.collect { connectionState ->
                _state.update { it.copy(connectionState = connectionState) }
                if (connectionState is ConnectionState.Error) {
                    _effect.send(ChatEffect.ShowError(connectionState.message))
                }
            }
        }
    }

    private fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank()) return
        chatRepository.sendMessage(text)
        _state.update { it.copy(inputText = "") }
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.disconnect()
    }
}
