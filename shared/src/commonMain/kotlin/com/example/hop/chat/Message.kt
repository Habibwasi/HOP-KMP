package com.example.hop.chat

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val bookingId: String,
    val senderId: String,
    val senderName: String,
    val body: String,
    val timestampMs: Long,
)
