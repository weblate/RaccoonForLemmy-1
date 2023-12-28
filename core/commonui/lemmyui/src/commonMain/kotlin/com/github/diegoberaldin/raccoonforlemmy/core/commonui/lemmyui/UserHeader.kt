package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Padding
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PlaceholderImage
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.getPrettyNumber
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun UserHeader(
    user: UserModel,
    modifier: Modifier = Modifier,
    autoLoadImages: Boolean = true,
    onOpenImage: ((String) -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        // banner
        val banner = user.banner.orEmpty()
        if (banner.isNotEmpty() && autoLoadImages) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(4f),
            ) {
                CustomImage(
                    modifier = Modifier.fillMaxSize(),
                    url = banner,
                    quality = FilterQuality.Low,
                    contentScale = ContentScale.FillBounds,
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
                            ),
                        ),
                    ),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.s).align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            // avatar
            val userAvatar = user.avatar.orEmpty()
            if (userAvatar.isNotEmpty() && autoLoadImages) {
                CustomImage(
                    modifier = Modifier.padding(Spacing.xxxs)
                        .size(IconSize.xxl)
                        .clip(RoundedCornerShape(IconSize.xxl / 2))
                        .onClick(
                            onClick = rememberCallback {
                                onOpenImage?.invoke(userAvatar)
                            },
                        ),
                    url = userAvatar,
                    quality = FilterQuality.Low,
                    contentScale = ContentScale.FillBounds,
                )
            } else {
                PlaceholderImage(
                    size = IconSize.xxl,
                    title = user.name,
                )
            }

            // textual data
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                Text(
                    text = buildString {
                        if (user.displayName.isNotEmpty()) {
                            append(user.displayName)
                        } else {
                            append(user.name)
                        }
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = buildString {
                        append(user.name)
                        append("@")
                        append(user.host)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                // stats and age
                val iconSize = 22.dp
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val postScore = user.score?.postScore
                    val commentScore = user.score?.commentScore
                    if (postScore != null) {
                        Icon(
                            modifier = Modifier.size(iconSize),
                            imageVector = Icons.Default.Padding,
                            contentDescription = null
                        )
                        Text(
                            text = postScore.getPrettyNumber(
                                thousandLabel = stringResource(MR.strings.profile_thousand_short),
                                millionLabel = stringResource(MR.strings.profile_million_short),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    if (commentScore != null) {
                        if (postScore != null) {
                            Spacer(modifier = Modifier.width(Spacing.xxxs))
                        }
                        Icon(
                            modifier = Modifier.size(iconSize),
                            imageVector = Icons.Default.Reply,
                            contentDescription = null
                        )
                        Text(
                            text = commentScore.getPrettyNumber(
                                thousandLabel = stringResource(MR.strings.profile_thousand_short),
                                millionLabel = stringResource(MR.strings.profile_million_short),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    if (user.accountAge.isNotEmpty()) {
                        if (postScore != null || commentScore != null) {
                            Spacer(modifier = Modifier.width(Spacing.xxxs))
                        }
                        Icon(
                            modifier = Modifier.size(iconSize),
                            imageVector = Icons.Default.Cake,
                            contentDescription = null
                        )
                        Text(
                            text = user.accountAge.prettifyDate(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}
