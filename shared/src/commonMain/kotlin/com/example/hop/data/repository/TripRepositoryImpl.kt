package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.PassengerSummaryDto
import com.example.hop.data.dto.PostTripRequestDto
import com.example.hop.data.dto.TripDto
import com.example.hop.data.dto.UpdateTripRequestDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.PassengerSummary
import com.example.hop.domain.model.Trip
import com.example.hop.domain.repository.PostTripRequest
import com.example.hop.domain.repository.TripRepository
import com.example.hop.domain.repository.UpdateTripRequest
import com.example.hop.network.ApiResponse
import com.example.hop.network.safeApiCall
import io.ktor.client.HttpClient
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TripRepositoryImpl(
    private val httpClient: HttpClient,
) : TripRepository {

    override suspend fun searchTrips(
        origin: String,
        dest: String,
        date: String,
        seats: Int,
    ): ApiResponse<List<Trip>> {
        val isoDate = resolveDate(date)
        val response = safeEnvelopeCall<List<TripDto>> {
            httpClient.get("trips/search") {
                url.parameters.append("origin", origin)
                url.parameters.append("dest", dest)
                url.parameters.append("date", isoDate)
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

    /** Converts human-friendly labels to ISO-8601 date strings (yyyy-MM-dd). */
    private fun resolveDate(date: String): String {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        return when (date.lowercase().trim()) {
            "today" -> today.toString()
            "tomorrow" -> today.plus(1, DateTimeUnit.DAY).toString()
            else -> {
                // If already ISO format (yyyy-MM-dd) pass through unchanged.
                if (Regex("""\d{4}-\d{2}-\d{2}""").matches(date.trim())) return date.trim()
                // Attempt to parse display labels like "4 May" / "4 May 2026".
                val monthNames = mapOf(
                    "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
                    "may" to 5, "jun" to 6, "jul" to 7, "aug" to 8,
                    "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12,
                    "january" to 1, "february" to 2, "march" to 3, "april" to 4,
                    "june" to 6, "july" to 7, "august" to 8, "september" to 9,
                    "october" to 10, "november" to 11, "december" to 12,
                )
                val parts = date.trim().split(Regex("""[\s,]+"""))
                val day = parts.getOrNull(0)?.toIntOrNull()
                val month = parts.getOrNull(1)?.lowercase()?.let { monthNames[it] }
                val year = parts.getOrNull(2)?.toIntOrNull() ?: today.year
                if (day != null && month != null) {
                    "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                } else {
                    date // unchanged — let backend report the validation error
                }
            }
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
                        thresholdDeadline = request.thresholdDeadline,
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

    override suspend fun updateTrip(tripId: String, request: UpdateTripRequest): ApiResponse<Trip> {
        val dto = UpdateTripRequestDto(
            originAddress = request.originName,
            originLat = request.originLat,
            originLng = request.originLng,
            destAddress = request.destName,
            destLat = request.destLat,
            destLng = request.destLng,
            departsAt = request.departsAt,
            distanceMetres = request.distanceMetres,
        )
        val response = safeEnvelopeCall<TripDto> {
            httpClient.patch("trips/$tripId") {
                contentType(ContentType.Application.Json)
                setBody(dto)
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.toDomain())
            is ApiResponse.Error -> response
        }
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
            if (error != null) return ApiResponse.Error(error.code, error.message, error.errorCode)
            ApiResponse.Success(checkNotNull(envelope.data) { "Null data in API envelope" })
        } catch (e: HttpRequestTimeoutException) {
            ApiResponse.Error(ApiResponse.CODE_TIMEOUT, "Connection timed out. Please check your network and try again.")
        } catch (e: ServerResponseException) {
            ApiResponse.Error(e.response.status.value, "Server error (${e.response.status.value}). Please try again later.")
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }
}
