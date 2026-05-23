package com.example.hop.domain.model

/**
 * Driver-facing summary of the passenger's next confirmed booking. Sourced
 * from `GET /bookings/me/active` so the home screen always reflects the
 * authoritative server view (handles cross-device sync, refunds, etc.).
 */
data class ActiveBooking(
    val id: String,
    val tripId: String,
    val originName: String,
    val destName: String,
    val departsAt: String, // ISO-8601
    val status: String,
)
