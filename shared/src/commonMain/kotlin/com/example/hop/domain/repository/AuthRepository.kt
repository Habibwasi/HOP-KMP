package com.example.hop.domain.repository

import com.example.hop.domain.model.User
import com.example.hop.network.ApiResponse

interface AuthRepository {
    suspend fun register(phone: String, firstName: String, lastName: String, email: String? = null, password: String? = null): ApiResponse<User>
    suspend fun login(email: String, password: String): ApiResponse<User>
    suspend fun logout(): ApiResponse<Unit>
    /**
     * Checks if a persisted refresh token exists and attempts to silently restore
     * the session by exchanging it for a fresh access token.
     *
     * @return [ApiResponse.Success] with the current user if the session was restored,
     *         [ApiResponse.Error] if there is no stored token or the refresh fails.
     */
    suspend fun restoreSession(): ApiResponse<User>
}
