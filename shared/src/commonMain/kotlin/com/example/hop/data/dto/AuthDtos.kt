package com.example.hop.data.dto

import com.example.hop.domain.model.User
import com.example.hop.domain.model.UserRole
import kotlinx.serialization.Serializable

// ── Request bodies ────────────────────────────────────────────────────────────

@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class OtpSendRequest(
    val phone: String,
)

@Serializable
data class OtpVerifyRequest(
    val phone: String,
    val code: String,
)

// ── Response bodies ───────────────────────────────────────────────────────────

@Serializable
data class UserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val phoneVerified: Boolean,
    val roles: List<String>,
    val isBanned: Boolean,
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
    fullName = fullName,
    email = email,
    phone = phone,
    phoneVerified = phoneVerified,
    roles = roles.mapNotNull { role -> runCatching { UserRole.valueOf(role) }.getOrNull() },
    isBanned = isBanned,
    ratingDriver = ratingDriver,
    ratingPassenger = ratingPassenger,
)
