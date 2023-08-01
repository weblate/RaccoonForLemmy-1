package com.github.diegoberaldin.raccoonforlemmy.feature_profile.content.logged.posts

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core_architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core_architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain_identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain_lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain_lemmy.repository.PostsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain_lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class ProfilePostsViewModel(
    private val mvi: DefaultMviModel<ProfilePostsMviModel.Intent, ProfilePostsMviModel.UiState, ProfilePostsMviModel.Effect>,
    private val user: UserModel,
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository,
) : ScreenModel,
    MviModel<ProfilePostsMviModel.Intent, ProfilePostsMviModel.UiState, ProfilePostsMviModel.Effect> by mvi {

    private var currentPage: Int = 1
    override fun onStarted() {
        mvi.onStarted()
        refresh()
    }

    override fun reduce(intent: ProfilePostsMviModel.Intent) {
        when (intent) {
            ProfilePostsMviModel.Intent.LoadNextPage -> loadNextPage()
            ProfilePostsMviModel.Intent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        currentPage = 1
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            return
        }

        mvi.scope.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value
            val refreshing = currentState.refreshing
            val postList = userRepository.getUserPosts(
                auth = auth,
                id = user.id,
                page = currentPage,
            )
            currentPage++
            val canFetchMore = postList.size >= PostsRepository.DEFAULT_PAGE_SIZE
            mvi.updateState {
                val newPosts = if (refreshing) {
                    postList
                } else {
                    it.posts + postList
                }
                it.copy(
                    posts = newPosts,
                    loading = false,
                    canFetchMore = canFetchMore,
                    refreshing = false,
                )
            }
        }
    }
}
