package com.github.diegoberaldin.raccoonforlemmy.unit.drawer

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.FavoriteCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.MultiCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield

class ModalDrawerViewModel(
    private val mvi: DefaultMviModel<ModalDrawerMviModel.Intent, ModalDrawerMviModel.UiState, ModalDrawerMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val accountRepository: AccountRepository,
    private val multiCommunityRepository: MultiCommunityRepository,
    private val siteRepository: SiteRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val settingsRepository: SettingsRepository,
    private val favoriteCommunityRepository: FavoriteCommunityRepository,
) : ModalDrawerMviModel,
    MviModel<ModalDrawerMviModel.Intent, ModalDrawerMviModel.UiState, ModalDrawerMviModel.Effect> by mvi {

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch(Dispatchers.Main) {
            apiConfigurationRepository.instance.onEach { instance ->
                mvi.updateState {
                    it.copy(instance = instance)
                }
            }.launchIn(this)
            identityRepository.isLogged.drop(1).debounce(250).onEach { _ ->
                refreshUser()
                refresh()
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState { it.copy(autoLoadImages = settings.autoLoadImages) }
            }.launchIn(this)

            observeChangesInFavoriteCommunities()

            mvi.scope?.launch {
                delay(250)
                refreshUser()
                refresh()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.observeChangesInFavoriteCommunities() {
        channelFlow {
            while (isActive) {
                val accountId = accountRepository.getActive()?.id
                trySend(accountId)
                delay(1000)
            }
        }.distinctUntilChanged().flatMapConcat { accountId ->
            channelFlow {
                while (isActive) {
                    val communityIds =
                        favoriteCommunityRepository.getAll(accountId).map { it.communityId }
                    trySend(communityIds)
                    delay(1000)
                }
            }.distinctUntilChanged()
        }.onEach { favoriteCommunityIds ->
            val newCommunities = uiState.value.communities.map { community ->
                community.copy(favorite = community.id in favoriteCommunityIds)
            }
                .sortedBy { it.name }
                .sortedByDescending { it.favorite }
            mvi.updateState { it.copy(communities = newCommunities) }
        }.launchIn(this)
    }

    override fun reduce(intent: ModalDrawerMviModel.Intent) {
        when (intent) {
            ModalDrawerMviModel.Intent.Refresh -> mvi.scope?.launch(Dispatchers.IO) {
                refresh()
            }

            is ModalDrawerMviModel.Intent.ChangeInstanceName -> mvi.updateState {
                it.copy(changeInstanceName = intent.value)
            }

            ModalDrawerMviModel.Intent.SubmitChangeInstance -> submitChangeInstance()
        }
    }

    private suspend fun refreshUser() {
        val auth = identityRepository.authToken.value.orEmpty()
        if (auth.isEmpty()) {
            mvi.updateState { it.copy(user = null) }
        } else {
            var user = siteRepository.getCurrentUser(auth)
            runCatching {
                withTimeout(2000) {
                    while (user == null) {
                        // retry getting user if non-empty auth
                        delay(500)
                        user = siteRepository.getCurrentUser(auth)
                        yield()
                    }
                    mvi.updateState { it.copy(user = user) }
                }
            }
        }
    }

    private suspend fun refresh() {
        if (uiState.value.refreshing) {
            return
        }
        mvi.updateState { it.copy(refreshing = true) }

        val auth = identityRepository.authToken.value
        val accountId = accountRepository.getActive()?.id
        val favoriteCommunityIds =
            favoriteCommunityRepository.getAll(accountId).map { it.communityId }
        val communities = communityRepository.getSubscribed(auth)
            .map { community ->
                community.copy(favorite = community.id in favoriteCommunityIds)
            }
            .sortedBy { it.name }
            .sortedByDescending { it.favorite }
        val multiCommunitites = multiCommunityRepository.getAll(accountId).sortedBy { it.name }

        mvi.updateState {
            it.copy(
                refreshing = false,
                communities = communities,
                multiCommunities = multiCommunitites,
            )
        }
    }

    private fun submitChangeInstance() {
        mvi.updateState { it.copy(changeInstanceNameError = null) }
        var valid = true
        val instanceName = uiState.value.changeInstanceName
        if (instanceName.isEmpty()) {
            mvi.updateState { it.copy(changeInstanceNameError = MR.strings.message_missing_field.desc()) }
            valid = false
        }
        if (!valid) {
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(changeInstanceloading = true) }
            val res = communityRepository.getAll(
                instance = instanceName,
                page = 1,
                limit = 1
            ) ?: emptyList()
            if (res.isEmpty()) {
                mvi.updateState {
                    it.copy(
                        changeInstanceNameError = MR.strings.message_invalid_field.desc(),
                        changeInstanceloading = false,
                    )
                }
                return@launch
            }

            apiConfigurationRepository.changeInstance(instanceName)
            mvi.updateState {
                it.copy(
                    changeInstanceloading = false,
                    changeInstanceName = "",
                )
            }
            mvi.emitEffect(ModalDrawerMviModel.Effect.CloseChangeInstanceDialog)
        }
    }
}