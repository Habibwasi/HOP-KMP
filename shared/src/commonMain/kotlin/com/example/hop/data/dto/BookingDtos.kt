package com.example.hop.data.dto

import com.example.hop.domain.model.Booking
import com.example.hop.domain.model.BookingStatus
import kotlinx.serialization.Serializable

// ── Request bodies ────────────────────────────────────────────────────────────

@Serializable
data class CreateBookingRequest(
    val tripId: String,
    val seats: Int,
)

@Serializable
data class RateBookingRequest(
    val stars: Int,
    val comment: String? = null,
)

// ── Response bodies ───────────────────────────────────────────────────────────

@Serializable
data class BookingDto(
    val id: String,
    val tripId: String,
    val passengerId: String,
    val seats: Int,
    val status: String,
    val paymentId: String? = null,
)

// ── Mapping ───────────────────────────────────────────────────────────────────

fun BookingDto.toDomain(): Booking = Booking(
    id = id,
    tripId = tripId,
    passengerId = passengerId,
    seats = seats,
    status = BookingStatus.entries.firstOrNull { it.name == status } ?: BookingStatus.UNKNOWN,
    paymentId = paymentId,
)
