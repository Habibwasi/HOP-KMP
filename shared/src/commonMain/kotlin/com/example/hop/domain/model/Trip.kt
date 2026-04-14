package com.example.hop.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TripModel {
    A,
    B,
    UNKNOWN, // Fallback for version mismatch or API drift
}

@Serializable
enum class TripStatus {
    ACTIVE,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    UNKNOWN, // Fallback for version mismatch or API drift
}

@Serializable
data class Trip(
    val id: String,
    val driverId: String,
    val model: TripModel,
    val originName: String,
    val originLat: Double,
    val originLng: Double,
    val destName: String,
    val destLat: Double,
    val destLng: Double,
    val distanceMetres: Int,
    val departsAt: String,
    val seatsTotal: Int,
    val seatsBooked: Int,
    val minThreshold: Int?,
    val priceOerePerSeat: Int,
    val driverNetOere: Int,
    val status: TripStatus,
    val recurrenceDays: List<String>?,
)

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Returns true if the trip state is unknown (version mismatch, API drift, or corruption).
 * UI-layer should render as disabled/greyed-out, not filtered.
 * ViewModels should mark such trips and soft-log for observability.
 */
fun Trip.isBroken(): Boolean = status == TripStatus.UNKNOWN || model == TripModel.UNKNOWN
