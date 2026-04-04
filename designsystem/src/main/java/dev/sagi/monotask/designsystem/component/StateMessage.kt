package dev.sagi.monotask.designsystem.component

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import kotlinx.coroutines.launch

private const val IMAGE_ANIM_DURATION_MS       = 400
private const val SMALL_IMAGE_ANIM_DURATION_MS = 300
private const val LARGE_CHAR_DELAY_MS          = 25L
private const val SMALL_CHAR_DELAY_MS          = 20L

enum class IllustrationSize { Large, Small }

/**
 * Screen-state composable: illustration image + typewriter title + optional subtitle.
 *
 * [IllustrationSize.Large] — dynamic sizing, used for full-screen states.
 * [IllustrationSize.Small] — fixed compact sizing, used for in-content status cards.
 */
@Composable
fun StateMessage(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    @DrawableRes imgRes: Int? = null,
    size: IllustrationSize = IllustrationSize.Large,
    animate: Boolean = true,
    action: (@Composable () -> Unit)? = null
) {
    when (size) {
        IllustrationSize.Large -> LargeStateMessage(title, subtitle, imgRes, modifier, animate, action)
        IllustrationSize.Small -> SmallStateMessage(title, subtitle, imgRes, modifier, animate, action)
    }
}

@Composable
private fun LargeStateMessage(
    title: String,
    subtitle: String?,
    @DrawableRes imgRes: Int?,
    modifier: Modifier,
    animate: Boolean,
    action: (@Composable () -> Unit)?
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val dynamicImageHeight = with(LocalDensity.current) { (screenWidth * 1.5f).toDp() }
    val dynamicFontSizeBig = with(LocalDensity.current) { (screenWidth * 0.18f).toSp() }
    val dynamicFontSizeSmall = with(LocalDensity.current) { (screenWidth * 0.11f).toSp() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        imgRes?.let {
            AnimatedIllustrationImage(
                imgRes = it,
                animate = animate,
                initialScale = 0.65f,
                stiffness = Spring.StiffnessLow,
                durationMs = IMAGE_ANIM_DURATION_MS,
                modifier = Modifier.height(dynamicImageHeight).padding(bottom = 32.dp)
            )
        }

        TypewriterText(
            text = title,
            delayBefore = IMAGE_ANIM_DURATION_MS.toLong(),
            charDelay = LARGE_CHAR_DELAY_MS,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = dynamicFontSizeBig),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            animate = animate,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        subtitle?.let {
            TypewriterText(
                text = it,
                delayBefore = IMAGE_ANIM_DURATION_MS + (title.length * LARGE_CHAR_DELAY_MS) + 200L,
                charDelay = LARGE_CHAR_DELAY_MS,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = dynamicFontSizeSmall),
                fontWeight = FontWeight.ExtraLight,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
                animate = animate,
            )
        }

        action?.let {
            Spacer(modifier = Modifier.height(24.dp))
            it()
        }
    }
}

@Composable
private fun SmallStateMessage(
    title: String,
    subtitle: String?,
    @DrawableRes imgRes: Int?,
    modifier: Modifier,
    animate: Boolean,
    action: (@Composable () -> Unit)?
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        imgRes?.let {
            AnimatedIllustrationImage(
                imgRes = it,
                animate = animate,
                initialScale = 0.8f,
                stiffness = Spring.StiffnessMedium,
                durationMs = SMALL_IMAGE_ANIM_DURATION_MS,
                modifier = Modifier.height(120.dp).padding(bottom = 8.dp)
            )
        }

        TypewriterText(
            text = title,
            delayBefore = SMALL_IMAGE_ANIM_DURATION_MS.toLong(),
            charDelay = SMALL_CHAR_DELAY_MS,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            animate = animate,
        )

        subtitle?.let {
            TypewriterText(
                text = it,
                delayBefore = SMALL_IMAGE_ANIM_DURATION_MS + (title.length * SMALL_CHAR_DELAY_MS) + 100L,
                charDelay = 15L,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraLight,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
                animate = animate,
            )
        }

        action?.let {
            Spacer(modifier = Modifier.height(8.dp))
            it()
        }
    }
}

@Composable
private fun AnimatedIllustrationImage(
    @DrawableRes imgRes: Int,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    initialScale: Float = 0.65f,
    dampingRatio: Float = Spring.DampingRatioMediumBouncy,
    stiffness: Float = Spring.StiffnessLow,
    durationMs: Int = IMAGE_ANIM_DURATION_MS,
) {
    val alpha = remember { Animatable(if (animate) 0f else 1f) }
    val scale = remember { Animatable(if (animate) initialScale else 1f) }
    var hasAnimated by remember { mutableStateOf(!animate) }

    LaunchedEffect(imgRes) {
        if (!hasAnimated) {
            hasAnimated = true
            launch { alpha.animateTo(1f, tween(durationMs)) }
            launch { scale.animateTo(1f, spring(dampingRatio, stiffness)) }
        }
    }

    Image(
        painter = painterResource(imgRes),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            scaleX = scale.value
            scaleY = scale.value
        }
    )
}

@Preview(showBackground = true, name = "StateMessage — Large")
@Composable
private fun StateMessageLargePreview() {
    MonoTaskTheme {
        StateMessage(
            title = "You're all clear!",
            subtitle = "No overdue or open tasks",
            size = IllustrationSize.Large,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "StateMessage — Small")
@Composable
private fun StateMessageSmallPreview() {
    MonoTaskTheme {
        StateMessage(
            title = "You're all clear!",
            subtitle = "No overdue or open tasks",
            size = IllustrationSize.Small,
            modifier = Modifier.padding(16.dp)
        )
    }
}
