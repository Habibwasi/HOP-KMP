package com.example.hop.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoleRated {
    DRIVER,
    PASSENGER,
}

@Serializable
data class Rating(
    val id: String,
    val bookingId: String,
    val raterId: String,
    val rateeId: String,
    val roleRated: RoleRated,
    val stars: Int,
    val comment: String?,
)
