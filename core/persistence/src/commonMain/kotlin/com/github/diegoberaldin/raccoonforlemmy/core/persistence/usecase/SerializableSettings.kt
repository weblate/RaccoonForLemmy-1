package com.github.diegoberaldin.raccoonforlemmy.core.persistence.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toLong
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toVoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontScales
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.toActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.toInt
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal val jsonSerializationStrategy =
    Json {
        encodeDefaults = true
        prettyPrint = true
    }

@Serializable
internal data class SerializableSettings(
    val theme: Int? = null,
    val uiFontFamily: Int = 0,
    val uiFontScale: Float = 1f,
    val contentFontScale: List<Float> = listOf(),
    val contentFontFamily: Int = 0,
    val locale: String? = null,
    val defaultListingType: Int = 2,
    val defaultPostSortType: Int = 1,
    val defaultInboxType: Int = 0,
    val defaultCommentSortType: Int = 3,
    val defaultExploreType: Int = 2,
    val includeNsfw: Boolean = false,
    val blurNsfw: Boolean = true,
    val navigationTitlesVisible: Boolean = true,
    val dynamicColors: Boolean = false,
    val urlOpeningMode: Int = 1,
    val enableSwipeActions: Boolean = true,
    val enableDoubleTapAction: Boolean = false,
    val customSeedColor: Int? = null,
    val upVoteColor: Int? = null,
    val downVoteColor: Int? = null,
    val postLayout: Int = 0,
    val fullHeightImages: Boolean = true,
    val voteFormat: Long = 0,
    val autoLoadImages: Boolean = true,
    val autoExpandComments: Boolean = true,
    val hideNavigationBarWhileScrolling: Boolean = true,
    val zombieModeInterval: Duration = 1.seconds,
    val zombieModeScrollAmount: Float = 55f,
    val markAsReadWhileScrolling: Boolean = false,
    val commentBarTheme: Int = 0,
    val replyColor: Int? = null,
    val saveColor: Int? = null,
    val searchPostTitleOnly: Boolean = false,
    val edgeToEdge: Boolean = true,
    val postBodyMaxLines: Int? = null,
    val infiniteScrollEnabled: Boolean = true,
    val actionsOnSwipeToStartPosts: List<Int> = emptyList(),
    val actionsOnSwipeToEndPosts: List<Int> = emptyList(),
    val actionsOnSwipeToStartComments: List<Int> = emptyList(),
    val actionsOnSwipeToEndComments: List<Int> = emptyList(),
    val actionsOnSwipeToStartInbox: List<Int> = emptyList(),
    val actionsOnSwipeToEndInbox: List<Int> = emptyList(),
    val opaqueSystemBars: Boolean = false,
    val showScores: Boolean = true,
    val preferUserNicknames: Boolean = true,
    val commentBarThickness: Int = 1,
    val commentIndentAmount: Int = 2,
    val imageSourcePath: Boolean = false,
    val defaultLanguageId: Long? = null,
    val inboxBackgroundCheckPeriod: Duration? = null,
    val fadeReadPosts: Boolean = false,
    val showUnreadComments: Boolean = false,
    val enableButtonsToScrollBetweenComments: Boolean = false,
    val fullWidthImages: Boolean = false,
    val enableToggleFavoriteInNavDrawer: Boolean = false,
    val inboxPreviewMaxLines: Int? = null,
    val defaultExploreResultType: Int = 2,
    val useAvatarAsProfileNavigationIcon: Boolean = false,
    val randomThemeColor: Boolean = false,
)

