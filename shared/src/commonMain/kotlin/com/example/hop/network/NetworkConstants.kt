package com.example.hop.network

object NetworkConstants {
    const val PRODUCTION_BASE_URL = "https://api.hop.dk/v1"
    const val STAGING_BASE_URL = "https://staging-api.hop.dk/v1"

    // Android emulator: 10.0.2.2 routes to host machine localhost
    const val LOCAL_ANDROID_BASE_URL = "http://10.0.2.2:3000/api/v1"
    // Physical device on same network for MAC
    const val LOCAL_DEVICE_BASE_URL = "http://172.20.10.2:3000/api/v1"

    const val REQUEST_TIMEOUT_MS = 30_000L
    const val CONNECT_TIMEOUT_MS = 10_000L
    const val SOCKET_TIMEOUT_MS = 30_000L
}