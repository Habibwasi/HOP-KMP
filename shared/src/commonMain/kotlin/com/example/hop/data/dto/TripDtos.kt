package com.example.hop.data.dto

import com.example.hop.domain.model.BookingStatus
import com.example.hop.domain.model.PassengerSummary
import com.example.hop.domain.model.Trip
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.model.TripStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Request bodies ────────────────────────────────────────────────────────────

@Serializable
data class PostTripRequestDto(
    val model: String,
    val originLat: Double,
    val originLng: Double,
    @SerialName("originAddress") val originName: String,
    val destLat: Double,
    val destLng: Double,
    @SerialName("destAddress") val destName: String,
    val distanceMetres: Int? = null,
    @SerialName("departureAt") val departsAt: String,
    @SerialName("seats") val seatsTotal: Int,
    @SerialName("minPassengers") val minThreshold: Int? = null,
    @SerialName("recurringDays") val recurrenceDays: List<String>? = null,
    val thresholdDeadline: String? = null,
)

// ── Response bodies ───────────────────────────────────────────────────────────

/** Minimal booking stub included by some trip endpoints to compute seatsBooked. */
@Serializable
data class BookingRefDto(
    val id: String? = null,
    val seats: Int = 1,
    val status: String = "CONFIRMED",
)

@Serializable
data class TripDto(
    val id: String,
    val driverId: String,
    val model: String,
    @SerialName("originAddress") val originName: String,
    val originLat: Double,
    val originLng: Double,
    @SerialName("destAddress") val destName: String,
    val destLat: Double,
    val destLng: Double,
    /** Backend stores distance as km (Float?); converted to metres in toDomain(). */
    val distanceKm: Double? = null,
    @SerialName("departureAt") val departsAt: String,
    @SerialName("seats") val seatsTotal: Int,
    /** Included on /trips/me/driver and search endpoints; absent on POST response. */
    val bookings: List<BookingRefDto>? = null,
    @SerialName("minPassengers") val minThreshold: Int? = null,
    /** Backend stores price per seat in øre (passenger price). */
    @SerialName("pricePerSeat") val priceOerePerSeat: Int,
    val status: String,
    @SerialName("recurringDays") val recurrenceDays: List<String>? = null,
    // Populated by /trips/me/passenger — absent on driver/search endpoints.
    val bookingId: String? = null,
    // Populated by /trips/me/passenger — the booking status for this passenger's booking.
    val bookingStatus: String? = null,
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
    distanceMetres = ((distanceKm ?: 0.0) * 1000).toInt(),
    departsAt = departsAt,
    seatsTotal = seatsTotal,
    seatsBooked = bookings?.sumOf { it.seats } ?: 0,
    minThreshold = minThreshold,
    priceOerePerSeat = priceOerePerSeat,
    // No platform fee — driverNet equals passenger price (P2P MobilePay model).
    driverNetOere = priceOerePerSeat,
    status = TripStatus.entries.firstOrNull { it.name == status } ?: TripStatus.UNKNOWN,
    recurrenceDays = recurrenceDays,
    bookingId = bookingId,
    bookingStatus = bookingStatus?.let { s -> BookingStatus.entries.firstOrNull { it.name == s } },
    awaitingPaymentBookingId = bookings?.firstOrNull { it.status == "AWAITING_PAYMENT" }?.id,
)

fun PassengerSummaryDto.toDomain(): PassengerSummary = PassengerSummary(
    bookingId = bookingId,
    passengerId = passengerId,
    fullName = fullName,
    rating = rating,
    seats = seats,
)