internal fun SerializableSettings.toModel() =
    SettingsModel(
        theme = theme,
        uiFontFamily = uiFontFamily,
        uiFontScale = uiFontScale,
        contentFontScale =
            ContentFontScales(
                title = contentFontScale[0],
                body = contentFontScale[1],
                comment = contentFontScale[2],
                ancillary = contentFontScale[3],
            ),
        contentFontFamily = contentFontFamily,
        locale = locale,
        defaultListingType = defaultListingType,
        defaultPostSortType = defaultPostSortType,
        defaultInboxType = defaultInboxType,
        defaultCommentSortType = defaultCommentSortType,
        defaultExploreType = defaultExploreType,
        includeNsfw = includeNsfw,
        blurNsfw = blurNsfw,
        navigationTitlesVisible = navigationTitlesVisible,
        dynamicColors = dynamicColors,
        urlOpeningMode = urlOpeningMode,
        enableSwipeActions = enableSwipeActions,
        enableDoubleTapAction = enableDoubleTapAction,
        customSeedColor = customSeedColor,
        upVoteColor = upVoteColor,
        downVoteColor = downVoteColor,
        postLayout = postLayout,
        fullHeightImages = fullHeightImages,
        voteFormat = voteFormat.toVoteFormat(),
        autoLoadImages = autoLoadImages,
        autoExpandComments = autoExpandComments,
        hideNavigationBarWhileScrolling = hideNavigationBarWhileScrolling,
        zombieModeInterval = zombieModeInterval,
        zombieModeScrollAmount = zombieModeScrollAmount,
        markAsReadWhileScrolling = markAsReadWhileScrolling,
        commentBarTheme = commentBarTheme,
        replyColor = replyColor,
        saveColor = saveColor,
        searchPostTitleOnly = searchPostTitleOnly,
        edgeToEdge = edgeToEdge,
        postBodyMaxLines = postBodyMaxLines,
        infiniteScrollEnabled = infiniteScrollEnabled,
        actionsOnSwipeToStartPosts = actionsOnSwipeToStartPosts.map { it.toActionOnSwipe() },
        actionsOnSwipeToEndPosts = actionsOnSwipeToEndPosts.map { it.toActionOnSwipe() },
        actionsOnSwipeToStartComments = actionsOnSwipeToStartComments.map { it.toActionOnSwipe() },
        actionsOnSwipeToEndComments = actionsOnSwipeToEndComments.map { it.toActionOnSwipe() },
        actionsOnSwipeToStartInbox = actionsOnSwipeToStartInbox.map { it.toActionOnSwipe() },
        actionsOnSwipeToEndInbox = actionsOnSwipeToEndInbox.map { it.toActionOnSwipe() },
        opaqueSystemBars = opaqueSystemBars,
        showScores = showScores,
        preferUserNicknames = preferUserNicknames,
        commentBarThickness = commentBarThickness,
        commentIndentAmount = commentIndentAmount,
        imageSourcePath = imageSourcePath,
        defaultLanguageId = defaultLanguageId,
        inboxBackgroundCheckPeriod = inboxBackgroundCheckPeriod,
        fadeReadPosts = fadeReadPosts,
        showUnreadComments = showUnreadComments,
        enableButtonsToScrollBetweenComments = enableButtonsToScrollBetweenComments,
        fullWidthImages = fullWidthImages,
        enableToggleFavoriteInNavDrawer = enableToggleFavoriteInNavDrawer,
        inboxPreviewMaxLines = inboxPreviewMaxLines,
        useAvatarAsProfileNavigationIcon = useAvatarAsProfileNavigationIcon,
        randomThemeColor = randomThemeColor,
    )

internal fun SettingsModel.toData() =
    SerializableSettings(
        theme = theme,
        uiFontFamily = uiFontFamily,
        uiFontScale = uiFontScale,
        contentFontScale =
            contentFontScale.let {
                listOf(
                    it.title,
                    it.body,
                    it.comment,
                    it.ancillary,
                )
            },
        contentFontFamily = contentFontFamily,
        locale = locale,
        defaultListingType = defaultListingType,
        defaultPostSortType = defaultPostSortType,
        defaultInboxType = defaultInboxType,
        defaultCommentSortType = defaultCommentSortType,
        defaultExploreType = defaultExploreType,
        includeNsfw = includeNsfw,
        blurNsfw = blurNsfw,
        navigationTitlesVisible = navigationTitlesVisible,
        dynamicColors = dynamicColors,
        urlOpeningMode = urlOpeningMode,
        enableSwipeActions = enableSwipeActions,
        enableDoubleTapAction = enableDoubleTapAction,
        customSeedColor = customSeedColor,
        upVoteColor = upVoteColor,
        downVoteColor = downVoteColor,
        postLayout = postLayout,
        fullHeightImages = fullHeightImages,
        voteFormat = voteFormat.toLong(),
        autoLoadImages = autoLoadImages,
        autoExpandComments = autoExpandComments,
        hideNavigationBarWhileScrolling = hideNavigationBarWhileScrolling,
        zombieModeInterval = zombieModeInterval,
        zombieModeScrollAmount = zombieModeScrollAmount,
        markAsReadWhileScrolling = markAsReadWhileScrolling,
        commentBarTheme = commentBarTheme,
        replyColor = replyColor,
        saveColor = saveColor,
        searchPostTitleOnly = searchPostTitleOnly,
        edgeToEdge = edgeToEdge,
        postBodyMaxLines = postBodyMaxLines,
        infiniteScrollEnabled = infiniteScrollEnabled,
        actionsOnSwipeToStartPosts = actionsOnSwipeToStartPosts.map { it.toInt() },
        actionsOnSwipeToEndPosts = actionsOnSwipeToEndPosts.map { it.toInt() },
        actionsOnSwipeToStartComments = actionsOnSwipeToStartComments.map { it.toInt() },
        actionsOnSwipeToEndComments = actionsOnSwipeToEndComments.map { it.toInt() },
        actionsOnSwipeToStartInbox = actionsOnSwipeToStartInbox.map { it.toInt() },
        actionsOnSwipeToEndInbox = actionsOnSwipeToEndInbox.map { it.toInt() },
        opaqueSystemBars = opaqueSystemBars,
        showScores = showScores,
        preferUserNicknames = preferUserNicknames,
        commentBarThickness = commentBarThickness,
        commentIndentAmount = commentIndentAmount,
        imageSourcePath = imageSourcePath,
        defaultLanguageId = defaultLanguageId,
        inboxBackgroundCheckPeriod = inboxBackgroundCheckPeriod,
        fadeReadPosts = fadeReadPosts,
        showUnreadComments = showUnreadComments,
        enableButtonsToScrollBetweenComments = enableButtonsToScrollBetweenComments,
        fullWidthImages = fullWidthImages,
        enableToggleFavoriteInNavDrawer = enableToggleFavoriteInNavDrawer,
        inboxPreviewMaxLines = inboxPreviewMaxLines,
        useAvatarAsProfileNavigationIcon = useAvatarAsProfileNavigationIcon,
        randomThemeColor = randomThemeColor,
    )
