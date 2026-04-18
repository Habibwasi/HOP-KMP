package com.example.hop.data.dto

import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.RoleRated
import com.example.hop.domain.model.UserReview
import kotlinx.serialization.Serializable

// ── User review ───────────────────────────────────────────────────────────────

@Serializable
data class UserReviewDto(
    val id: String,
    val raterName: String,
    val stars: Int,
    val comment: String? = null,
    /** "DRIVER" or "PASSENGER" */
    val roleRated: String,
)

fun UserReviewDto.toDomain(): UserReview = UserReview(
    id = id,
    raterName = raterName,
    stars = stars,
    comment = comment,
    roleRated = runCatching { RoleRated.valueOf(roleRated) }.getOrDefault(RoleRated.PASSENGER),
)

// ── Car details ───────────────────────────────────────────────────────────────

@Serializable
data class CarDetailsDto(
    val make: String,
    val model: String,
    val year: Int,
    val licensePlate: String,
    val colour: String,
    val seatsAvailable: Int,
)

fun CarDetailsDto.toDomain(): CarDetails = CarDetails(
    make = make,
    model = model,
    year = year,
    licensePlate = licensePlate,
    colour = colour,
    seatsAvailable = seatsAvailable,
)

// ── Update name ───────────────────────────────────────────────────────────────

@Serializable
data class UpdateNameRequest(
    val fullName: String,
)

// ── Report user ───────────────────────────────────────────────────────────────

@Serializable
data class ReportUserRequest(
    val reason: String,
)
