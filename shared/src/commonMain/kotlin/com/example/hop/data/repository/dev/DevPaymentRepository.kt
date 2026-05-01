package com.example.hop.data.repository.dev

import com.example.hop.domain.repository.PaymentInitResult
import com.example.hop.domain.repository.PaymentRepository
import com.example.hop.network.ApiResponse

class DevPaymentRepository : PaymentRepository {
    override suspend fun initiatePayment(bookingId: String, provider: String): ApiResponse<PaymentInitResult> {
        return ApiResponse.Success(
            PaymentInitResult(
                paymentId = "dev-payment-$bookingId",
                provider = provider,
                redirectUrl = "https://dev.hop.dk/payment/redirect",
                amountOere = 15000,
            )
        )
    }
}
