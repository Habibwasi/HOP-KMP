package com.example.hop.domain.repository

import com.example.hop.domain.model.TaxMonthlySummary
import com.example.hop.domain.model.TaxRecord
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
    /** DR-14: full annual summary + presigned PDF URL. */
    suspend fun getTaxReport(year: Int): ApiResponse<TaxReportSummary>

    /** DR-13: monthly summary (estimated tax, gross, befordringsfradrag). */
    suspend fun getTaxSummary(year: Int, month: Int): ApiResponse<TaxMonthlySummary>

    /** DR-13: all tax ledger records for the given year. */
    suspend fun getTaxRecords(year: Int): ApiResponse<List<TaxRecord>>

    /** DR-13: presigned URL for the annual PDF report. */
    suspend fun getTaxReportUrl(year: Int): ApiResponse<String>
}
