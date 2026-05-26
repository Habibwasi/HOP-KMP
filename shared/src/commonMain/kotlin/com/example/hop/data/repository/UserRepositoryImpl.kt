package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.CarDetailsDto
import com.example.hop.data.dto.HopNotificationDto
import com.example.hop.data.dto.PushTokenRequest
import com.example.hop.data.dto.ReportUserRequest
import com.example.hop.data.dto.UpdateMobilepayRequest
import com.example.hop.data.dto.UpdateNameRequest
import com.example.hop.data.dto.UserDto
import com.example.hop.data.dto.UserReviewDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.HopNotification
import com.example.hop.domain.model.User
import com.example.hop.domain.model.UserReview
import com.example.hop.domain.repository.UserRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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
            if (error != null) return ApiResponse.Error(error.code, error.message, error.errorCode)
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

    override suspend fun updateMobilepayNumber(number: String): ApiResponse<User> {
        val response = safeEnvelopeCall<UserDto> {
            httpClient.patch("users/me") {
                setBody(UpdateMobilepayRequest(mobilepayNumber = number))
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

    override suspend fun savePushToken(token: String, platform: String): ApiResponse<Unit> =
        safeApiCall {
            httpClient.post("users/push-token") {
                setBody(PushTokenRequest(token = token, platform = platform))
            }
            Unit
        }

    override suspend fun getNotifications(): ApiResponse<List<HopNotification>> {
        val response = safeEnvelopeCall<List<HopNotificationDto>> {
            httpClient.get("notifications").body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.map { it.toDomain() })
            is ApiResponse.Error -> response
        }
    }

    override suspend fun markNotificationRead(notificationId: String): ApiResponse<Unit> =
        safeApiCall {
            httpClient.post("notifications/$notificationId/read")
            Unit
        }

    override suspend fun uploadAvatar(imageData: ByteArray, contentType: String): ApiResponse<User> {
        val response = safeEnvelopeCall<UserDto> {
            httpClient.post("users/me/avatar") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append(
                            key = "file",
                            value = imageData,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, contentType)
                                append(HttpHeaders.ContentDisposition, "filename=avatar.jpg")
                            }
                        )
                    }
                ))
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(response.data.toDomain())
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
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }
}
