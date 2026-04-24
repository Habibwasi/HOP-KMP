package com.example.hop.domain.repository

import com.example.hop.domain.model.User
import com.example.hop.network.ApiResponse

interface AuthRepository {
    suspend fun register(email: String, password: String, firstName: String, lastName: String, phone: String? = null): ApiResponse<User>
    suspend fun login(email: String, password: String): ApiResponse<User>
    suspend fun logout(): ApiResponse<Unit>
    /**
     * Checks if a Supabase session is persisted and fetches the current user from the backend.
     *
     * @return [ApiResponse.Success] with the current user if the session was restored,
     *         [ApiResponse.Error] if there is no stored session.
     */
    suspend fun restoreSession(): ApiResponse<User>

    /**
     * Handles an email-confirmation deep-link callback (hop://auth/callback…).
     * Imports the Supabase session from the URL, then fetches and returns the
     * current user so the caller can navigate to the home screen.
     */
    suspend fun handleDeepLink(url: String): ApiResponse<User>
}
