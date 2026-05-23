package com.example.hop.domain.model

data class EarningsPoint(
    val date: String,
    val earningsOere: Int,
)

data class DemandHotspot(
    val areaName: String,
    val demandCount: Int,
    val lat: Double? = null,
    val lng: Double? = null,
)
