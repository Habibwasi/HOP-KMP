package com.example.hop.domain.repository

import com.example.hop.network.ApiResponse

data class RouteInfo(
    val distanceMetres: Int,
    val originLat: Double,
    val originLng: Double,
    val destLat: Double,
    val destLng: Double,
)

interface RoutingRepository {
    /**
     * Returns the driving distance in metres between two free-text addresses.
     * Uses the Google Maps Directions API under the hood.
     */
    suspend fun getDistanceMetres(origin: String, destination: String): ApiResponse<Int>

    /**
     * Returns distance + origin/destination lat-lng in a single Directions API call,
     * eliminating the need for separate geocoding requests.
     */
    suspend fun getRouteInfo(origin: String, destination: String): ApiResponse<RouteInfo>
}
