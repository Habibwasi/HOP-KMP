package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.BookingDto
import com.example.hop.data.dto.CreateBookingRequest
import com.example.hop.data.dto.RateBookingRequest
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.Booking
import com.example.hop.domain.repository.BookingRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class BookingRepositoryImpl(
    private val httpClient: HttpClient,
) : BookingRepository {

    override suspend fun createBooking(tripId: String, seats: Int): ApiResponse<Booking> {
        val response = safeEnvelopeCall<BookingDto> {
            httpClient.post("bookings") {
                setBody(CreateBookingRequest(tripId = tripId, seats = seats))
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.toDomain())
            is ApiResponse.Error -> response
        }
    }

    override suspend fun getBooking(id: String): ApiResponse<Booking> {
        val response = safeEnvelopeCall<BookingDto> {
            httpClient.get("bookings/$id").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.toDomain())
            is ApiResponse.Error -> response
        }
    }

    override suspend fun cancelBooking(id: String): ApiResponse<Unit> = safeApiCall {
        httpClient.patch("bookings/$id/cancel")
        Unit
    }

    override suspend fun rateBooking(
        bookingId: String,
        stars: Int,
        comment: String?,
    ): ApiResponse<Unit> = safeApiCall {
        httpClient.post("bookings/$bookingId/rate") {
            setBody(RateBookingRequest(stars = stars, comment = comment))
        }
        Unit
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
        } catch (e: io.ktor.client.plugins.HttpRequestTimeoutException) {
            ApiResponse.Error(ApiResponse.CODE_TIMEOUT, "Connection timed out. Please check your network and try again.")
        } catch (e: io.ktor.client.plugins.ServerResponseException) {
            ApiResponse.Error(e.response.status.value, "Server error (${e.response.status.value}). Please try again later.")
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }
}
