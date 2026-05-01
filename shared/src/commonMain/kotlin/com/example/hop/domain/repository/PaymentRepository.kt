package com.example.hop.domain.repository

import com.example.hop.network.ApiResponse

interface PaymentRepository {
    /** POST /payments/initiate — creates a payment intent and returns the provider redirect URL. */
    suspend fun initiatePayment(bookingId: String, provider: String): ApiResponse<PaymentInitResult>
}

data class PaymentInitResult(
    val paymentId: String,
    val provider: String,
    val redirectUrl: String,
    val amountOere: Int,
)
