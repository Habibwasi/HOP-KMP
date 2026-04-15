package com.example.hop.data.dto

import com.example.hop.domain.model.PassengerSummary
import com.example.hop.domain.model.Trip
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.model.TripStatus
import kotlinx.serialization.Serializable

// ── Request bodies ────────────────────────────────────────────────────────────

@Serializable
data class PostTripRequestDto(
    val model: String,
    val originName: String,
    val originLat: Double,
    val originLng: Double,
    val destName: String,
    val destLat: Double,
    val destLng: Double,
    val distanceMetres: Int,
    val departsAt: String,
    val seatsTotal: Int,
    val minThreshold: Int? = null,
    val recurrenceDays: List<String>? = null,
)

// ── Response bodies ───────────────────────────────────────────────────────────

@Serializable
data class TripDto(
    val id: String,
    val driverId: String,
    val model: String,
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
    val status: String,
    val recurrenceDays: List<String>?,
    // Populated by /trips/me/passenger — absent on driver/search endpoints.
    val bookingId: String? = null,
)

// Note: Trips endpoints return list directly in ApiEnvelope.data field,
// not wrapped in an object. If API changes, adjust deserialization accordingly.

// ── Mapping ───────────────────────────────────────────────────────────────────

fun TripDto.toDomain(): Trip = Trip(
    id = id,
    driverId = driverId,
    model = TripModel.entries.firstOrNull { it.name == model } ?: TripModel.UNKNOWN,
    originName = originName,
    originLat = originLat,
    originLng = originLng,
    destName = destName,
    destLat = destLat,
    destLng = destLng,
    distanceMetres = distanceMetres,
    departsAt = departsAt,
    seatsTotal = seatsTotal,
    seatsBooked = seatsBooked,
    minThreshold = minThreshold,
    priceOerePerSeat = priceOerePerSeat,
    driverNetOere = driverNetOere,
    status = TripStatus.entries.firstOrNull { it.name == status } ?: TripStatus.UNKNOWN,
    recurrenceDays = recurrenceDays,
    bookingId = bookingId,
)

fun PassengerSummaryDto.toDomain(): PassengerSummary = PassengerSummary(
    bookingId = bookingId,
    passengerId = passengerId,
    fullName = fullName,
    rating = rating,
    seats = seats,
)
