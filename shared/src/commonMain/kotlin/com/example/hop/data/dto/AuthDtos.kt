package com.example.hop.data.dto

import com.example.hop.domain.model.User
import com.example.hop.domain.model.UserRole
import kotlinx.serialization.Serializable

// ── Request bodies ────────────────────────────────────────────────────────────

@Serializable
data class RegisterRequest(
    val phone: String,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val password: String? = null,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

// ── Response bodies ───────────────────────────────────────────────────────────

@Serializable
data class UserDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val phone: String? = null,
    val isVerified: Boolean = false,
    val role: String = "PASSENGER",
    val isBanned: Boolean = false,
    val ratingDriver: Double? = null,
    val ratingPassenger: Double? = null,
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto,
)

// ── Envelope ──────────────────────────────────────────────────────────────────

@Serializable
data class ApiEnvelope<T>(
    val data: T? = null,
    val error: ApiEnvelopeError? = null,
)

@Serializable
data class ApiEnvelopeError(
    val code: Int,
    val message: String,
)

// ── Mapping ───────────────────────────────────────────────────────────────────

fun UserDto.toDomain(): User = User(
    id = id,
    fullName = "$firstName $lastName".trim(),
    email = email.orEmpty(),
    phone = phone,
    phoneVerified = isVerified,
    roles = listOfNotNull(runCatching { UserRole.valueOf(role) }.getOrNull()),
    isBanned = isBanned,
    ratingDriver = ratingDriver,
    ratingPassenger = ratingPassenger,
)
