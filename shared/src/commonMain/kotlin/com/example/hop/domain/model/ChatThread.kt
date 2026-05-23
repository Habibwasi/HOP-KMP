package com.example.hop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatThread(
    val bookingId: String,
    val bookingStatus: String,    // matches BookingStatus enum names
    val tripOrigin: String,
    val tripDest: String,
    val departureAt: String,      // ISO-8601 UTC
    val otherPartyId: String = "",
    val otherPartyName: String,
    val otherPartyAvatarUrl: String?,
    val myRole: String,           // "PASSENGER" | "DRIVER"
)
