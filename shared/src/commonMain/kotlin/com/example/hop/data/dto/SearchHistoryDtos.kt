package com.example.hop.data.dto

import com.example.hop.domain.model.RecentSearch
import kotlinx.serialization.Serializable

@Serializable
data class RecentSearchDto(
    val id: String,
    val originLabel: String,
    val destLabel: String,
    val useCount: Int = 1,
)

@Serializable
data class RecordSearchRequest(
    val originLabel: String,
    val destLabel: String,
)

fun RecentSearchDto.toDomain(): RecentSearch = RecentSearch(
    id = id,
    originLabel = originLabel,
    destLabel = destLabel,
    useCount = useCount,
)
