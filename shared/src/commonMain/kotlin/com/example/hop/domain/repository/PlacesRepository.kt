package com.example.hop.domain.repository

import com.example.hop.domain.model.SavedPlace
import com.example.hop.domain.model.SavedPlaceKind
import com.example.hop.network.ApiResponse

interface PlacesRepository {
    /** GET /places — all saved places for the current user. */
    suspend fun list(): ApiResponse<List<SavedPlace>>

    /** POST /places — upsert a place by `(userId, label)`. */
    suspend fun upsert(
        label: String,
        address: String,
        lat: Double? = null,
        lng: Double? = null,
        kind: SavedPlaceKind? = null,
    ): ApiResponse<SavedPlace>

    /** DELETE /places/:id */
    suspend fun delete(id: String): ApiResponse<Unit>
}
