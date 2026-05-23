package com.example.hop.domain.model

/**
 * A previously searched origin → destination pair, surfaced in the search
 * card as one-tap re-runs. Sorted server-side by `lastUsedAt DESC`.
 */
data class RecentSearch(
    val id: String,
    val originLabel: String,
    val destLabel: String,
    val useCount: Int,
)
