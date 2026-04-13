package com.example.hop.domain.model

data class Message(
    val id: String,
    val bookingId: String,
    val senderId: String,
    val body: String,
    val sentAt: String,
    val readAt: String?,
)
