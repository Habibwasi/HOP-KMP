package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.domain.repository.PaymentInitResult
import com.example.hop.domain.repository.PaymentRepository
import com.example.hop.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
private data class InitiatePaymentRequest(
    val bookingId: String,
    val provider: String,
)

@Serializable
private data class PaymentInitDto(
    val paymentId: String,
    val provider: String,
    val redirectUrl: String,
    val amountOere: Int,
)

class PaymentRepositoryImpl(
    private val httpClient: HttpClient,
) : PaymentRepository {

    override suspend fun initiatePayment(bookingId: String, provider: String): ApiResponse<PaymentInitResult> {
        val response = safeEnvelopeCall<PaymentInitDto> {
            httpClient.post("payments/initiate") {
                setBody(InitiatePaymentRequest(bookingId = bookingId, provider = provider))
            }.body()
        }
        return when (response) {
            is ApiResponse.Success -> ApiResponse.Success(
                PaymentInitResult(
                    paymentId = response.data.paymentId,
                    provider = response.data.provider,
                    redirectUrl = response.data.redirectUrl,
                    amountOere = response.data.amountOere,
                )
            )
            is ApiResponse.Error -> response
        }
    }

    private suspend fun <T> safeEnvelopeCall(
        block: suspend () -> ApiEnvelope<T>,
    ): ApiResponse<T> = try {
        val envelope = block()
        if (envelope.data != null) ApiResponse.Success(envelope.data)
        else ApiResponse.Error(envelope.error?.code ?: -1, envelope.error?.message ?: "Unknown error")
    } catch (e: Exception) {
        ApiResponse.Error(-1, e.message ?: "Unknown error")
    }
}
