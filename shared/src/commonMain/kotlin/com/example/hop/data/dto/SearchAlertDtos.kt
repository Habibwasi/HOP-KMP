package com.example.hop.data.dto

import com.example.hop.domain.model.SearchAlert
import kotlinx.serialization.Serializable

@Serializable
data class SearchAlertDto(
    val id: String,
    val origin: String,
    val dest: String,
    val seats: Int = 1,
    val createdAt: String,
)

@Serializable
data class CreateSearchAlertRequest(
    val origin: String,
    val dest: String,
    val seats: Int,
)

fun SearchAlertDto.toDomain(): SearchAlert = SearchAlert(
    id = id,
    origin = origin,
    dest = dest,
    seats = seats,
    createdAt = createdAt,
)
