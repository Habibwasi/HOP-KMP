package com.example.hop.domain.repository

import com.example.hop.domain.model.Trip
import com.example.hop.network.ApiResponse

interface TripRepository {
    suspend fun searchTrips(
        origin: String,
        dest: String,
        date: String,
        seats: Int,
    ): ApiResponse<List<Trip>>

    suspend fun getTripById(id: String): ApiResponse<Trip>

    suspend fun getMyTripsAsPassenger(): ApiResponse<List<Trip>>

    suspend fun getMyTripsAsDriver(): ApiResponse<List<Trip>>

    suspend fun postTrip(request: PostTripRequest): ApiResponse<Trip>

    suspend fun completeTrip(tripId: String): ApiResponse<Unit>

    suspend fun cancelTrip(tripId: String): ApiResponse<Unit>
}

data class PostTripRequest(
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
