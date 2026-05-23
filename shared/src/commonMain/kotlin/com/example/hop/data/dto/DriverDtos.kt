package com.example.hop.data.dto

import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.LicenceStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─ Request ────────────────────────────────────────────────────────────────────

@Serializable
data class SubmitLicenceRequestDto(
    @SerialName("make") val make: String,
    @SerialName("model") val model: String,
    @SerialName("year") val year: Int,
    @SerialName("license_plate") val licensePlate: String,
    @SerialName("colour") val colour: String,
    @SerialName("seats_available") val seatsAvailable: Int,
)

// ─ Response ───────────────────────────────────────────────────────────────────

@Serializable
data class LicenceStatusDto(
    @SerialName("status") val status: String,
)

// ─ Mapping ────────────────────────────────────────────────────────────────────

fun CarDetails.toSubmitLicenceRequestDto() = SubmitLicenceRequestDto(
    make = make,
    model = model,
    year = year,
    licensePlate = licensePlate,
    colour = colour,
    seatsAvailable = seatsAvailable,
)

fun LicenceStatusDto.toDomain(): LicenceStatus = when (status.uppercase()) {
    "NONE" -> LicenceStatus.NONE
    "PENDING" -> LicenceStatus.PENDING
    "APPROVED" -> LicenceStatus.APPROVED
    "REJECTED" -> LicenceStatus.REJECTED
    else -> LicenceStatus.UNKNOWN
}
