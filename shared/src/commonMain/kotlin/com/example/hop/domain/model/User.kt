package com.example.hop.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    DRIVER,
    PASSENGER,
    ADMIN,
}

@Serializable
data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val phoneVerified: Boolean,
    val roles: List<UserRole>,
    val isBanned: Boolean,
    val ratingDriver: Double?,
    val ratingPassenger: Double?,
    val mobilepayNumber: String? = null,
    val avatarUrl: String? = null,
)
