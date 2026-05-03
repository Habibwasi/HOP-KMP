package com.example.hop.domain.repository

import com.example.hop.network.ApiResponse

interface RoutingRepository {
    /**
     * Returns the driving distance in metres between two free-text addresses.
     * Uses the Google Maps Directions API under the hood.
     */
    suspend fun getDistanceMetres(origin: String, destination: String): ApiResponse<Int>
}
