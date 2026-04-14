package com.example.hop.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    UNKNOWN, // Fallback for version mismatch or API drift
}

@Serializable
data class Booking(
    val id: String,
    val tripId: String,
    val passengerId: String,
    val seats: Int,
    val status: BookingStatus,
    val paymentId: String?,
)
