package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.AuthResponse
import com.example.hop.data.dto.LoginRequest
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
        phone: String,
        firstName: String,
        lastName: String,
        email: String?,
        password: String?,
    ): ApiResponse<User> = safeAuthCall {
        httpClient.post("auth/register") {
            setBody(RegisterRequest(phone, firstName, lastName, email, password))
        }.body<ApiEnvelope<AuthResponse>>()
    }

    override suspend fun login(
        email: String,
        password: String,
    ): ApiResponse<User> = safeAuthCall {
        httpClient.post("auth/login") {
            setBody(LoginRequest(email, password))
        }.body<ApiEnvelope<AuthResponse>>()
    }

    override suspend fun logout(): ApiResponse<Unit> = safeApiCall {
        httpClient.post("auth/logout")
        Unit
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun safeAuthCall(
        block: suspend () -> ApiEnvelope<AuthResponse>,
    ): ApiResponse<User> {
        return try {
            val envelope = block()
            val error = envelope.error
            if (error != null) return ApiResponse.Error(error.code, error.message)
            val response = checkNotNull(envelope.data) { "Null data in auth envelope" }
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)
            ApiResponse.Success(response.user.toDomain())
        } catch (e: ClientRequestException) {
            val error = runCatching {
                e.response.body<ApiEnvelope<Nothing>>().error
            }.getOrNull()
            ApiResponse.Error(error?.code ?: e.response.status.value, error?.message ?: e.message)
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }
}
