package com.example.hop.domain.model

data class SearchAlert(
    val id: String,
    val origin: String,
    val dest: String,
    val seats: Int,
    val createdAt: String,
)
