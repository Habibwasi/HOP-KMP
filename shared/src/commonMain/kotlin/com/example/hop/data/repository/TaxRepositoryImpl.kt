package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.TaxReportSummaryDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.repository.TaxRepository
import com.example.hop.domain.repository.TaxReportSummary
import com.example.hop.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get

class TaxRepositoryImpl(
    private val httpClient: HttpClient,
) : TaxRepository {

    override suspend fun getTaxReport(year: Int): ApiResponse<TaxReportSummary> {
        return try {
            val envelope: ApiEnvelope<TaxReportSummaryDto> =
                httpClient.get("tax/report/$year").body()
            val error = envelope.error
            if (error != null) return ApiResponse.Error(error.code, error.message)
            val data = checkNotNull(envelope.data) { "Null data in tax report envelope" }
            ApiResponse.Success(data.toDomain())
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }
}
