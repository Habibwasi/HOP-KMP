package com.example.hop.data.dto

import com.example.hop.domain.model.DemandHotspot
import com.example.hop.domain.model.EarningsPoint
import kotlinx.serialization.Serializable

@Serializable
data class EarningsPointDto(
    val date: String = "",
    val earningsOere: Int = 0,
)

@Serializable
data class EarningsSeriesDto(
    val series: List<EarningsPointDto> = emptyList(),
    val totalOere: Int = 0,
)

@Serializable
data class DemandHotspotDto(
    val areaName: String = "",
    val demandCount: Int = 0,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class DemandHotspotsDto(
    val hotspots: List<DemandHotspotDto> = emptyList(),
)

@Serializable
data class Co2SavedDto(val co2SavedKg: Int = 0)

fun EarningsPointDto.toDomain(): EarningsPoint =
    EarningsPoint(date = date, earningsOere = earningsOere)

fun DemandHotspotDto.toDomain(): DemandHotspot = DemandHotspot(
    areaName = areaName,
    demandCount = demandCount,
    lat = lat,
    lng = lng,
)
