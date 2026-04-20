package com.example.hop.data.repository.dev

import com.example.hop.domain.model.TaxMonthlySummary
import com.example.hop.domain.model.TaxRecord
import com.example.hop.domain.repository.TaxReportSummary
import com.example.hop.domain.repository.TaxRepository
import com.example.hop.network.ApiResponse

class DevTaxRepository : TaxRepository {

    override suspend fun getTaxReport(year: Int): ApiResponse<TaxReportSummary> =
        ApiResponse.Success(
            TaxReportSummary(
                year = year,
                totalEarningsOere = 250_000,
                totalDeductionOere = 45_000,
                totalTaxableOere = 205_000,
                presignedUrl = "https://example.com/dev-tax-report.pdf",
            )
        )

    override suspend fun getTaxSummary(year: Int, month: Int): ApiResponse<TaxMonthlySummary> =
        ApiResponse.Success(
            TaxMonthlySummary(
                year = year,
                month = month,
                grossOere = 22_000,
                befordringsfradragOere = 4_200,
                taxableOere = 17_800,
                estimatedTaxOere = 6_500,
            )
        )

    override suspend fun getTaxRecords(year: Int): ApiResponse<List<TaxRecord>> =
        ApiResponse.Success(
            listOf(
                TaxRecord(
                    id = "dev-tax-1",
                    driverId = DevAuthRepository.DEV_USER.id,
                    tripId = "dev-trip-1",
                    bookingId = "dev-booking-1",
                    distanceMetres = 187_000,
                    grossOere = 15_000,
                    skatRateOere = 228,
                    deductionOere = 4_264,
                    taxableOere = 10_736,
                    tripDate = "2026-04-15",
                    taxYear = year,
                ),
            )
        )

    override suspend fun getTaxReportUrl(year: Int): ApiResponse<String> =
        ApiResponse.Success("https://example.com/dev-tax-report-$year.pdf")
}
