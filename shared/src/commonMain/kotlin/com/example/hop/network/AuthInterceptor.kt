package com.example.hop.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders

class AuthInterceptorConfig {
    var tokenStorage: TokenStorage? = null
}

val AuthInterceptor = createClientPlugin("AuthInterceptor", ::AuthInterceptorConfig) {
    val storage = requireNotNull(pluginConfig.tokenStorage) {
        "TokenStorage must be set via AuthInterceptorConfig.tokenStorage"
    }

    onRequest { request, _ ->
        val token = storage.getAccessToken()
        if (token != null) {
            request.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}
