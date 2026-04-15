package com.example.hop.domain.repository

import com.example.hop.network.ApiResponse

/**
 * Tax report summary returned by `GET /tax/report/:year`.
 * All monetary amounts are in øre (1 DKK = 100 øre).
 */
data class TaxReportSummary(
    val year: Int,
    val totalEarningsOere: Int,
    val totalDeductionOere: Int,
    val totalTaxableOere: Int,
    /** Presigned S3 URL valid for a short window (backend enforced TTL). */
    val presignedUrl: String,
)

interface TaxRepository {
    suspend fun getTaxReport(year: Int): ApiResponse<TaxReportSummary>
}
