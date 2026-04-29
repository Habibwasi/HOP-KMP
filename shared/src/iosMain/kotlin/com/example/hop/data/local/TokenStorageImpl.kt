package com.example.hop.data.local

import com.example.hop.network.TokenStorage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
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
 *
 * ## Kotlin/Native CF-bridging notes
 *
 * Two toll-free bridging problems must be solved in this class:
 *
 * **CF → ObjC (keys):** Security constants like `kSecClass` are typed as
 * `CFStringRef = CPointer<__CFString>`. A plain `as NSString` cast throws
 * `TypeCastException` at runtime. [nsKey] uses `interpretObjCPointer(rawValue)` —
 * the canonical K/N toll-free bridge from CF to ObjC — to produce a genuine
 * `NSString` wrapper at the same memory address.
 *
 * **ObjC → CF (query dicts):** Security functions expect `CFDictionaryRef =
 * CPointer<__CFDictionary>`. A plain `nsDict as CFDictionaryRef` throws
 * `ClassCastException` at runtime because K/N refuses to cast an ObjC-object
 * wrapper (`NSDictionaryAsKMap`) to a `CPointer`. [withCFDict] uses
 * `CFBridgingRetain(nsDict)` to obtain a `CPointer<out CPointed>` for the same
 * object (ObjC → CF direction), then narrows via an `@Suppress("UNCHECKED_CAST")`
 * cast that is a no-op at runtime since `CPointer<T>` erases its type parameter.
 * `CFRelease` in the `finally` block balances the extra retain.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class TokenStorageImpl : TokenStorage {

    override suspend fun getAccessToken(): String? = read(KEY_ACCESS_TOKEN)

    override suspend fun saveAccessToken(token: String) = upsert(KEY_ACCESS_TOKEN, token)

    override suspend fun getRefreshToken(): String? = read(KEY_REFRESH_TOKEN)

    override suspend fun saveRefreshToken(token: String) = upsert(KEY_REFRESH_TOKEN, token)

    override suspend fun clearTokens() {
        delete(KEY_ACCESS_TOKEN)
        delete(KEY_REFRESH_TOKEN)
        delete(KEY_RECOVERY_PENDING)
    }

    override suspend fun saveRecoveryPending(pending: Boolean) {
        upsert(KEY_RECOVERY_PENDING, pending.toString())
    }

    override suspend fun getRecoveryPending(): Boolean {
        return read(KEY_RECOVERY_PENDING) == "true"
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * Toll-free bridges a Security/CoreFoundation CFStringRef constant to NSString
     * for use as an NSMutableDictionary key.
     *
     * `interpretObjCPointer(rawValue)` reinterprets the raw native pointer as an ObjC
     * object reference without a runtime type check — safe for toll-free bridged pairs
     * where the underlying bit pattern is identical.
     */
    private fun CFStringRef?.nsKey(): NSString =
        interpretObjCPointer(this!!.rawValue)

    /**
     * Bridges [this] NSMutableDictionary to a [CFDictionaryRef] for the duration of
     * [block], then releases the extra CF retain introduced by [CFBridgingRetain].
     *
     * `CFBridgingRetain` returns `CPointer<out CPointed>` (i.e. `CFTypeRef`).
     * The narrowing cast to `CFDictionaryRef = CPointer<__CFDictionary>` is flagged
     * as `UNCHECKED_CAST` at compile time and is a no-op at runtime since K/N erases
     * `CPointer<T>`'s type parameter. Both sides reference the same memory address.
     */
    @Suppress("UNCHECKED_CAST")
    private inline fun <R> NSMutableDictionary.withCFDict(block: (CFDictionaryRef) -> R): R {
        val retained = CFBridgingRetain(this)   // ObjC → CF, bumps retain count
        val cfRef = retained as CFDictionaryRef
        return try {
            block(cfRef)
        } finally {
            CFRelease(retained)                 // balances CFBridgingRetain
        }
    }

    private fun read(account: String): String? {
        val query = baseQuery(account).apply {
            setObject(true, forKey = kSecReturnData.nsKey())
            setObject(kSecMatchLimitOne!!, forKey = kSecMatchLimit.nsKey())
        }
        return query.withCFDict { cfQuery ->
            memScoped {
                val resultRef = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(cfQuery, resultRef.ptr)
                if (status == errSecSuccess) {
                    // CFBridgingRelease transfers CF ownership to ARC — safe bridge
                    // from COpaquePointer back to a typed ObjC object.
                    val nsData = CFBridgingRelease(resultRef.value) as? NSData
                        ?: return@memScoped null
                    NSString.create(data = nsData, encoding = NSUTF8StringEncoding) as? String
                } else {
                    null
                }
            }
        }
    }

    private fun upsert(account: String, value: String) {
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return

        val addQuery = baseQuery(account).apply {
            // kSecAttrAccessible is only valid in the ADD dictionary, not in queries.
            setObject(kSecAttrAccessibleWhenUnlockedThisDeviceOnly!!, forKey = kSecAttrAccessible.nsKey())
            setObject(data, forKey = kSecValueData.nsKey())
        }
        val status = addQuery.withCFDict { SecItemAdd(it, null) }

        if (status == errSecDuplicateItem) {
            // Key already exists — update the stored value only.
            val searchQuery = baseQuery(account)
            val updateDict = NSMutableDictionary().apply {
                setObject(data, forKey = kSecValueData.nsKey())
            }
            searchQuery.withCFDict { cfSearch ->
                updateDict.withCFDict { cfUpdate ->
                    SecItemUpdate(cfSearch, cfUpdate)
                }
            }
        }
    }

    private fun delete(account: String) {
        baseQuery(account).withCFDict { SecItemDelete(it) }
    }

    /**
     * Builds a base Keychain **search** query for a generic-password item identified
     * by [kSecAttrService] = [SERVICE] and [kSecAttrAccount] = [account].
     *
     * [kSecAttrAccessible] is intentionally **excluded** — Apple docs prohibit it in
     * search/read/delete queries; it is only added in the insert path of [upsert].
     *
     * CFStringRef constants are bridged to NSString keys via [nsKey].
     */
    private fun baseQuery(account: String): NSMutableDictionary = NSMutableDictionary().apply {
        setObject(kSecClassGenericPassword!!, forKey = kSecClass.nsKey())
        setObject(SERVICE, forKey = kSecAttrService.nsKey())
        setObject(account, forKey = kSecAttrAccount.nsKey())
    }

    private companion object {
        const val SERVICE = "com.example.hop"
        const val KEY_ACCESS_TOKEN = "hop_access_token"
        const val KEY_REFRESH_TOKEN = "hop_refresh_token"
        const val KEY_RECOVERY_PENDING = "hop_recovery_pending"
    }
}
