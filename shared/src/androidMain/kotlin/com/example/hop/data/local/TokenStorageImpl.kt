package com.example.hop.data.local

import com.example.hop.network.TokenStorage

/**
 * Android implementation of [TokenStorage] backed by EncryptedSharedPreferences.
 *
 * TODO: Add dependency to shared/build.gradle.kts androidMain:
 *   implementation("androidx.security:security-crypto:1.1.0-alpha06")
 *
 * TODO: Inject [android.content.Context] (via Koin androidContext()) and initialise prefs:
 *   private val prefs = EncryptedSharedPreferences.create(
 *       context,
 *       "hop_secure_prefs",
 *       MasterKey.Builder(context)
 *           .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
 *           .build(),
 *       EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
 *       EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
 *   )
 */
class TokenStorageImpl : TokenStorage {

    override suspend fun getAccessToken(): String? {
        TODO("Read key ACCESS_TOKEN from EncryptedSharedPreferences")
    }

    override suspend fun saveAccessToken(token: String) {
        TODO("Write key ACCESS_TOKEN to EncryptedSharedPreferences")
    }

    override suspend fun getRefreshToken(): String? {
        TODO("Read key REFRESH_TOKEN from EncryptedSharedPreferences")
    }

    override suspend fun saveRefreshToken(token: String) {
        TODO("Write key REFRESH_TOKEN to EncryptedSharedPreferences")
    }

    override suspend fun clearTokens() {
        TODO("Remove both ACCESS_TOKEN and REFRESH_TOKEN from EncryptedSharedPreferences")
    }

    private companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
    }
}
