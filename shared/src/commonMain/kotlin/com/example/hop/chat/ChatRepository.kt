package com.example.hop.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    val messages: Flow<Message>
    val connectionState: StateFlow<ConnectionState>

    fun connect(bookingId: String, token: String)
    fun disconnect()
    fun sendMessage(body: String)
}
