package com.example.hop.network

/**
 * Read-only token accessor used by [AuthInterceptor] to attach Bearer credentials.
 * Platform-specific implementations (Keychain on iOS, EncryptedSharedPreferences on Android)
 * are injected via Koin from their respective source sets.
 */
interface TokenStorage {
    suspend fun getAccessToken(): String?
    suspend fun saveAccessToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun saveRefreshToken(token: String)
    suspend fun clearTokens()
    /** Persists whether a password-reset email has been sent and the recovery link has not yet been handled. */
    suspend fun saveRecoveryPending(pending: Boolean)
    suspend fun getRecoveryPending(): Boolean
}
