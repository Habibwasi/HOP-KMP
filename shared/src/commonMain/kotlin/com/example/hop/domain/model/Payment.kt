package com.example.hop.domain.model

enum class PaymentStatus {
    HELD,
    RELEASED,
    REFUNDED,
    FAILED,
}

data class Payment(
    val id: String,
    val bookingId: String,
    val passengerId: String,
    val driverId: String,
    val amountOere: Int,
    val driverNetOere: Int,
    val platformFeeOere: Int,
    val status: PaymentStatus,
    val mobilepayRef: String?,
)
