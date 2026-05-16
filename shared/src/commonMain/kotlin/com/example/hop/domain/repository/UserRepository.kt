package com.example.hop.domain.repository

import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.HopNotification
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
    /** PATCH /users/me — updates the driver's MobilePay number (8-digit DK phone). */
    suspend fun updateMobilepayNumber(number: String): ApiResponse<User>
    /** POST /users/:id/report — files a report against another user. */
    suspend fun reportUser(userId: String, reason: String): ApiResponse<Unit>
    /** POST /users/push-token — registers or refreshes an FCM push token. */
    suspend fun savePushToken(token: String): ApiResponse<Unit>
    /** GET /notifications — returns the notification inbox for the current user. */
    suspend fun getNotifications(): ApiResponse<List<HopNotification>>
    /** POST /notifications/:id/read — marks a single notification as read. */
    suspend fun markNotificationRead(notificationId: String): ApiResponse<Unit>
}
