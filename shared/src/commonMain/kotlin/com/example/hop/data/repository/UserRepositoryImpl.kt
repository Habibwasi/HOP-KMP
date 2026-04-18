package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.CarDetailsDto
import com.example.hop.data.dto.ReportUserRequest
import com.example.hop.data.dto.UpdateNameRequest
import com.example.hop.data.dto.UserDto
import com.example.hop.data.dto.UserReviewDto
import com.example.hop.data.dto.toDomain
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.User
import com.example.hop.domain.model.UserReview
import com.example.hop.domain.repository.UserRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

class UserRepositoryImpl(
    private val httpClient: HttpClient,
) : UserRepository {

    override suspend fun getCurrentUserProfile(): ApiResponse<User> {
        val response = safeEnvelopeCall<UserDto> {
            httpClient.get("users/me").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.toDomain())
            is ApiResponse.Error -> response
        }
    }

    override suspend fun getUserProfile(userId: String): ApiResponse<User> {
        val response = safeEnvelopeCall<UserDto> {
            httpClient.get("users/$userId").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.toDomain())
            is ApiResponse.Error -> response
        }
    }

    override suspend fun getUserReviews(userId: String): ApiResponse<List<UserReview>> {
        val response = safeEnvelopeCall<List<UserReviewDto>> {
            httpClient.get("users/$userId/reviews").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.map { it.toDomain() })
            is ApiResponse.Error -> response
        }
    }

    override suspend fun getUserCarDetails(userId: String): ApiResponse<CarDetails?> {
        return try {
            val envelope: ApiEnvelope<CarDetailsDto?> = httpClient.get("users/$userId/car").body()
            val error = envelope.error
            if (error != null) return ApiResponse.Error(error.code, error.message)
            ApiResponse.Success(envelope.data?.toDomain())
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                ApiResponse.Success(null)
            } else {
                ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
            }
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }

    override suspend fun updateFullName(name: String): ApiResponse<User> {
        val response = safeEnvelopeCall<UserDto> {
            httpClient.patch("users/me") {
                setBody(UpdateNameRequest(fullName = name))
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.toDomain())
            is ApiResponse.Error -> response
        }
    }

    override suspend fun reportUser(userId: String, reason: String): ApiResponse<Unit> =
        safeApiCall {
            httpClient.post("users/$userId/report") {
                setBody(ReportUserRequest(reason = reason))
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
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }
}
