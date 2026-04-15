package com.example.hop.data.dto

import com.example.hop.domain.repository.TaxReportSummary
import kotlinx.serialization.Serializable

// ── Tax DTOs ──────────────────────────────────────────────────────────────────

@Serializable
data class TaxReportSummaryDto(
    val year: Int,
    val totalEarningsOere: Int,
    val totalDeductionOere: Int,
    val totalTaxableOere: Int,
    val presignedUrl: String,
)

fun TaxReportSummaryDto.toDomain(): TaxReportSummary = TaxReportSummary(
    year = year,
    totalEarningsOere = totalEarningsOere,
    totalDeductionOere = totalDeductionOere,
    totalTaxableOere = totalTaxableOere,
    presignedUrl = presignedUrl,
)

// ── Passenger Summary DTO (used by GET /trips/:id/bookings) ───────────────────

@Serializable
data class PassengerSummaryDto(
    val bookingId: String,
    val passengerId: String,
    val fullName: String,
    val rating: Float = 0f,
    val seats: Int,
)
