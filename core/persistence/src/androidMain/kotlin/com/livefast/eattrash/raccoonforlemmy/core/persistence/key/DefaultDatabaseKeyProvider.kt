package com.livefast.eattrash.raccoonforlemmy.core.persistence.key

import android.util.Base64
import com.livefast.eattrash.raccoonforlemmy.core.preferences.store.TemporaryKeyStore
import org.koin.core.annotation.Single
import java.security.SecureRandom

@Single
internal actual class DefaultDatabaseKeyProvider(
    private val keyStore: TemporaryKeyStore,
) : DatabaseKeyProvider {
    actual override fun getKey(): ByteArray {
        val savedKey = retrieveStoreKey()
        return if (savedKey.isEmpty()) {
            val key = generateKey()
            val keyString = encodeToString(key)
            storeKey(keyString)
            key
        } else {
            val res = decodeFromString(savedKey)
            if (res.isNotEmpty()) {
                res
            } else {
                // regenerates the key
                val key = generateKey()
                val keyString = encodeToString(key)
                storeKey(keyString)
                key
            }
        }
    }

    actual override fun removeKey() {
        keyStore.remove(DATABASE_KEY)
    }

    private fun retrieveStoreKey(): String = keyStore[DATABASE_KEY, ""]

    private fun storeKey(key: String) = keyStore.save(DATABASE_KEY, key)

    private fun generateKey(): ByteArray {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        return key
    }

    private fun encodeToString(key: ByteArray): String = Base64.encodeToString(key, Base64.DEFAULT)

    private fun decodeFromString(key: String): ByteArray = Base64.decode(key, Base64.DEFAULT)

    companion object {
        private const val DATABASE_KEY = "database_key"
    }
}
