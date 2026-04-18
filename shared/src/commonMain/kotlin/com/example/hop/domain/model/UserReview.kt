package com.example.hop.domain.model

import kotlinx.serialization.Serializable

/**
 * A single review left for a user, as returned by GET /users/:id/reviews.
 * The backend join includes [raterName] so the client never needs a second call.
 */
@Serializable
data class UserReview(
    val id: String,
    val raterName: String,
    val stars: Int,
    val comment: String?,
    val roleRated: RoleRated,
)
