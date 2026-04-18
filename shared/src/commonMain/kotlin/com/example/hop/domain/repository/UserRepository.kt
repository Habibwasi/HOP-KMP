package com.example.hop.domain.repository

import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.User
import com.example.hop.domain.model.UserReview
import com.example.hop.network.ApiResponse

interface UserRepository {
    /** GET /users/me — own profile from the current session token. */
    suspend fun getCurrentUserProfile(): ApiResponse<User>
    /** GET /users/:id — any user's public profile. */
    suspend fun getUserProfile(userId: String): ApiResponse<User>
    /** GET /users/:id/reviews — paginated reviews for the user (returns all for MVP). */
    suspend fun getUserReviews(userId: String): ApiResponse<List<UserReview>>
    /** GET /users/:id/car — car details for the given driver. Null if the user has no car. */
    suspend fun getUserCarDetails(userId: String): ApiResponse<CarDetails?>
    /** PATCH /users/me — updates the current user's full name. */
    suspend fun updateFullName(name: String): ApiResponse<User>
    /** POST /users/:id/report — files a report against another user. */
    suspend fun reportUser(userId: String, reason: String): ApiResponse<Unit>
}
