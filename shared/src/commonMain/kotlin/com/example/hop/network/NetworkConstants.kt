package com.example.hop.network

object NetworkConstants {
    const val PRODUCTION_BASE_URL = "https://api.hop.dk/v1"
    const val STAGING_BASE_URL = "https://staging-api.hop.dk/v1"

    const val REQUEST_TIMEOUT_MS = 30_000L
    const val CONNECT_TIMEOUT_MS = 10_000L
    const val SOCKET_TIMEOUT_MS = 30_000L
}
