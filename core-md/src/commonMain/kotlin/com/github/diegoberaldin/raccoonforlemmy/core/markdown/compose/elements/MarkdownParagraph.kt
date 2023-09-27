package com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalMarkdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.utils.buildMarkdownAnnotatedString
import org.intellij.markdown.ast.ASTNode

@Composable
internal fun MarkdownParagraph(
    content: String,
    node: ASTNode,
    style: TextStyle = LocalMarkdownTypography.current.paragraph,
    onOpenUrl: ((String) -> Unit)? = null,
    inlineImages: Boolean = true,
) {
    val styledText = buildAnnotatedString {
        pushStyle(style.toSpanStyle())
        buildMarkdownAnnotatedString(content, node)
        pop()
    }
    MarkdownText(
        content = styledText,
        style = style,
        onOpenUrl = onOpenUrl,
        inlineImages = inlineImages
    )
}
