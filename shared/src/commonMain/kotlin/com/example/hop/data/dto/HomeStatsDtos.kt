package com.example.hop.data.dto

import com.example.hop.domain.model.ActiveBooking
import com.example.hop.domain.model.UserStats
import kotlinx.serialization.Serializable

@Serializable
data class UserStatsDto(
    val averageRating: Double? = null,
    val totalRatings: Int = 0,
    val completedTrips: Int = 0,
)

@Serializable
data class UnreadCountDto(val count: Int = 0)

fun UserStatsDto.toDomain(): UserStats = UserStats(
    averageRating = averageRating,
    totalRatings = totalRatings,
    completedTrips = completedTrips,
)

// ── Active booking ───────────────────────────────────────────────────────────
// Mirrors the shape returned by `GET /bookings/me/active`, which embeds the
// trip and driver. We only deserialise the fields the home banner needs.

@Serializable
data class ActiveBookingDto(
    val id: String = "",
    val tripId: String = "",
    val status: String = "",
    val trip: ActiveBookingTripDto? = null,
)

@Serializable
data class ActiveBookingTripDto(
    val originAddress: String = "",
    val destAddress: String = "",
    val departureAt: String = "",
)

fun ActiveBookingDto.toDomain(): ActiveBooking = ActiveBooking(
    id = id,
    tripId = tripId,
    originName = trip?.originAddress.orEmpty(),
    destName = trip?.destAddress.orEmpty(),
    departsAt = trip?.departureAt.orEmpty(),
    status = status,
)
