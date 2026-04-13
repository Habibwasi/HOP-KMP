package com.example.hop.domain.model

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
}

data class Booking(
    val id: String,
    val tripId: String,
    val passengerId: String,
    val seats: Int,
    val status: BookingStatus,
    val paymentId: String?,
)
