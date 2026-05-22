package com.example.hop.network

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val message: String, val errorCode: String? = null) : ApiResponse<Nothing>()

    companion object {
        /** Returned when the device has no network or the connection was refused. */
        const val CODE_NO_NETWORK = -2

        /** Returned when the Ktor request timeout fires (30 s by default). */
        const val CODE_TIMEOUT = -3
    }
}

suspend fun <T> safeApiCall(block: suspend () -> T): ApiResponse<T> = try {
    ApiResponse.Success(block())
} catch (e: HttpRequestTimeoutException) {
    // Ktor request-timeout plugin fired — distinct from a server error.
    ApiResponse.Error(ApiResponse.CODE_TIMEOUT, "Connection timed out. Please check your network and try again.")
} catch (e: ServerResponseException) {
    // 5xx response — server-side fault, not the user's.
    ApiResponse.Error(e.response.status.value, "Server error (${e.response.status.value}). Please try again later.")
} catch (e: ClientRequestException) {
    // 4xx response — parse the structured error body to extract errorCode.
    try {
        val body = e.response.bodyAsText()
        val json = Json { ignoreUnknownKeys = true }.parseToJsonElement(body).jsonObject
        val errorObj = json["error"]?.jsonObject
        val errorCode = errorObj?.get("errorCode")?.jsonPrimitive?.contentOrNull
        val msg = errorObj?.get("message")?.jsonPrimitive?.contentOrNull
            ?: (e.message ?: "Request error")
        ApiResponse.Error(e.response.status.value, msg, errorCode)
    } catch (_: Exception) {
        ApiResponse.Error(e.response.status.value, e.message ?: "Request error")
    }
} catch (e: Exception) {
    // Catch-all: differentiate common network-layer failures from unexpected bugs
    // so the UI can show a meaningful message instead of a raw exception dump.
    // We check class name rather than importing platform-specific IOException
    // types so this compiles in commonMain for all targets (JVM, iOS, JS).
    val isNetworkError = e::class.simpleName?.contains("IOException") == true
        || e.message?.contains("Unable to resolve host", ignoreCase = true) == true
        || e.message?.contains("No address associated with hostname", ignoreCase = true) == true
        || e.message?.contains("Failed to connect", ignoreCase = true) == true
        || e.message?.contains("Connection refused", ignoreCase = true) == true
        || e.message?.contains("Network is unreachable", ignoreCase = true) == true
        || e.message?.contains("Software caused connection abort", ignoreCase = true) == true
    if (isNetworkError) {
        ApiResponse.Error(ApiResponse.CODE_NO_NETWORK, "No internet connection. Please check your network and try again.")
    } else {
        ApiResponse.Error(-1, e.message ?: "Unknown error")
    }
}
