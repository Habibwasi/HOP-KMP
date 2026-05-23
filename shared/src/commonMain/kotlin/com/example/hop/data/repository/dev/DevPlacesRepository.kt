package com.example.hop.data.repository.dev

import com.example.hop.domain.model.SavedPlace
import com.example.hop.domain.model.SavedPlaceKind
import com.example.hop.domain.repository.GeocodeResult
import com.example.hop.domain.repository.PlacesRepository
import com.example.hop.network.ApiResponse

class DevPlacesRepository : PlacesRepository {

    private val places = mutableListOf(
        SavedPlace(id = "place-1", label = "Home", address = "Nørrebrogade 1, Copenhagen", lat = 55.6897, lng = 12.5490, kind = SavedPlaceKind.HOME),
        SavedPlace(id = "place-2", label = "Work", address = "Ørestad Boulevard 5, Copenhagen", lat = 55.6291, lng = 12.5768, kind = SavedPlaceKind.WORK),
    )

    override suspend fun list(): ApiResponse<List<SavedPlace>> =
        ApiResponse.Success(places.toList())

    override suspend fun upsert(
        label: String,
        address: String,
        lat: Double?,
        lng: Double?,
        kind: SavedPlaceKind?,
    ): ApiResponse<SavedPlace> {
        val existing = places.indexOfFirst { it.label == label }
        val updated = SavedPlace(
            id = if (existing >= 0) places[existing].id else "place-${places.size + 1}",
            label = label,
            address = address,
            lat = lat,
            lng = lng,
            kind = kind ?: SavedPlaceKind.CUSTOM,
        )
        if (existing >= 0) places[existing] = updated else places.add(updated)
        return ApiResponse.Success(updated)
    }

    override suspend fun delete(id: String): ApiResponse<Unit> {
        places.removeAll { it.id == id }
        return ApiResponse.Success(Unit)
    }

    override suspend fun geocode(address: String): ApiResponse<GeocodeResult> =
        ApiResponse.Success(GeocodeResult(lat = 55.6761, lng = 12.5683, formattedAddress = address))
}
