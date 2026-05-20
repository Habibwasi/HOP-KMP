package com.example.hop.network

import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(
        supabase: SupabaseClient,
        baseUrl: String = NetworkConstants.PRODUCTION_BASE_URL,
        sessionExpiryNotifier: SessionExpiryNotifier? = null,
    ): HttpClient = HttpClient {

        expectSuccess = true

        // Automatically retry on transient connection failures (stale keep-alive
        // connections, network resets, etc.).  retryOnException() covers any
        // exception thrown before a response is received, which is safe for
        // idempotent GET/HEAD requests and avoids java.* references in commonMain.
        install(HttpRequestRetry) {
            maxRetries = 2
            retryOnException(maxRetries = 2, retryOnTimeout = true)
            exponentialDelay(base = 2.0, maxDelayMs = 5_000)
        }

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
            level = LogLevel.ALL
        }

        install(HttpTimeout) {
            requestTimeoutMillis = NetworkConstants.REQUEST_TIMEOUT_MS
            connectTimeoutMillis = NetworkConstants.CONNECT_TIMEOUT_MS
            socketTimeoutMillis = NetworkConstants.SOCKET_TIMEOUT_MS
        }

        install(AuthInterceptor) {
            this.supabase = supabase
        }

        if (sessionExpiryNotifier != null) {
            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status == HttpStatusCode.Unauthorized) {
                        sessionExpiryNotifier.notifyExpired()
                    }
                }
            }
        }

        defaultRequest {
            url(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            contentType(ContentType.Application.Json)
        }
    }
}
