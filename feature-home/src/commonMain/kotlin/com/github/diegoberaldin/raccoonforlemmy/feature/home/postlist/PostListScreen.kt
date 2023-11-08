package com.github.diegoberaldin.raccoonforlemmy.feature.home.postlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Dimensions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.report.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.feature.home.di.getHomeScreenModel
import com.github.diegoberaldin.raccoonforlemmy.feature.home.ui.HomeTab
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostListScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getHomeScreenModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        val isFabVisible by fabNestedScrollConnection.isFabVisible.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
        val themeRepository = remember { getThemeRepository() }
        val upvoteColor by themeRepository.upvoteColor.collectAsState()
        val downvoteColor by themeRepository.downvoteColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        val lazyListState = rememberLazyListState()
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val scope = rememberCoroutineScope()
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()

        DisposableEffect(key) {
            onDispose {
                notificationCenter.removeObserver(key)
            }
        }
        LaunchedEffect(Unit) {
            navigationCoordinator.onDoubleTabSelection.onEach { tab ->
                if (tab == HomeTab) {
                    lazyListState.scrollToItem(0)
                    topAppBarState.heightOffset = 0f
                    topAppBarState.contentOffset = 0f
                }
            }.launchIn(this)
        }
        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    PostListMviModel.Effect.BackToTop -> {
                        lazyListState.scrollToItem(0)
                    }

                    is PostListMviModel.Effect.ZombieModeTick -> {
                        if (effect.index >= 0) {
                            lazyListState.animateScrollBy(settings.zombieModeScrollAmount)
                        }
                    }
                }
            }.launchIn(this)
        }
        LaunchedEffect(notificationCenter) {
            notificationCenter.addObserver(
                { result ->
                    (result as? ListingType)?.also {
                        model.reduce(PostListMviModel.Intent.ChangeListing(it))
                    }
                }, key, NotificationCenterContractKeys.ChangeFeedType
            )
            notificationCenter.addObserver(
                {
                    (it as? SortType)?.also { sortType ->
                        model.reduce(
                            PostListMviModel.Intent.ChangeSort(sortType)
                        )
                    }
                }, key, NotificationCenterContractKeys.ChangeSortType
            )
            notificationCenter.addObserver(
                {
                    model.reduce(PostListMviModel.Intent.Refresh)
                },
                key,
                NotificationCenterContractKeys.CommentCreated
            )
            notificationCenter.addObserver(
                {
                    model.reduce(PostListMviModel.Intent.Refresh)
                },
                key,
                NotificationCenterContractKeys.PostCreated
            )
        }

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                PostsTopBar(
                    currentInstance = uiState.instance,
                    listingType = uiState.listingType,
                    sortType = uiState.sortType,
                    scrollBehavior = scrollBehavior,
                    onHamburgerTapped = rememberCallback {
                        scope.launch {
                            drawerCoordinator.toggleDrawer()
                        }
                    },
                    onSelectListingType = rememberCallback {
                        val sheet = ListingTypeBottomSheet(
                            isLogged = uiState.isLogged,
                        )
                        navigationCoordinator.getBottomNavigator()?.show(sheet)
                    },
                    onSelectSortType = rememberCallback {
                        val sheet = SortBottomSheet(
                            expandTop = true,
                        )
                        navigationCoordinator.getBottomNavigator()?.show(sheet)
                    },
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = isFabVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it * 2 },
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it * 2 },
                    ),
                ) {
                    FloatingActionButtonMenu(
                        modifier = Modifier.padding(bottom = Dimensions.topBarHeight),
                        items = buildList {
                            if (uiState.zombieModeActive) {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.SyncDisabled,
                                    text = stringResource(MR.strings.action_deactivate_zombie_mode),
                                    onSelected = rememberCallback(model) {
                                        model.reduce(PostListMviModel.Intent.PauseZombieMode)
                                    },
                                )
                            } else {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.Sync,
                                    text = stringResource(MR.strings.action_activate_zombie_mode),
                                    onSelected = rememberCallback(model) {
                                        model.reduce(PostListMviModel.Intent.StartZombieMode(-1))
                                    },
                                )
                            }
                            this += FloatingActionButtonMenuItem(
                                icon = Icons.Default.ExpandLess,
                                text = stringResource(MR.strings.action_back_to_top),
                                onSelected = rememberCallback {
                                    scope.launch {
                                        lazyListState.scrollToItem(0)
                                        topAppBarState.heightOffset = 0f
                                        topAppBarState.contentOffset = 0f
                                    }
                                },
                            )
                            this += FloatingActionButtonMenuItem(
                                icon = Icons.Default.ClearAll,
                                text = stringResource(MR.strings.action_clear_read),
                                onSelected = rememberCallback {
                                    model.reduce(PostListMviModel.Intent.ClearRead)
                                    scope.launch {
                                        lazyListState.scrollToItem(0)
                                    }
                                },
                            )
                        }
                    )
                }
            }
        ) { padding ->
            if (uiState.currentUserId != null) {
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = uiState.refreshing,
                    onRefresh = rememberCallback(model) {
                        model.reduce(PostListMviModel.Intent.Refresh)
                    },
                )
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxWidth()
                        .let {
                            val connection = navigationCoordinator.getBottomBarScrollConnection()
                            if (connection != null && settings.hideNavigationBarWhileScrolling) {
                                it.nestedScroll(connection)
                            } else {
                                it
                            }
                        }
                        .let {
                            if (settings.hideNavigationBarWhileScrolling) {
                                it.nestedScroll(scrollBehavior.nestedScrollConnection)
                            } else {
                                it
                            }
                        }
                        .nestedScroll(fabNestedScrollConnection)
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState,
                        userScrollEnabled = !uiState.zombieModeActive,
                    ) {
                        if (uiState.posts.isEmpty() && uiState.loading) {
                            items(5) {
                                PostCardPlaceholder(
                                    postLayout = uiState.postLayout,
                                )
                                if (uiState.postLayout != PostLayout.Card) {
                                    Divider(modifier = Modifier.padding(vertical = Spacing.s))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.s))
                                }
                            }
                        }
                        items(uiState.posts, key = { it.id }) { post ->
                            SwipeableCard(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState.swipeActionsEnabled,
                                backgroundColor = rememberCallbackArgs {
                                    when (it) {
                                        DismissValue.DismissedToStart -> upvoteColor
                                            ?: defaultUpvoteColor

                                        DismissValue.DismissedToEnd -> downvoteColor
                                            ?: defaultDownVoteColor

                                        DismissValue.Default -> Color.Transparent
                                    }
                                },
                                onGestureBegin = {
                                    model.reduce(PostListMviModel.Intent.HapticIndication)
                                },
                                onDismissToStart = rememberCallback(model) {
                                    model.reduce(PostListMviModel.Intent.UpVotePost(post.id))
                                },
                                onDismissToEnd = rememberCallback(model) {
                                    model.reduce(PostListMviModel.Intent.DownVotePost(post.id))
                                },
                                swipeContent = { direction ->
                                    val icon = when (direction) {
                                        DismissDirection.StartToEnd -> Icons.Default.ArrowCircleDown
                                        DismissDirection.EndToStart -> Icons.Default.ArrowCircleUp
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                    )
                                },
                                content = {
                                    PostCard(
                                        post = post,
                                        postLayout = uiState.postLayout,
                                        fullHeightImage = uiState.fullHeightImages,
                                        separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                        autoLoadImages = uiState.autoLoadImages,
                                        blurNsfw = uiState.blurNsfw,
                                        onClick = rememberCallback(model) {
                                            model.reduce(PostListMviModel.Intent.MarkAsRead(post.id))
                                            navigationCoordinator.getRootNavigator()?.push(
                                                PostDetailScreen(post),
                                            )
                                        },
                                        onOpenCommunity = rememberCallbackArgs { community ->
                                            navigationCoordinator.getRootNavigator()?.push(
                                                CommunityDetailScreen(community),
                                            )
                                        },
                                        onOpenCreator = rememberCallbackArgs { user ->
                                            navigationCoordinator.getRootNavigator()?.push(
                                                UserDetailScreen(user),
                                            )
                                        },
                                        onUpVote = rememberCallback(model) {
                                            model.reduce(
                                                PostListMviModel.Intent.UpVotePost(
                                                    id = post.id,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onDownVote = rememberCallback(model) {
                                            model.reduce(
                                                PostListMviModel.Intent.DownVotePost(
                                                    id = post.id,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onSave = rememberCallback(model) {
                                            model.reduce(
                                                PostListMviModel.Intent.SavePost(
                                                    id = post.id,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onReply = rememberCallback(model) {
                                            val screen = CreateCommentScreen(
                                                originalPost = post,
                                            )
                                            navigationCoordinator.getBottomNavigator()?.show(screen)
                                        },
                                        onImageClick = rememberCallbackArgs(model, post) { url ->
                                            model.reduce(PostListMviModel.Intent.MarkAsRead(post.id))
                                            navigationCoordinator.getRootNavigator()?.push(
                                                ZoomableImageScreen(url)
                                            )
                                        },
                                        options = buildList {
                                            add(stringResource(MR.strings.post_action_share))
                                            add(stringResource(MR.strings.post_action_hide))
                                            add(stringResource(MR.strings.post_action_see_raw))
                                            add(stringResource(MR.strings.post_action_report))
                                            if (post.creator?.id == uiState.currentUserId) {
                                                add(stringResource(MR.strings.post_action_edit))
                                                add(stringResource(MR.strings.comment_action_delete))
                                            }
                                        },
                                        onOptionSelected = rememberCallbackArgs(model) { optionIdx ->
                                            when (optionIdx) {
                                                5 -> model.reduce(
                                                    PostListMviModel.Intent.DeletePost(post.id)
                                                )

                                                4 -> {
                                                    navigationCoordinator.getBottomNavigator()
                                                        ?.show(
                                                            CreatePostScreen(editedPost = post)
                                                        )
                                                }

                                                3 -> {
                                                    navigationCoordinator.getBottomNavigator()
                                                        ?.show(
                                                            CreateReportScreen(postId = post.id)
                                                        )
                                                }

                                                2 -> {
                                                    rawContent = post
                                                }

                                                1 -> model.reduce(PostListMviModel.Intent.Hide(post.id))

                                                else -> model.reduce(
                                                    PostListMviModel.Intent.SharePost(post.id)
                                                )
                                            }
                                        }
                                    )
                                },
                            )
                            if (uiState.postLayout != PostLayout.Card) {
                                Divider(modifier = Modifier.padding(vertical = Spacing.s))
                            } else {
                                Spacer(modifier = Modifier.height(Spacing.s))
                            }
                        }
                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(PostListMviModel.Intent.LoadNextPage)
                            }
                            if (uiState.loading && !uiState.refreshing) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(Spacing.xs),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(25.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                        if (uiState.posts.isEmpty() && !uiState.loading) {
                            item {
                                Text(
                                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                                    textAlign = TextAlign.Center,
                                    text = stringResource(MR.strings.message_empty_list),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(Spacing.xxxl))
                        }
                    }

                    PullRefreshIndicator(
                        refreshing = uiState.refreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        backgroundColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }

        if (rawContent != null) {
            when (val content = rawContent) {
                is PostModel -> {
                    RawContentDialog(
                        title = content.title,
                        url = content.url,
                        text = content.text,
                        onDismiss = {
                            rawContent = null
                        },
                    )
                }
            }
        }
    }
}
