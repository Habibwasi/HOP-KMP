package com.example.hop.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OtpPurpose {
    @SerialName("PHONE_VERIFY") PHONE_VERIFY,
    @SerialName("LOGIN") LOGIN,
}
