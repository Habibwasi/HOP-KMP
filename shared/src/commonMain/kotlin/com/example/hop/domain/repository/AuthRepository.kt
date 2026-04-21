package com.example.hop.domain.repository

import com.example.hop.domain.model.User
import com.example.hop.network.ApiResponse

interface AuthRepository {
    suspend fun register(phone: String, firstName: String, lastName: String, email: String? = null, password: String? = null): ApiResponse<User>
    suspend fun login(email: String, password: String): ApiResponse<User>
    suspend fun logout(): ApiResponse<Unit>
}
