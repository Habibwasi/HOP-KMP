package com.example.hop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TaxRecord(
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
