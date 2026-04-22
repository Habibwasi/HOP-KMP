package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.AuthResponse
import com.example.hop.data.dto.LoginRequest
import com.example.hop.data.dto.RegisterRequest
import com.example.hop.data.dto.UserDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.User
import com.example.hop.domain.repository.AuthRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.TokenStorage
import com.example.hop.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
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
        val refreshToken = tokenStorage.getRefreshToken()
        httpClient.post("auth/logout") {
            if (refreshToken != null) setBody(mapOf("refreshToken" to refreshToken))
        }
        tokenStorage.clearTokens()
    }

    /**
     * Silent session restore on cold start.
     *
     * If a refresh token is persisted, calls GET /users/me with the stored
     * access token. The [AuthInterceptor] + [HttpSend] interceptor will
     * silently exchange it for a fresh token pair if it has expired.
     * Returns [ApiResponse.Error] (no network call) when no token is stored.
     */
    override suspend fun restoreSession(): ApiResponse<User> {
        val hasToken = tokenStorage.getRefreshToken() != null
        if (!hasToken) return ApiResponse.Error(-1, "No stored session")
        return try {
            val envelope = httpClient.get("users/me").body<ApiEnvelope<UserDto>>()
            val error = envelope.error
            if (error != null) return ApiResponse.Error(error.code, error.message)
            val userDto = checkNotNull(envelope.data) { "Null data in /users/me envelope" }
            ApiResponse.Success(userDto.toDomain())
        } catch (e: ClientRequestException) {
            val error = runCatching {
                e.response.body<ApiEnvelope<Nothing>>().error
            }.getOrNull()
            ApiResponse.Error(error?.code ?: e.response.status.value, error?.message ?: e.message)
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
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

            // Revoke the previous refresh token on the server (best-effort) before
            // overwriting local storage. This prevents a prior session — belonging to
            // a different account — from remaining valid after a new login/register.
            val oldRefreshToken = tokenStorage.getRefreshToken()
            if (oldRefreshToken != null) {
                try {
                    httpClient.post("auth/logout") {
                        setBody(mapOf("refreshToken" to oldRefreshToken))
                    }
                } catch (_: Exception) {
                    // Best-effort: if the revocation call fails we still proceed.
                    // The server-side single-session policy (revoking all tokens on
                    // generateTokens) acts as a safety net.
                }
            }

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
