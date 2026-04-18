package com.example.hop.domain.model

data class HopNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    /** ISO-8601 UTC timestamp, e.g. "2026-04-18T10:30:00Z". */
    val createdAt: String,
    val isRead: Boolean,
    /** Optional deep-link ID: tripId, bookingId, etc. */
    val deepLinkId: String? = null,
)

enum class NotificationType {
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    TRIP_REMINDER,
    NEW_RATING,
    THRESHOLD_MET,
    CHAT_MESSAGE,
    GENERAL,
}
