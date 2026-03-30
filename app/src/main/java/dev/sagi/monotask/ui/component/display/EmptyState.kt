package dev.sagi.monotask.ui.component.display

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ========== IllustrationSize ==========

enum class IllustrationSize { Large, Small }

// ========== IllustrationMessage ==========

/**
 * Shared base composable: illustration image + typewriter title + optional typewriter subtitle.
 *
 * [IllustrationSize.Large] — dynamic sizing, used for full-screen empty states.
 * [IllustrationSize.Small] — fixed compact sizing, used for in-content status cards.
 */
@Composable
fun IllustrationMessage(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    @DrawableRes imgRes: Int? = null,
    size: IllustrationSize = IllustrationSize.Large,
    animate: Boolean = true,
    action: (@Composable () -> Unit)? = null
) {
    when (size) {
        IllustrationSize.Large -> LargeIllustrationContent(title, subtitle, imgRes, modifier, animate, action)
        IllustrationSize.Small -> SmallIllustrationContent(title, subtitle, imgRes, modifier, animate, action)
    }
}

@Composable
private fun LargeIllustrationContent(
    title: String,
    subtitle: String?,
    @DrawableRes imgRes: Int?,
    modifier: Modifier,
    animate: Boolean,
    action: (@Composable () -> Unit)?
) {
    val imageAnimDuration = 400
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val dynamicImageSize = with(LocalDensity.current) { (screenWidth * 1.5f).toDp() }
    val dynamicFontSizeBig = with(LocalDensity.current) { (screenWidth * 0.18f).toSp() }
    val dynamicFontSizeSmall = with(LocalDensity.current) { (screenWidth * 0.11f).toSp() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        imgRes?.let {
            val alpha = remember { Animatable(if (animate) 0f else 1f) }
            val scale = remember { Animatable(if (animate) 0.65f else 1f) }
            var hasAnimated by remember { mutableStateOf(!animate) }
            LaunchedEffect(it) {
                if (!hasAnimated) {
                    hasAnimated = true
                    launch { alpha.animateTo(1f, tween(imageAnimDuration)) }
                    launch {
                        scale.animateTo(
                            1f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                        )
                    }
                }
            }
            Image(
                painter = painterResource(it),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(dynamicImageSize)
                    .padding(bottom = 32.dp)
                    .graphicsLayer { this.alpha = alpha.value; scaleX = scale.value; scaleY = scale.value }
            )
        }

        val charDelay = 25L
        TypewriterText(
            text        = title,
            delayBefore = imageAnimDuration.toLong(),
            charDelay   = charDelay,
            style       = MaterialTheme.typography.headlineMedium.copy(fontSize = dynamicFontSizeBig),
            textAlign   = TextAlign.Center,
            color       = MaterialTheme.colorScheme.onBackground,
            animate     = animate,
            modifier    = Modifier.padding(bottom = 6.dp)
        )

        subtitle?.let {
            TypewriterText(
                text        = it,
                delayBefore = imageAnimDuration + (title.length * charDelay) + 200L,
                charDelay   = charDelay,
                style       = MaterialTheme.typography.labelLarge.copy(fontSize = dynamicFontSizeSmall),
                fontWeight  = FontWeight.Thin,
                textAlign   = TextAlign.Center,
                color       = MaterialTheme.colorScheme.outline,
                animate     = animate,
            )
        }

        action?.let {
            Spacer(modifier = Modifier.height(24.dp))
            it()
        }
    }
}

@Composable
private fun SmallIllustrationContent(
    title: String,
    subtitle: String?,
    @DrawableRes imgRes: Int?,
    modifier: Modifier,
    animate: Boolean,
    action: (@Composable () -> Unit)?
) {
    val imageAnimDuration = 300
    Column(
        modifier            = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        imgRes?.let {
            val alpha = remember { Animatable(if (animate) 0f else 1f) }
            val scale = remember { Animatable(if (animate) 0.8f else 1f) }
            var hasAnimated by remember { mutableStateOf(!animate) }
            LaunchedEffect(it) {
                if (!hasAnimated) {
                    hasAnimated = true
                    launch { alpha.animateTo(1f, tween(imageAnimDuration)) }
                    launch {
                        scale.animateTo(
                            1f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                        )
                    }
                }
            }
            Image(
                painter      = painterResource(it),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier     = Modifier
                    .height(120.dp)
                    .padding(bottom = 8.dp)
                    .graphicsLayer { this.alpha = alpha.value; scaleX = scale.value; scaleY = scale.value }
            )
        }

        val charDelay = 20L
        TypewriterText(
            text        = title,
            delayBefore = imageAnimDuration.toLong(),
            charDelay   = charDelay,
            style       = MaterialTheme.typography.titleMedium,
            fontWeight  = FontWeight.SemiBold,
            textAlign   = TextAlign.Center,
            color       = MaterialTheme.colorScheme.onBackground,
            animate     = animate,
        )

        subtitle?.let {
            TypewriterText(
                text        = it,
                delayBefore = imageAnimDuration + (title.length * charDelay) + 100L,
                charDelay   = 15L,
                style       = MaterialTheme.typography.labelMedium,
                fontWeight  = FontWeight.Normal,
                textAlign   = TextAlign.Center,
                color       = MaterialTheme.colorScheme.outline,
                animate     = animate,
            )
        }

        action?.let {
            Spacer(modifier = Modifier.height(8.dp))
            it()
        }
    }
}

