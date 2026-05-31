package com.example.hop.data.dto

import com.example.hop.domain.model.FaqItem
import kotlinx.serialization.Serializable

@Serializable
data class FaqItemDto(
    val id: String = "",
    val topic: String = "",
    val question: String = "",
    val answer: String = "",
)

@Serializable
data class FaqListDto(
    val faqs: List<FaqItemDto> = emptyList(),
)

fun FaqItemDto.toDomain(): FaqItem = FaqItem(
    id = id,
    topic = topic,
    question = question,
    answer = answer,
)
