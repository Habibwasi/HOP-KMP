package com.example.hop.domain.model

import kotlinx.serialization.Serializable

/**
 * Lightweight passenger record returned alongside a trip for the driver's
 * TripDetailActiveDriver screen (DR-10).
 *
 * Derived from the backend's `/trips/:id/bookings` endpoint which joins
 * Booking records with the User profile.
 */
@Serializable
data class PassengerSummary(
    val bookingId: String,
    val passengerId: String,
    val fullName: String,
    /** Passenger's current platform rating (1.0–5.0). 0 = no ratings yet. */
    val rating: Float,
    val seats: Int,
    val avatarUrl: String? = null,
) {
    /** Two-letter initials derived from fullName (e.g., "Lars Eriksen" → "LE"). */
    val initials: String
        get() = fullName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
}
