package com.example.hop.domain.repository

import com.example.hop.domain.model.User
import com.example.hop.network.ApiResponse

interface AuthRepository {
    suspend fun register(fullName: String, email: String, password: String): ApiResponse<User>
    suspend fun login(email: String, password: String): ApiResponse<User>
    suspend fun sendOtp(phone: String): ApiResponse<Unit>
    suspend fun verifyOtp(phone: String, code: String): ApiResponse<User>
    suspend fun logout(): ApiResponse<Unit>
}
