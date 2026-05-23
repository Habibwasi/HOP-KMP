package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.TaxMonthlySummaryDto
import com.example.hop.data.dto.TaxRecordDto
import com.example.hop.data.dto.TaxReportSummaryDto
import com.example.hop.data.dto.TaxReportUrlDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.TaxMonthlySummary
import com.example.hop.domain.model.TaxRecord
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
            if (error != null) return ApiResponse.Error(error.code, error.message, error.errorCode)
            val data = checkNotNull(envelope.data) { "Null data in tax report envelope" }
            ApiResponse.Success(data.toDomain())
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }

    override suspend fun getTaxSummary(year: Int, month: Int): ApiResponse<TaxMonthlySummary> {
        return try {
            val envelope: ApiEnvelope<TaxMonthlySummaryDto> =
                httpClient.get("tax/summary/$year/$month").body()
            val error = envelope.error
            if (error != null) return ApiResponse.Error(error.code, error.message, error.errorCode)
            val data = checkNotNull(envelope.data) { "Null data in tax summary envelope" }
            ApiResponse.Success(data.toDomain())
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }

    override suspend fun getTaxRecords(year: Int): ApiResponse<List<TaxRecord>> {
        return try {
            val envelope: ApiEnvelope<List<TaxRecordDto>> =
                httpClient.get("tax/records/$year").body()
            val error = envelope.error
            if (error != null) return ApiResponse.Error(error.code, error.message, error.errorCode)
            val data = checkNotNull(envelope.data) { "Null data in tax records envelope" }
            ApiResponse.Success(data.map { it.toDomain() })
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }

    override suspend fun getTaxReportUrl(year: Int): ApiResponse<String> {
        return try {
            val envelope: ApiEnvelope<TaxReportUrlDto> =
                httpClient.get("tax/report-url/$year").body()
            val error = envelope.error
            if (error != null) return ApiResponse.Error(error.code, error.message, error.errorCode)
            val data = checkNotNull(envelope.data) { "Null data in tax report URL envelope" }
            ApiResponse.Success(data.url)
        } catch (e: ClientRequestException) {
            ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error")
        }
    }
}
