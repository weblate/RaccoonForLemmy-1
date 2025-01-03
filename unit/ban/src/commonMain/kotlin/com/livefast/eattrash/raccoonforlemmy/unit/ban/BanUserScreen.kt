package com.livefast.eattrash.raccoonforlemmy.unit.ban

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import com.livefast.eattrash.raccoonforlemmy.core.appearance.theme.Spacing
import com.livefast.eattrash.raccoonforlemmy.core.commonui.components.ProgressHud
import com.livefast.eattrash.raccoonforlemmy.core.commonui.lemmyui.SettingsIntValueRow
import com.livefast.eattrash.raccoonforlemmy.core.commonui.lemmyui.SettingsSwitchRow
import com.livefast.eattrash.raccoonforlemmy.core.l10n.LocalStrings
import com.livefast.eattrash.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.livefast.eattrash.raccoonforlemmy.core.utils.safeImePadding
import com.livefast.eattrash.raccoonforlemmy.core.utils.toReadableMessage
import com.livefast.eattrash.raccoonforlemmy.unit.ban.di.BanUserMviModelParams
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.seconds

class BanUserScreen(
    private val userId: Long,
    private val communityId: Long,
    private val newValue: Boolean,
    private val postId: Long? = null,
    private val commentId: Long? = null,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model: BanUserMviModel =
            rememberScreenModel(
                arg =
                    BanUserMviModelParams(
                        userId = userId,
                        communityId = communityId,
                        newValue = newValue,
                        postId = postId ?: 0L,
                        commentId = commentId ?: 0L,
                    ),
            )
        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = LocalStrings.current.messageGenericError
        val successMessage = LocalStrings.current.messageOperationSuccessful
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
        val focusManager = LocalFocusManager.current

        LaunchedEffect(model) {
            model.effects
                .onEach {
                    when (it) {
                        is BanUserMviModel.Effect.Failure -> {
                            snackbarHostState.showSnackbar(it.message ?: genericError)
                        }

                        BanUserMviModel.Effect.Success -> {
                            navigationCoordinator.showGlobalMessage(message = successMessage, delay = 1.seconds)
                            navigationCoordinator.popScreen()
                        }
                    }
                }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier.navigationBarsPadding().safeImePadding(),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navigationCoordinator.popScreen()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = LocalStrings.current.buttonClose,
                            )
                        }
                    },
                    title = {
                        val title =
                            if (newValue) {
                                LocalStrings.current.modActionBan
                            } else {
                                LocalStrings.current.modActionAllow
                            }
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                model.reduce(BanUserMviModel.Intent.Submit)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.Send,
                                contentDescription = LocalStrings.current.actionSend,
                            )
                        }
                    },
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        snackbarData = data,
                    )
                }
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .safeImePadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val focusRequester = remember { FocusRequester() }
                TextField(
                    modifier =
                        Modifier
                            .focusRequester(focusRequester)
                            .heightIn(min = 300.dp, max = 500.dp)
                            .fillMaxWidth(),
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                    label = {
                        Text(text = LocalStrings.current.banReasonPlaceholder)
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = uiState.text,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            autoCorrectEnabled = true,
                        ),
                    onValueChange = { value ->
                        model.reduce(BanUserMviModel.Intent.SetText(value))
                    },
                    isError = uiState.textError != null,
                    supportingText = {
                        val error = uiState.textError
                        if (error != null) {
                            Text(
                                text = error.toReadableMessage(),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )

                if (uiState.targetBanValue) {
                    // it is a ban (as opposed to unban)
                    SettingsSwitchRow(
                        title = LocalStrings.current.banItemPermanent,
                        value = uiState.permanent,
                        onValueChanged = { value ->
                            model.reduce(BanUserMviModel.Intent.ChangePermanent(value))
                        },
                    )

                    if (!uiState.permanent) {
                        SettingsIntValueRow(
                            title = LocalStrings.current.banItemDurationDays,
                            value = uiState.days,
                            onIncrement = {
                                model.reduce(BanUserMviModel.Intent.IncrementDays)
                            },
                            onDecrement = {
                                model.reduce(BanUserMviModel.Intent.DecrementDays)
                            },
                        )
                    }

                    SettingsSwitchRow(
                        title = LocalStrings.current.banItemRemoveData,
                        value = uiState.removeData,
                        onValueChanged = { value ->
                            model.reduce(BanUserMviModel.Intent.ChangeRemoveData(value))
                        },
                    )
                }

                Spacer(Modifier.height(Spacing.xxl))
            }

            if (uiState.loading) {
                ProgressHud()
            }

            SnackbarHost(
                modifier = Modifier.padding(bottom = Spacing.xxxl),
                hostState = snackbarHostState,
            ) { data ->
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    snackbarData = data,
                )
            }
        }
    }
}
