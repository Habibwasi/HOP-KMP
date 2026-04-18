package com.example.hop.network

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.AuthResponse
import com.example.hop.data.dto.RefreshTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * Handles the JWT refresh flow at the network layer.
 *
 * Uses a **separate** minimal [HttpClient] (no auth interceptor, no [HttpSend] retry) to
 * avoid recursion when the refresh endpoint itself returns an error.
 *
 * A [Mutex] serialises concurrent 401 responses into a single refresh attempt. Waiters that
 * acquire the lock after the first caller has already refreshed detect the stale-token
 * short-circuit and return `true` immediately, so the rotating refresh token is only
 * consumed once per expiry cycle.
 */
class TokenRefreshManager(
    private val tokenStorage: TokenStorage,
    private val sessionExpiryNotifier: SessionExpiryNotifier,
    private val baseUrl: String,
) {

    private val mutex = Mutex()

    /**
     * A dedicated client with no auth or retry plugins — used exclusively for the
     * `POST /auth/refresh` call so that it never re-enters the [HttpSend] interceptor.
     */
    private val refreshClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
        }
    }

    /**
     * Attempts to exchange the stored refresh token for a new token pair.
     *
     * @param staleAccessToken the token that triggered the 401. If the stored access token has
     *   already changed (a concurrent caller refreshed while this one was waiting for the Mutex),
     *   the refresh is skipped and `true` is returned immediately.
     * @return `true` if the caller may retry its original request with a fresh token;
     *         `false` if the session is unrecoverable (notifyExpired has already been called).
     */
    suspend fun tryRefresh(staleAccessToken: String?): Boolean = mutex.withLock {
        // Short-circuit: another coroutine refreshed while we were waiting for the lock.
        // The access token has already rotated — no need to burn another refresh token.
        val currentAccessToken = tokenStorage.getAccessToken()
        if (staleAccessToken != null && currentAccessToken != staleAccessToken) {
            return@withLock true
        }

        val refreshToken = tokenStorage.getRefreshToken()
        if (refreshToken == null) {
            sessionExpiryNotifier.notifyExpired()
            return@withLock false
        }

        return@withLock try {
            val response = refreshClient.post("${baseUrl.trimEnd('/')}/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken = refreshToken))
            }

            if (response.status.isSuccess()) {
                val envelope = response.body<ApiEnvelope<AuthResponse>>()
                val authData = envelope.data
                if (authData != null) {
                    tokenStorage.saveAccessToken(authData.accessToken)
                    tokenStorage.saveRefreshToken(authData.refreshToken)
                    true
                } else {
                    tokenStorage.clearTokens()
                    sessionExpiryNotifier.notifyExpired()
                    false
                }
            } else {
                tokenStorage.clearTokens()
                sessionExpiryNotifier.notifyExpired()
                false
            }
        } catch (e: Exception) {
            tokenStorage.clearTokens()
            sessionExpiryNotifier.notifyExpired()
            false
        }
    }

    fun close() {
        refreshClient.close()
    }
}
