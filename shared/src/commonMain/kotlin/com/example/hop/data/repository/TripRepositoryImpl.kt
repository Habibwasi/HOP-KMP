package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.PassengerSummaryDto
import com.example.hop.data.dto.PostTripRequestDto
import com.example.hop.data.dto.TripDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.PassengerSummary
import com.example.hop.domain.model.Trip
import com.example.hop.domain.repository.PostTripRequest
import com.example.hop.domain.repository.TripRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class TripRepositoryImpl(
    private val httpClient: HttpClient,
) : TripRepository {

    override suspend fun searchTrips(
        origin: String,
        dest: String,
        date: String,
        seats: Int,
    ): ApiResponse<List<Trip>> {
        val response = safeEnvelopeCall<List<TripDto>> {
            httpClient.get("trips/search") {
                url.parameters.append("origin", origin)
                url.parameters.append("dest", dest)
                url.parameters.append("date", date)
                url.parameters.append("seats", seats.toString())
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> {
                ApiResponse.Success(response.data.map { it.toDomain() })
            }
            is ApiResponse.Error -> response
        }
    }

    override suspend fun getTripById(id: String): ApiResponse<Trip> {
        val response = safeEnvelopeCall<TripDto> {
            httpClient.get("trips/$id").body()
        }
        return when (response) {
            is ApiResponse.Success -> {
                ApiResponse.Success(response.data.toDomain())
            }
            is ApiResponse.Error -> response
        }
    }

    override suspend fun getMyTripsAsPassenger(): ApiResponse<List<Trip>> {
        val response = safeEnvelopeCall<List<TripDto>> {
            httpClient.get("trips/me/passenger").body()
        }
        return when (response) {
            is ApiResponse.Success -> {
                ApiResponse.Success(response.data.map { it.toDomain() })
            }
            is ApiResponse.Error -> response
        }
    }

    override suspend fun getMyTripsAsDriver(): ApiResponse<List<Trip>> {
        val response = safeEnvelopeCall<List<TripDto>> {
            httpClient.get("trips/me/driver").body()
        }
        return when (response) {
            is ApiResponse.Success -> {
                ApiResponse.Success(response.data.map { it.toDomain() })
            }
            is ApiResponse.Error -> response
        }
    }

    override suspend fun postTrip(request: PostTripRequest): ApiResponse<Trip> {
        val response = safeEnvelopeCall<TripDto> {
            httpClient.post("trips") {
                setBody(
                    PostTripRequestDto(
                        model = request.model,
                        originName = request.originName,
                        originLat = request.originLat,
                        originLng = request.originLng,
                        destName = request.destName,
                        destLat = request.destLat,
                        destLng = request.destLng,
                        distanceMetres = request.distanceMetres,
                        departsAt = request.departsAt,
                        seatsTotal = request.seatsTotal,
                        minThreshold = request.minThreshold,
                        recurrenceDays = request.recurrenceDays,
                    )
                )
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> {
                ApiResponse.Success(response.data.toDomain())
            }
            is ApiResponse.Error -> response
        }
    }

    override suspend fun completeTrip(tripId: String): ApiResponse<Unit> = safeApiCall {
        httpClient.post("trips/$tripId/complete")
        Unit
    }

    // DELETE /trips/:id per Agent.md API spec. Cancels the trip.
    override suspend fun cancelTrip(tripId: String): ApiResponse<Unit> = safeApiCall {
        httpClient.delete("trips/$tripId")
        Unit
    }

    override suspend fun getTripPassengers(tripId: String): ApiResponse<List<PassengerSummary>> {
        val response = safeEnvelopeCall<List<PassengerSummaryDto>> {
            httpClient.get("trips/$tripId/bookings").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.map { it.toDomain() })
            is ApiResponse.Error -> response
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun <T> safeEnvelopeCall(
        block: suspend () -> ApiEnvelope<T>,
    ): ApiResponse<T> {
        return try {
            val envelope = block()
            val error = envelope.error
            if (error != null) return ApiResponse.Error(error.code, error.message)
            ApiResponse.Success(checkNotNull(envelope.data) { "Null data in API envelope" })
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }
}
