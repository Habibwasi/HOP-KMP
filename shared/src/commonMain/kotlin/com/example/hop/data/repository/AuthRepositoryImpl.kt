package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.AuthResponse
import com.example.hop.data.dto.LoginRequest
import com.example.hop.data.dto.OtpSendRequest
import com.example.hop.data.dto.OtpVerifyRequest
import com.example.hop.data.dto.RegisterRequest
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.User
import com.example.hop.domain.repository.AuthRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.TokenStorage
import com.example.hop.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
    ): ApiResponse<User> {
        val response = safeEnvelopeCall<AuthResponse> {
            httpClient.post("auth/register") {
                setBody(RegisterRequest(fullName, email, password))
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> {
                tokenStorage.saveAccessToken(response.data.accessToken)
                tokenStorage.saveRefreshToken(response.data.refreshToken)
                ApiResponse.Success(response.data.user.toDomain())
            }
            is ApiResponse.Error -> response
        }
    }

    override suspend fun login(
        email: String,
        password: String,
    ): ApiResponse<User> {
        val response = safeEnvelopeCall<AuthResponse> {
            httpClient.post("auth/login") {
                setBody(LoginRequest(email, password))
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> {
                tokenStorage.saveAccessToken(response.data.accessToken)
                tokenStorage.saveRefreshToken(response.data.refreshToken)
                ApiResponse.Success(response.data.user.toDomain())
            }
            is ApiResponse.Error -> response
        }
    }

    override suspend fun sendOtp(phone: String): ApiResponse<Unit> = safeApiCall {
        httpClient.post("auth/otp/send") {
            setBody(OtpSendRequest(phone))
        }
        Unit
    }

    override suspend fun verifyOtp(phone: String, code: String): ApiResponse<User> {
        val response = safeEnvelopeCall<AuthResponse> {
            httpClient.post("auth/otp/verify") {
                setBody(OtpVerifyRequest(phone, code))
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> {
                tokenStorage.saveAccessToken(response.data.accessToken)
                tokenStorage.saveRefreshToken(response.data.refreshToken)
                ApiResponse.Success(response.data.user.toDomain())
            }
            is ApiResponse.Error -> response
        }
    }

    override suspend fun logout(): ApiResponse<Unit> = safeApiCall {
        httpClient.post("auth/logout")
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
