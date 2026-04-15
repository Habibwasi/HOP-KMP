package com.example.hop.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class LicenceStatus {
    NONE,
    PENDING,
    APPROVED,
    REJECTED,
    UNKNOWN, // Fallback for version mismatch or API drift
}
