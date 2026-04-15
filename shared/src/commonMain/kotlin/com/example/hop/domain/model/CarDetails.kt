package com.example.hop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CarDetails(
    val make: String,
    val model: String,
    val year: Int,
    val licensePlate: String,
    val colour: String,
    val seatsAvailable: Int,
)
