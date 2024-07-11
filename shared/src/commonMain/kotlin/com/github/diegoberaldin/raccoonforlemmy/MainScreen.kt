package com.github.diegoberaldin.raccoonforlemmy

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.DrawerEvent
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.feature.home.ui.HomeTab
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.main.SettingsScreen
import com.github.diegoberaldin.raccoonforlemmy.navigation.TabNavigationItem
import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.ManageAccountsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal object MainScreen : Screen {
    @Composable
    override fun Content() {
        val themeRepository = remember { getThemeRepository() }
        var bottomBarHeightPx by remember { mutableStateOf(0f) }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val model = getScreenModel<MainMviModel>()
        val uiState by model.uiState.collectAsState()
        val uiFontScale by themeRepository.uiFontScale.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val exitMessage = LocalStrings.current.messageConfirmExit
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
        val bottomNavigationInsetPx =
            with(LocalDensity.current) {
                WindowInsets.navigationBars.getBottom(this)
            }
        val bottomNavigationInset =
            with(LocalDensity.current) {
                bottomNavigationInsetPx.toDp()
            }
        val scope = rememberCoroutineScope()
        val inboxReadAllSuccessMessage = LocalStrings.current.messageReadAllInboxSuccess

        LaunchedEffect(model) {
            model.effects
                .onEach {
                    when (it) {
                        is MainMviModel.Effect.UnreadItemsDetected -> {
                            navigationCoordinator.setInboxUnread(it.value)
                        }

                        MainMviModel.Effect.ReadAllInboxSuccess -> {
                            navigationCoordinator.showGlobalMessage(inboxReadAllSuccessMessage)
                        }
                    }
                }.launchIn(this)
        }

        val scrollConnection =
            remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource,
                    ): Offset {
                        val delta = available.y
                        val newOffset =
                            (uiState.bottomBarOffsetHeightPx + delta).coerceIn(
                                -(bottomBarHeightPx + bottomNavigationInsetPx),
                                0f,
                            )
                        model.reduce(MainMviModel.Intent.SetBottomBarOffsetHeightPx(newOffset))
                        return Offset.Zero
                    }
                }
            }
        navigationCoordinator.setBottomBarScrollConnection(scrollConnection)

        LaunchedEffect(navigationCoordinator) {
            with(navigationCoordinator) {
                if (currentSection.value == null) {
                    setCurrentSection(TabNavigationSection.Home)
                }
            }

            navigationCoordinator.exitMessageVisible
                .onEach { visible ->
                    if (visible) {
                        snackbarHostState.showSnackbar(
                            message = exitMessage,
                            duration = SnackbarDuration.Short,
                        )
                        navigationCoordinator.setExitMessageVisible(false)
                    }
                }.launchIn(this)
            navigationCoordinator.globalMessage
                .onEach { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                    )
                }.launchIn(this)
        }

        TabNavigator(HomeTab) { tabNavigator ->
            navigationCoordinator.setTabNavigator(tabNavigator)

            LaunchedEffect(tabNavigator.current) {
                // when the current tab changes, reset the bottom bar offset to the default value
                model.reduce(MainMviModel.Intent.SetBottomBarOffsetHeightPx(0f))
            }

            LaunchedEffect(drawerCoordinator) {
                drawerCoordinator.events
                    .onEach { evt ->
                        when (evt) {
                            is DrawerEvent.ChangeListingType -> {
                                if (tabNavigator.current == HomeTab) {
                                    notificationCenter.send(
                                        NotificationCenterEvent.ChangeFeedType(
                                            evt.value,
                                            "postList",
                                        ),
                                    )
                                } else {
                                    with(navigationCoordinator) {
                                        changeTab(HomeTab)
                                        setCurrentSection(TabNavigationSection.Home)
                                    }
                                    launch {
                                        // wait for transition to finish
                                        delay(750)
                                        notificationCenter.send(
                                            NotificationCenterEvent.ChangeFeedType(
                                                evt.value,
                                                "postList",
                                            ),
                                        )
                                    }
                                }
                            }

                            DrawerEvent.OpenSettings -> {
                                val screen = SettingsScreen()
                                navigationCoordinator.pushScreen(screen)
                            }

                            else -> Unit
                        }
                    }.launchIn(this)
            }

            Scaffold(
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data ->
                        Snackbar(
                            modifier =
                                Modifier
                                    .graphicsLayer {
                                        translationY =
                                            (-uiState.bottomBarOffsetHeightPx)
                                                .coerceAtMost(bottomBarHeightPx - bottomNavigationInsetPx)
                                    },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            snackbarData = data,
                        )
                    }
                },
                bottomBar = {
                    CompositionLocalProvider(
                        LocalDensity provides
                            Density(
                                density = LocalDensity.current.density,
                                fontScale = uiFontScale,
                            ),
                    ) {
                        val titleVisible by themeRepository.navItemTitles.collectAsState()
                        var uiFontSizeWorkaround by remember { mutableStateOf(true) }
                        LaunchedEffect(themeRepository) {
                            themeRepository.uiFontScale
                                .drop(1)
                                .onEach {
                                    uiFontSizeWorkaround = false
                                    delay(50)
                                    uiFontSizeWorkaround = true
                                }.launchIn(this)
                        }

                        fun handleOnLongPress(
                            tab: Tab,
                            section: TabNavigationSection,
                        ) {
                            when (section) {
                                TabNavigationSection.Explore -> {
                                    tabNavigator.current = tab
                                    navigationCoordinator.setCurrentSection(TabNavigationSection.Explore)
                                    scope.launch {
                                        notificationCenter.send(NotificationCenterEvent.OpenSearchInExplore)
                                    }
                                }

                                TabNavigationSection.Inbox -> {
                                    if (uiState.isLogged) {
                                        model.reduce(MainMviModel.Intent.ReadAllInbox)
                                    }
                                }

                                TabNavigationSection.Profile -> {
                                    if (uiState.isLogged) {
                                        val screen = ManageAccountsScreen()
                                        navigationCoordinator.showBottomSheet(screen)
                                    }
                                }

                                else -> Unit
                            }
                        }

                        if (uiFontSizeWorkaround) {
                            NavigationBar(
                                modifier =
                                    Modifier
                                        .onGloballyPositioned {
                                            if (bottomBarHeightPx == 0f) {
                                                bottomBarHeightPx = it.size.toSize().height
                                            }
                                        }.offset {
                                            IntOffset(
                                                x = 0,
                                                y = -uiState.bottomBarOffsetHeightPx.roundToInt(),
                                            )
                                        },
                                windowInsets =
                                    WindowInsets(
                                        left = 0.dp,
                                        top = 0.dp,
                                        right = 0.dp,
                                        bottom = bottomNavigationInset,
                                    ),
                                tonalElevation = 0.dp,
                            ) {
                                for (section in uiState.bottomBarSections) {
                                    TabNavigationItem(
                                        section = section,
                                        withText = titleVisible,
                                        customIconUrl =
                                            if (section == TabNavigationSection.Profile) {
                                                uiState.customProfileUrl
                                            } else {
                                                null
                                            },
                                        onClick =
                                            rememberCallbackArgs { tab ->
                                                tabNavigator.current = tab
                                                navigationCoordinator.setCurrentSection(section)
                                            },
                                        onLongPress =
                                            rememberCallbackArgs { tab ->
                                                handleOnLongPress(tab, section)
                                            },
                                    )
                                }
                            }
                        }
                    }
                },
                content = {
                    CurrentTab()
                },
            )
        }
    }
}
