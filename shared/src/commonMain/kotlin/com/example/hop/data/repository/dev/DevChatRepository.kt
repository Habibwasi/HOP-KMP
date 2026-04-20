package com.example.hop.data.repository.dev

import com.example.hop.chat.ChatRepository
import com.example.hop.chat.ConnectionState
import com.example.hop.chat.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

class DevChatRepository : ChatRepository {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    override val messages: Flow<Message> = emptyFlow()

    override fun connect(bookingId: String, token: String) {
        _connectionState.value = ConnectionState.Connected
    }

    override fun disconnect() {
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun sendMessage(body: String) {
        // No-op in dev mode
    }
}
