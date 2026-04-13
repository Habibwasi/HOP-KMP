package com.example.hop.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(
        tokenStorage: TokenStorage,
        baseUrl: String = NetworkConstants.PRODUCTION_BASE_URL,
    ): HttpClient = HttpClient {

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
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
}