// ========== TypewriterText ==========

@Composable
fun TypewriterText(
    modifier: Modifier = Modifier,
    text: String = "Preview Text",
    delayBefore: Long = 0L,
    charDelay: Long = 30L,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    animate: Boolean = true,
) {
    var hasAnimated by remember { mutableStateOf(!animate) }
    var displayed by remember { mutableStateOf(if (!animate) text else "") }

    LaunchedEffect(text) {
        if (hasAnimated) {
            displayed = text
            return@LaunchedEffect
        }
        hasAnimated = true
        displayed = ""
        delay(delayBefore)
        text.forEachIndexed { i, _ ->
            displayed = text.substring(0, i + 1)
            delay(charDelay)
        }
    }

    Text(
        text       = displayed,
        style      = style,
        color      = color,
        fontWeight = fontWeight,
        textAlign  = textAlign,
        modifier   = modifier
    )
}

// ========== EmptyState ==========

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    emoji: String? = null,
    isMainContent: Boolean = false,
    @DrawableRes imgRes: Int? = null,
    action: (@Composable () -> Unit)? = null
) {
    if (isMainContent) {
        IllustrationMessage(
            title    = title,
            subtitle = subtitle,
            imgRes   = imgRes,
            size     = IllustrationSize.Large,
            modifier = modifier,
            action   = action
        )
    } else {
        SubtleEmptyStateContent(title, subtitle, emoji, modifier, action)
    }
}

@Composable
private fun SubtleEmptyStateContent(
    title: String,
    subtitle: String?,
    emoji: String?,
    modifier: Modifier,
    action: (@Composable () -> Unit)?
) {
    val delayBefore = 600L
    val titleWithEmoji = if (emoji != null) "$title $emoji" else title

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        TypewriterText(
            text        = titleWithEmoji,
            delayBefore = delayBefore,
            charDelay   = 30L,
            style       = MaterialTheme.typography.titleSmall,
            fontWeight  = FontWeight.SemiBold,
            textAlign   = TextAlign.Start,
            color       = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        subtitle?.let {
            Spacer(modifier = Modifier.height(4.dp))
            TypewriterText(
                text        = it,
                delayBefore = delayBefore + (titleWithEmoji.length * 30L),
                charDelay   = 20L,
                style       = MaterialTheme.typography.bodySmall,
                fontWeight  = FontWeight.Thin,
                textAlign   = TextAlign.Start,
                color       = MaterialTheme.colorScheme.outline,
            )
        }

        action?.let {
            Spacer(modifier = Modifier.height(12.dp))
            it()
        }
    }
}


// ========== Previews ==========

@Preview(showBackground = true)
@Composable
fun FocusEmptyStatePreview() {
    MonoTaskTheme {
        EmptyState(
            emoji    = "🦾",
            title    = "You're all caught up!",
            subtitle = "No tasks here. Enjoy the moment."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FriendsEmptyStatePreview() {
    MonoTaskTheme {
        EmptyState(
            title    = "No friends yet",
            subtitle = "Share your invite link to connect with friends"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubtleEmptyStatePreview() {
    MonoTaskTheme {
        EmptyState(
            emoji         = "📭",
            title         = "Nothing here yet",
            subtitle      = "Check back later",
            isMainContent = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SmallIllustrationPreview() {
    MonoTaskTheme {
        IllustrationMessage(
            title    = "You're all clear!",
            subtitle = "No overdue or open tasks",
            size     = IllustrationSize.Small,
            modifier = Modifier.padding(16.dp)
        )
    }
}
