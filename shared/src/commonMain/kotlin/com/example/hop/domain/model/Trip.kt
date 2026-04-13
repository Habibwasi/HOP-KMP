package com.example.hop.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TripModel {
    A,
    B,
}

@Serializable
enum class TripStatus {
    ACTIVE,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
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
