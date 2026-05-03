package com.example.hop.domain.repository

import com.example.hop.domain.model.PassengerSummary
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

    /** Returns booked passenger summaries for a driver's active trip. */
    suspend fun getTripPassengers(tripId: String): ApiResponse<List<PassengerSummary>>
}

data class PostTripRequest(
    val model: String,
    val originName: String,
    val originLat: Double,
    val originLng: Double,
    val destName: String,
    val destLat: Double,
    val destLng: Double,
    val departsAt: String,
    val seatsTotal: Int,
    val minThreshold: Int? = null,
    val thresholdDeadline: String? = null,
    val recurrenceDays: List<String>? = null,
)
