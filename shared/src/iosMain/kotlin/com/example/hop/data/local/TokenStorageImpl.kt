package com.example.hop.data.local

import com.example.hop.network.TokenStorage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.NSData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecDuplicateItem
import platform.Security.errSecSuccess
import platform.Foundation.CFBridgingRelease
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * iOS implementation of [TokenStorage] backed by the system Keychain.
 *
 * Tokens are stored as generic-password items under the [SERVICE] service label with
 * [kSecAttrAccessibleWhenUnlockedThisDeviceOnly] — items are never synced to iCloud
 * Keychain and can only be read while the device is unlocked.
 */
@OptIn(ExperimentalForeignApi::class)
class TokenStorageImpl : TokenStorage {

    override suspend fun getAccessToken(): String? = read(KEY_ACCESS_TOKEN)

    override suspend fun saveAccessToken(token: String) = upsert(KEY_ACCESS_TOKEN, token)

    override suspend fun getRefreshToken(): String? = read(KEY_REFRESH_TOKEN)

    override suspend fun saveRefreshToken(token: String) = upsert(KEY_REFRESH_TOKEN, token)

    override suspend fun clearTokens() {
        delete(KEY_ACCESS_TOKEN)
        delete(KEY_REFRESH_TOKEN)
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun read(account: String): String? {
        val query = baseQuery(account).apply {
            // Return the raw data bytes
            setObject(true, forKey = kSecReturnData as NSString)
            // Return only one match
            setObject(kSecMatchLimitOne, forKey = kSecMatchLimit as NSString)
        }
        return memScoped {
            val resultRef = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, resultRef.ptr)
            if (status == errSecSuccess) {
                // CFBridgingRelease transfers CF ownership to ARC and returns a properly
                // typed ObjC reference — COpaquePointer cannot be cast directly to NSData.
                val nsData = CFBridgingRelease(resultRef.value) as? NSData ?: return@memScoped null
                NSString.create(data = nsData, encoding = NSUTF8StringEncoding) as? String
            } else {
                null
            }
        }
    }

    private fun upsert(account: String, value: String) {
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return

        val addQuery = baseQuery(account).apply {
            // kSecAttrAccessible is only valid in the ADD dictionary, not in search queries.
            setObject(
                kSecAttrAccessibleWhenUnlockedThisDeviceOnly!!,
                forKey = kSecAttrAccessible as NSString,
            )
            setObject(data, forKey = kSecValueData as NSString)
        }
        val status = SecItemAdd(addQuery as CFDictionaryRef, null)

        if (status == errSecDuplicateItem) {
            // Key already exists — update the stored value only
            val searchQuery = baseQuery(account)
            val update = NSMutableDictionary().apply {
                setObject(data, forKey = kSecValueData as NSString)
            }
            SecItemUpdate(searchQuery as CFDictionaryRef, update as CFDictionaryRef)
        }
    }

    private fun delete(account: String) {
        SecItemDelete(baseQuery(account) as CFDictionaryRef)
    }

    /**
     * Builds a base Keychain **search** query for a generic-password item identified by
     * [kSecAttrService] = [SERVICE] and [kSecAttrAccount] = [account].
     *
     * [kSecAttrAccessible] is intentionally **excluded** here — Apple docs prohibit it
     * in search/read/delete dictionaries. It is only added in the insert path of [upsert].
     *
     * CFStringRef keys are toll-free bridged with NSString, so the cast is safe.
     */
    @Suppress("UNCHECKED_CAST")
    private fun baseQuery(account: String): NSMutableDictionary = NSMutableDictionary().apply {
        setObject(kSecClassGenericPassword!!, forKey = kSecClass as NSString)
        setObject(SERVICE, forKey = kSecAttrService as NSString)
        setObject(account, forKey = kSecAttrAccount as NSString)
    }

    private companion object {
        const val SERVICE = "com.example.hop"
        const val KEY_ACCESS_TOKEN = "hop_access_token"
        const val KEY_REFRESH_TOKEN = "hop_refresh_token"
    }
}
