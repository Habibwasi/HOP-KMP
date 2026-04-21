package com.example.hop.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.plugin
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(
        tokenStorage: TokenStorage,
        tokenRefreshManager: TokenRefreshManager,
        baseUrl: String = NetworkConstants.PRODUCTION_BASE_URL,
    ): HttpClient {
        val client = HttpClient {

            expectSuccess = true

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) = println("Ktor: $message")
                }
                level = LogLevel.ALL // Reduce to LogLevel.NONE for production builds
            }

            install(HttpTimeout) {
                requestTimeoutMillis = NetworkConstants.REQUEST_TIMEOUT_MS
                connectTimeoutMillis = NetworkConstants.CONNECT_TIMEOUT_MS
                socketTimeoutMillis = NetworkConstants.SOCKET_TIMEOUT_MS
            }

            install(AuthInterceptor) {
                this.tokenStorage = tokenStorage
            }

            defaultRequest {
                url(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
                contentType(ContentType.Application.Json)
            }
        }

        // HttpSend interceptors must be registered on the created client in Ktor 3.x.
        // (install(HttpSend) { intercept { } } is config-time only and does not compile.)
        client.plugin(HttpSend).intercept { request ->
            val call = execute(request)

            if (call.response.status != HttpStatusCode.Unauthorized) {
                return@intercept call
            }

            // Capture the stale token before entering tryRefresh so concurrent waiters
            // can detect it was already rotated and skip a redundant refresh call.
            val staleToken = request.headers[HttpHeaders.Authorization]
                ?.removePrefix("Bearer ")

            val refreshed = tokenRefreshManager.tryRefresh(staleToken)
            if (!refreshed) {
                return@intercept call
            }

            // Swap in the fresh access token and retry the original request.
            val newToken = tokenStorage.getAccessToken()
            if (newToken != null) {
                request.headers.remove(HttpHeaders.Authorization)
                request.headers.append(HttpHeaders.Authorization, "Bearer $newToken")
            }
            execute(request)
        }

        return client
    }
}
