package com.livefast.eattrash.raccoonforlemmy.core.persistence.repository

import com.livefast.eattrash.raccoonforlemmy.core.preferences.store.TemporaryKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultUserSortRepository(
    private val keyStore: TemporaryKeyStore,
    private val serializer: SortSerializer,
) : UserSortRepository {
    override suspend fun getForPosts(handle: String): Int? =
        withContext(Dispatchers.IO) {
            val map =
                keyStore.get(POST_SETTINGS_KEY, listOf()).let {
                    serializer.deserializeMap(it)
                }
            map[handle]
        }

    override suspend fun getForComments(handle: String): Int? =
        withContext(Dispatchers.IO) {
            val map =
                keyStore.get(COMMENT_SETTINGS_KEY, listOf()).let {
                    serializer.deserializeMap(it)
                }
            map[handle]
        }

    override suspend fun saveForPosts(
        handle: String,
        value: Int,
    ) = withContext(Dispatchers.IO) {
        val map =
            keyStore.get(POST_SETTINGS_KEY, listOf()).let {
                serializer.deserializeMap(it)
            }
        map[handle] = value
        val newValue = serializer.serializeMap(map)
        keyStore.save(POST_SETTINGS_KEY, newValue)
    }

    override suspend fun saveForComments(
        handle: String,
        value: Int,
    ) = withContext(Dispatchers.IO) {
        val map =
            keyStore.get(COMMENT_SETTINGS_KEY, listOf()).let {
                serializer.deserializeMap(it)
            }
        map[handle] = value
        val newValue = serializer.serializeMap(map)
        keyStore.save(COMMENT_SETTINGS_KEY, newValue)
    }

    override suspend fun clear() =
        withContext(Dispatchers.IO) {
            keyStore.remove(POST_SETTINGS_KEY)
            keyStore.remove(COMMENT_SETTINGS_KEY)
        }

    companion object {
        private const val POST_SETTINGS_KEY = "userPostSort"
        private const val COMMENT_SETTINGS_KEY = "userCommentSort"
    }
}