package com.example.hop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TripSettlementEntry(
    val bookingId: String,
    val passengerFirstName: String,
    val passengerLastName: String,
    val suggestedAmountOere: Int,
    val passengerPaidAt: String?,
    val driverConfirmedAt: String?,
    val bookingStatus: String,
) {
    val passengerName: String get() = "$passengerFirstName $passengerLastName"
    val passengerInitials: String get() = buildString {
        passengerFirstName.firstOrNull()?.let { append(it.uppercaseChar()) }
        passengerLastName.firstOrNull()?.let { append(it.uppercaseChar()) }
    }

    enum class PaymentStatus { WAITING, PAID, CONFIRMED }

    val paymentStatus: PaymentStatus get() = when {
        driverConfirmedAt != null -> PaymentStatus.CONFIRMED
        passengerPaidAt != null -> PaymentStatus.PAID
        else -> PaymentStatus.WAITING
    }
}
