package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.ActiveBookingDto
import com.example.hop.data.dto.UnreadCountDto
import com.example.hop.data.dto.UserStatsDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.ActiveBooking
import com.example.hop.domain.model.UserStats
import com.example.hop.domain.repository.HomeStatsRepository
import com.example.hop.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get

class HomeStatsRepositoryImpl(
    private val httpClient: HttpClient,
) : HomeStatsRepository {

    override suspend fun getMyStats(): ApiResponse<UserStats> {
        val response = safeEnvelopeCall<UserStatsDto> {
            httpClient.get("users/me/stats").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.toDomain())
            is ApiResponse.Error -> response
        }
    }

    override suspend fun getUnreadNotificationsCount(): ApiResponse<Int> {
        val response = safeEnvelopeCall<UnreadCountDto> {
            httpClient.get("notifications/unread-count").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.count)
            is ApiResponse.Error -> response
        }
    }

    override suspend fun getActiveBooking(): ApiResponse<ActiveBooking?> {
        // The endpoint may legitimately return null inside the envelope when
        // the user has no upcoming confirmed bookings. We can't use
        // safeEnvelopeCall directly because it throws on null data — handle
        // the envelope inline so null becomes Success(null) instead of an
        // error.
        return try {
            val envelope: ApiEnvelope<ActiveBookingDto?> =
                httpClient.get("bookings/me/active").body()
            val error = envelope.error
            if (error != null) ApiResponse.Error(error.code, error.message, error.errorCode)
            else ApiResponse.Success(envelope.data?.toDomain())
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }

    private suspend fun <T> safeEnvelopeCall(
        block: suspend () -> ApiEnvelope<T>,
    ): ApiResponse<T> = try {
        val envelope = block()
        val error = envelope.error
        if (error != null) ApiResponse.Error(error.code, error.message, error.errorCode)
        else ApiResponse.Success(checkNotNull(envelope.data) { "Null data in API envelope" })
    } catch (e: ClientRequestException) {
        ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
    } catch (e: Exception) {
        ApiResponse.Error(-1, e.message ?: "Unknown error")
    }
}
