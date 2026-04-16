package com.example.hop.data.dto

import com.example.hop.domain.model.TaxMonthlySummary
import com.example.hop.domain.model.TaxRecord
import com.example.hop.domain.repository.TaxReportSummary
import kotlinx.serialization.Serializable

// ── Annual report DTO ─────────────────────────────────────────────────────────

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

// ── Monthly summary DTO ───────────────────────────────────────────────────────

@Serializable
data class TaxMonthlySummaryDto(
    val year: Int,
    val month: Int,
    val grossOere: Int,
    val befordringsfradragOere: Int,
    val taxableOere: Int,
    val estimatedTaxOere: Int,
)

fun TaxMonthlySummaryDto.toDomain(): TaxMonthlySummary = TaxMonthlySummary(
    year = year,
    month = month,
    grossOere = grossOere,
    befordringsfradragOere = befordringsfradragOere,
    taxableOere = taxableOere,
    estimatedTaxOere = estimatedTaxOere,
)

// ── Tax record DTO ────────────────────────────────────────────────────────────

@Serializable
data class TaxRecordDto(
    val id: String,
    val driverId: String,
    val tripId: String,
    val bookingId: String,
    val distanceMetres: Int,
    val grossOere: Int,
    val skatRateOere: Int,
    val deductionOere: Int,
    val taxableOere: Int,
    val tripDate: String,
    val taxYear: Int,
)

fun TaxRecordDto.toDomain(): TaxRecord = TaxRecord(
    id = id,
    driverId = driverId,
    tripId = tripId,
    bookingId = bookingId,
    distanceMetres = distanceMetres,
    grossOere = grossOere,
    skatRateOere = skatRateOere,
    deductionOere = deductionOere,
    taxableOere = taxableOere,
    tripDate = tripDate,
    taxYear = taxYear,
)

// ── Report URL wrapper DTO ────────────────────────────────────────────────────

@Serializable
data class TaxReportUrlDto(val url: String)

// ── Passenger Summary DTO (used by GET /trips/:id/bookings) ───────────────────

@Serializable
data class PassengerSummaryDto(
    val bookingId: String,
    val passengerId: String,
    val fullName: String,
    val rating: Float = 0f,
    val seats: Int,
)
