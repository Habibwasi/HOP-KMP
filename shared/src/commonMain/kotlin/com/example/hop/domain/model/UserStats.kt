package com.example.hop.domain.model

/**
 * Aggregated trust signals for the current user, surfaced on the home
 * `TrustStatsCard`. `averageRating` is null when the user has zero ratings.
 */
data class UserStats(
    val averageRating: Double?,
    val totalRatings: Int,
    val completedTrips: Int,
)
