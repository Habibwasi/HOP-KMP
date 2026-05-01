package com.example.hop.data.dto

import com.example.hop.domain.model.SavedPlace
import com.example.hop.domain.model.SavedPlaceKind
import com.example.hop.domain.repository.GeocodeResult
import kotlinx.serialization.Serializable

@Serializable
data class SavedPlaceDto(
    val id: String,
    val label: String,
    val address: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val kind: String = "CUSTOM",
)

@Serializable
data class UpsertPlaceRequest(
    val label: String,
    val address: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val kind: String? = null,
)

@Serializable
data class GeocodeResultDto(
    val lat: Double,
    val lng: Double,
    val formattedAddress: String,
)

fun SavedPlaceDto.toDomain(): SavedPlace = SavedPlace(
    id = id,
    label = label,
    address = address,
    lat = lat,
    lng = lng,
    kind = runCatching { SavedPlaceKind.valueOf(kind.uppercase()) }
        .getOrDefault(SavedPlaceKind.CUSTOM),
)

fun GeocodeResultDto.toDomain(): GeocodeResult = GeocodeResult(
    lat = lat,
    lng = lng,
    formattedAddress = formattedAddress,
)
