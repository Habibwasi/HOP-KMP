package com.example.hop.domain.model

enum class RoleRated {
    DRIVER,
    PASSENGER,
}

data class Rating(
    val id: String,
    val bookingId: String,
    val raterId: String,
    val rateeId: String,
    val roleRated: RoleRated,
    val stars: Int,
    val comment: String?,
)
