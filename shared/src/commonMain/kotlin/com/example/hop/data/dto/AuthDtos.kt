package com.example.hop.data.dto

import com.example.hop.domain.model.User
import com.example.hop.domain.model.UserRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Request bodies ────────────────────────────────────────────────────────────

@Serializable
data class CreateProfileRequest(
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val email: String? = null,
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
    val mobilepayNumber: String? = null,
    val avatarUrl: String? = null,
)

// ── Envelope ──────────────────────────────────────────────────────────────────

@Serializable
data class ApiEnvelope<T>(
    val data: T? = null,
    val error: ApiEnvelopeError? = null,
)

@Serializable
data class ApiEnvelopeError(
    @SerialName("statusCode") val code: Int,
    val message: String,
    val errorCode: String? = null,
)

// ── Mapping ───────────────────────────────────────────────────────────────────

fun UserDto.toDomain(): User = User(
    id = id,
    fullName = "$firstName $lastName".trim(),
    email = email.orEmpty(),
    phone = phone,
    phoneVerified = isVerified,
    roles = when (role.uppercase()) {
        "BOTH" -> listOf(UserRole.PASSENGER, UserRole.DRIVER)
        else -> listOfNotNull(runCatching { UserRole.valueOf(role.uppercase()) }.getOrNull())
    },
    isBanned = isBanned,
    ratingDriver = ratingDriver,
    ratingPassenger = ratingPassenger,
    mobilepayNumber = mobilepayNumber,
    avatarUrl = avatarUrl,
)
