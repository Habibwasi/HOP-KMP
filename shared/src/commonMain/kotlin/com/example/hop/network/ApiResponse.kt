package com.example.hop.network

import io.ktor.client.plugins.ClientRequestException

sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val message: String) : ApiResponse<Nothing>()
}

suspend fun <T> safeApiCall(block: suspend () -> T): ApiResponse<T> = try {
    ApiResponse.Success(block())
} catch (e: ClientRequestException) {
    ApiResponse.Error(e.response.status.value, e.message ?: "Client error")
} catch (e: Exception) {
    ApiResponse.Error(-1, e.message ?: "Unknown error")
}
