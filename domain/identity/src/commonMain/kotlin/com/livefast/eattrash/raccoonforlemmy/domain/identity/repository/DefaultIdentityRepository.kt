package com.livefast.eattrash.raccoonforlemmy.domain.identity.repository

import com.livefast.eattrash.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.livefast.eattrash.raccoonforlemmy.core.utils.network.NetworkManager
import com.livefast.eattrash.raccoonforlemmy.domain.lemmy.data.UserModel
import com.livefast.eattrash.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single
internal class DefaultIdentityRepository(
    private val accountRepository: AccountRepository,
    private val siteRepository: SiteRepository,
    private val networkManager: NetworkManager,
) : IdentityRepository {
    override val authToken = MutableStateFlow<String?>(null)
    override val isLogged = MutableStateFlow<Boolean?>(null)
    override var cachedUser: UserModel? = null
        private set

    override suspend fun startup() =
        withContext(Dispatchers.IO) {
            val account = accountRepository.getActive()
            if (account != null) {
                authToken.value = account.jwt
            } else {
                authToken.value = ""
            }
            refreshLoggedState()
        }

    override fun storeToken(jwt: String) {
        authToken.value = jwt
    }

    override fun clearToken() {
        authToken.value = ""
        cachedUser = null
        isLogged.value = false
    }

    override suspend fun refreshLoggedState() =
        withContext(Dispatchers.IO) {
            val auth = authToken.value.orEmpty()
            isLogged.value = null
            if (auth.isNotEmpty()) {
                val newIsLogged =
                    if (networkManager.isNetworkAvailable()) {
                        refreshCachedUser(auth)
                        cachedUser != null
                    } else {
                        null
                    }
                isLogged.value = newIsLogged
            } else {
                isLogged.value = false
            }
        }

    private suspend fun refreshCachedUser(auth: String) {
        val remoteUser = siteRepository.getCurrentUser(auth)
        cachedUser = remoteUser
    }
}
