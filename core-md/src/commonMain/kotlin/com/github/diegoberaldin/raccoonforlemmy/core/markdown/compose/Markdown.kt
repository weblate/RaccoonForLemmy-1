package com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements.MarkdownBlockQuote
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements.MarkdownBulletList
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements.MarkdownCodeBlock
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements.MarkdownCodeFence
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements.MarkdownHeader
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements.MarkdownImage
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements.MarkdownOrderedList
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements.MarkdownParagraph
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements.MarkdownText
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.MarkdownColors
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.MarkdownPadding
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.MarkdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.ReferenceLinkHandlerImpl
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.markdownColor
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.markdownPadding
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.markdownTypography
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownElementTypes.ATX_1
import org.intellij.markdown.MarkdownElementTypes.ATX_2
import org.intellij.markdown.MarkdownElementTypes.ATX_3
import org.intellij.markdown.MarkdownElementTypes.ATX_4
import org.intellij.markdown.MarkdownElementTypes.ATX_5
import org.intellij.markdown.MarkdownElementTypes.ATX_6
import org.intellij.markdown.MarkdownElementTypes.BLOCK_QUOTE
import org.intellij.markdown.MarkdownElementTypes.CODE_BLOCK
import org.intellij.markdown.MarkdownElementTypes.CODE_FENCE
import org.intellij.markdown.MarkdownElementTypes.IMAGE
import org.intellij.markdown.MarkdownElementTypes.LINK_DEFINITION
import org.intellij.markdown.MarkdownElementTypes.ORDERED_LIST
import org.intellij.markdown.MarkdownElementTypes.PARAGRAPH
import org.intellij.markdown.MarkdownElementTypes.UNORDERED_LIST
import org.intellij.markdown.MarkdownTokenTypes.Companion.EOL
import org.intellij.markdown.MarkdownTokenTypes.Companion.TEXT
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

/**
 * Credits:
 * https://github.com/mikepenz/multiplatform-markdown-renderer
 */
@Composable
fun Markdown(
    content: String,
    colors: MarkdownColors = markdownColor(),
    typography: MarkdownTypography = markdownTypography(),
    padding: MarkdownPadding = markdownPadding(),
    modifier: Modifier = Modifier.fillMaxSize(),
    flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor(),
    onOpenUrl: ((String) -> Unit)? = null,
) {
    val matches = Regex("::: spoiler (?<title>.*?)\\n(?<content>.*?)\\n:::\\n").findAll(content)
    val mangledContent = buildString {
        var lastIndex = -1
        for (match in matches) {
            val (start, end) = match.range.first to match.range.last
            if (lastIndex == -1) {
                append(content.substring(0, start))
            } else {
                append(content.substring(lastIndex, start))
            }
            val title = match.groups["title"]?.value.orEmpty()
            val spoilerContent = match.groups["content"]?.value.orEmpty()
            val replacement =
                "<details>\\n<summary>\\n$title\\n</summary>\\n\\n$spoilerContent\\n</details>\\n"
            append(replacement)
            lastIndex = end
        }
        if (lastIndex >= 0) {
            if (lastIndex < content.length) {
                append(content.substring(lastIndex))
            }
        } else {
            append(content)
        }
    }

    CompositionLocalProvider(
        LocalReferenceLinkHandler provides ReferenceLinkHandlerImpl(),
        LocalMarkdownPadding provides padding,
        LocalMarkdownColors provides colors,
        LocalMarkdownTypography provides typography,
    ) {
        Column(modifier) {
            val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(mangledContent)
            parsedTree.children.forEach { node ->
                if (!node.handleElement(mangledContent, onOpenUrl)) {
                    node.children.forEach { child ->
                        child.handleElement(mangledContent, onOpenUrl)
                    }
                }
            }
        }
    }
}

@Composable
private fun ASTNode.handleElement(
    content: String,
    onOpenUrl: ((String) -> Unit)? = null,
): Boolean {
    val typography = LocalMarkdownTypography.current
    var handled = true
    Spacer(Modifier.height(LocalMarkdownPadding.current.block))
    when (type) {
        TEXT -> {
            val text = getTextInNode(content).toString()
            MarkdownText(text, onOpenUrl = onOpenUrl)
        }

        EOL -> {}
        CODE_FENCE -> MarkdownCodeFence(content, this)
        CODE_BLOCK -> MarkdownCodeBlock(content, this)
        ATX_1 -> MarkdownHeader(content, this, typography.h1)
        ATX_2 -> MarkdownHeader(content, this, typography.h2)
        ATX_3 -> MarkdownHeader(content, this, typography.h3)
        ATX_4 -> MarkdownHeader(content, this, typography.h4)
        ATX_5 -> MarkdownHeader(content, this, typography.h5)
        ATX_6 -> MarkdownHeader(content, this, typography.h6)
        BLOCK_QUOTE -> MarkdownBlockQuote(content, this)
        PARAGRAPH -> MarkdownParagraph(
            content,
            this,
            style = typography.paragraph,
            onOpenUrl = onOpenUrl
        )

        ORDERED_LIST -> Column(modifier = Modifier) {
            MarkdownOrderedList(
                content,
                this@handleElement,
                style = typography.ordered,
                onOpenUrl = onOpenUrl
            )
        }

        UNORDERED_LIST -> Column(modifier = Modifier) {
            MarkdownBulletList(
                content,
                this@handleElement,
                style = typography.bullet,
                onOpenUrl = onOpenUrl
            )
        }

        IMAGE -> MarkdownImage(content, this)
        LINK_DEFINITION -> {
            val linkLabel =
                findChildOfType(MarkdownElementTypes.LINK_LABEL)?.getTextInNode(content)?.toString()
            if (linkLabel != null) {
                val destination =
                    findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(content)
                        ?.toString()
                LocalReferenceLinkHandler.current.store(linkLabel, destination)
            }
        }

        else -> handled = false
    }
    return handled
}
