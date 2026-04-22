package com.example.hop.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders

class AuthInterceptorConfig {
    var supabase: SupabaseClient? = null
}

val AuthInterceptor = createClientPlugin("AuthInterceptor", ::AuthInterceptorConfig) {
    val supabase = requireNotNull(pluginConfig.supabase) {
        "SupabaseClient must be set via AuthInterceptorConfig.supabase"
    }

    onRequest { request, _ ->
        val token = supabase.auth.currentSessionOrNull()?.accessToken
        if (token != null) {
            request.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}
