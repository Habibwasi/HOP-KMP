package com.example.hop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RideSettlement(
    val bookingId: String,
    val tripId: String?,
    val suggestedAmountOere: Int,
    val mobilepayNumber: String,
    val passengerPaidAt: String?,
    val driverConfirmedAt: String?,
    val disputedAt: String?,
    val disputeReason: String?,
    val createdAt: String,
)
