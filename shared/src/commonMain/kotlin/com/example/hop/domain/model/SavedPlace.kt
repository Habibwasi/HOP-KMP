package com.example.hop.domain.model

enum class SavedPlaceKind { HOME, WORK, CUSTOM }

/**
 * A user's saved location ("Home", "Work", "Mum's house").
 * `lat`/`lng` are optional — chips work label-first; coordinates are populated
 * once a place has been resolved through geocoding.
 */
data class SavedPlace(
    val id: String,
    val label: String,
    val address: String,
    val lat: Double?,
    val lng: Double?,
    val kind: SavedPlaceKind,
)
