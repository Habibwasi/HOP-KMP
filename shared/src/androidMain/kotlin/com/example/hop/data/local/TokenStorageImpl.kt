package com.example.hop.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.hop.network.TokenStorage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of [TokenStorage] backed by EncryptedSharedPreferences.
 *
 * [getAccessToken] reads from the live Supabase session so that the WebSocket
 * and any other caller always receives a valid JWT without needing an explicit
 * save step after login.
 */
class TokenStorageImpl(context: Context, private val supabase: SupabaseClient) : TokenStorage {

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

    override suspend fun getAccessToken(): String? =
        supabase.auth.currentSessionOrNull()?.accessToken

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

    private companion object {
        const val PREFS_NAME = "hop_secure_prefs"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_RECOVERY_PENDING = "recovery_pending"
    }
}
