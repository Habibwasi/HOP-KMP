package com.example.hop.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TokenStorageImpl(context: Context) : TokenStorage {
    companion object {
        private const val PREFS_NAME = "token_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_RECOVERY_PENDING = "recovery_pending"
    }

    private val sharedPreferences: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setRequestStrongBoxBacked(false)
            .build()
        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun saveAccessToken(token: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun saveRefreshToken(token: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    override suspend fun clearTokens() = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_RECOVERY_PENDING)
            .apply()
    }

    override suspend fun saveRecoveryPending(pending: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(KEY_RECOVERY_PENDING, pending).apply()
    }

    override suspend fun getRecoveryPending(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(KEY_RECOVERY_PENDING, false)
    }
}
