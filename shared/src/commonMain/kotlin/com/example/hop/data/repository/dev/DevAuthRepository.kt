package com.example.hop.data.repository.dev

import com.example.hop.domain.model.User
import com.example.hop.domain.model.UserRole
import com.example.hop.domain.repository.AuthRepository
import com.example.hop.network.ApiResponse

/**
 * Fake [AuthRepository] that always succeeds with a dummy user.
 * Used in debug builds so you can test the app without a real backend.
 */
class DevAuthRepository : AuthRepository {

    companion object {
        val DEV_USER = User(
            id = "dev-user-001",
            fullName = "Dev Tester",
            email = "dev@hop.test",
            phone = "+4512345678",
            phoneVerified = true,
            roles = listOf(UserRole.PASSENGER, UserRole.DRIVER),
            isBanned = false,
            ratingDriver = 4.7,
            ratingPassenger = 4.9,
        )
    }

    override suspend fun register(email: String, password: String, firstName: String, lastName: String, phone: String?): ApiResponse<User> =
        ApiResponse.Success(DEV_USER.copy(phone = phone, fullName = "$firstName $lastName"))

    override suspend fun login(email: String, password: String): ApiResponse<User> =
        ApiResponse.Success(DEV_USER)

    override suspend fun logout(): ApiResponse<Unit> =
        ApiResponse.Success(Unit)

    override suspend fun restoreSession(): ApiResponse<User> =
        ApiResponse.Success(DEV_USER)
}
