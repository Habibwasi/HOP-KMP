package com.example.hop.data.dto

import com.example.hop.domain.model.HopNotification
import com.example.hop.domain.model.NotificationType
import kotlinx.serialization.Serializable

@Serializable
data class HopNotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val createdAt: String,
    val isRead: Boolean,
    val deepLinkId: String? = null,
)

@Serializable
data class PushTokenRequest(
    val token: String,
    val platform: String = "android",
)

fun HopNotificationDto.toDomain(): HopNotification = HopNotification(
    id = id,
    type = when (type) {
        "BOOKING_CONFIRMED"    -> NotificationType.BOOKING_CONFIRMED
        "BOOKING_CANCELLED"    -> NotificationType.BOOKING_CANCELLED
        "TRIP_REMINDER"        -> NotificationType.TRIP_REMINDER
        "NEW_RATING"           -> NotificationType.NEW_RATING
        "THRESHOLD_MET"        -> NotificationType.THRESHOLD_MET
        "CHAT_MESSAGE"         -> NotificationType.CHAT_MESSAGE
        "PAYMENT_MARKED_PAID" -> NotificationType.PAYMENT_MARKED_PAID
        "PAYMENT_CONFIRMED"   -> NotificationType.PAYMENT_CONFIRMED
        "PAYMENT_DISPUTED"    -> NotificationType.PAYMENT_DISPUTED
        "RIDE_AWAITING_PAYMENT" -> NotificationType.RIDE_AWAITING_PAYMENT
        "SEARCH_ALERT"        -> NotificationType.SEARCH_ALERT
        else                  -> NotificationType.GENERAL
    },
    title = title,
    body = body,
    createdAt = createdAt,
    isRead = isRead,
    deepLinkId = deepLinkId,
)
