package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.CreateProfileRequest
import com.example.hop.data.dto.UserDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.User
import com.example.hop.domain.repository.AuthRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.safeApiCall
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SupabaseAuthRepositoryImpl(
    private val supabase: SupabaseClient,
    private val httpClient: HttpClient,
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?,
    ): ApiResponse<User> = safeApiCall {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val envelope = httpClient.post("users/profile") {
            setBody(CreateProfileRequest(firstName, lastName, phone, email))
        }.body<ApiEnvelope<UserDto>>()
        val error = envelope.error
        if (error != null) throw Exception(error.message)
        checkNotNull(envelope.data) { "Null data in /users/profile response" }.toDomain()
    }

    override suspend fun login(
        email: String,
        password: String,
    ): ApiResponse<User> = safeApiCall {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val envelope = httpClient.get("users/me").body<ApiEnvelope<UserDto>>()
        val error = envelope.error
        if (error != null) throw Exception(error.message)
        checkNotNull(envelope.data) { "Null data in /users/me response" }.toDomain()
    }

    override suspend fun logout(): ApiResponse<Unit> = safeApiCall {
        supabase.auth.signOut()
    }

    override suspend fun restoreSession(): ApiResponse<User> {
        val session = supabase.auth.currentSessionOrNull()
            ?: return ApiResponse.Error(-1, "No stored session")
        return safeApiCall {
            val envelope = httpClient.get("users/me").body<ApiEnvelope<UserDto>>()
            val error = envelope.error
            if (error != null) throw Exception(error.message)
            checkNotNull(envelope.data) { "Null data in /users/me response" }.toDomain()
        }
    }
}
