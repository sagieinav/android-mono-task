package dev.sagi.monotask.ui.focus.component

import androidx.compose.animation.AnimatedVisibility
import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.component.MonoLabel
import dev.sagi.monotask.designsystem.theme.LocalCustomColors
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.customColors
import dev.sagi.monotask.designsystem.theme.glassBorder
import dev.sagi.monotask.ui.focus.FocusUiEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun XpLabel(xp: Int, modifier: Modifier = Modifier) {
    val color = MaterialTheme.customColors.xp
    MonoLabel(
        label = "$xp XP",
        color = color,
        modifier = modifier,
        leadingContent = {
            Icon(
                painter = painterResource(IconPack.Xp),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        }
    )
}


// ========== XpLabelCompletion ==========

private const val XP_POP_SCALE        = 1.8f
private const val XP_INITIAL_OFFSET_Y = -30f
private const val XP_EXIT_OFFSET_Y    = -200f
private const val SHIMMER_HALF_WIDTH  = 0.2f
private const val SHIMMER_SWEEP_SCALE = 1.4f

@Composable
fun XpLabelCompletion(
    xpDelta: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val scale   = remember { Animatable(0f) }
    val offsetY = remember { Animatable(XP_INITIAL_OFFSET_Y) }
    val alpha   = remember { Animatable(0f) }
    val shimmer = remember { Animatable(0f) }
    val color   = MaterialTheme.customColors.xp

    LaunchedEffect(visible) {
        if (visible) {
            scale.snapTo(1f)
            offsetY.snapTo(XP_INITIAL_OFFSET_Y)
            alpha.snapTo(1f)
            shimmer.snapTo(0f)

            // 1. Pop
            scale.animateTo(
                XP_POP_SCALE,
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )

            // 2. Shimmer
            shimmer.animateTo(1f, tween(durationMillis = 400, easing = EaseInOutQuart))

            // 3. Float up & fade out
            launch {
                offsetY.animateTo(XP_EXIT_OFFSET_Y, tween(400, easing = EaseInOutCubic))
            }
            alpha.animateTo(0f, tween(300))
        } else {
            scale.snapTo(0f)
            alpha.snapTo(0f)
            shimmer.snapTo(0f)
            offsetY.snapTo(XP_INITIAL_OFFSET_Y)
        }
    }

    val shimmerCenter = shimmer.value * SHIMMER_SWEEP_SCALE - SHIMMER_HALF_WIDTH
    val textBrush = if (shimmer.value in 0.01f..0.99f) {
        Brush.horizontalGradient(
            0f                                              to color,
            (shimmerCenter - SHIMMER_HALF_WIDTH).coerceIn(0f, 1f) to color,
            shimmerCenter.coerceIn(0f, 1f)                        to Color.White,
            (shimmerCenter + SHIMMER_HALF_WIDTH).coerceIn(0f, 1f) to color,
            1f                                                     to color
        )
    } else null

    Text(
        text = "+$xpDelta XP",
        style = MaterialTheme.typography.labelLarge.copy(
            brush = textBrush,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Proportional,
                trim = LineHeightStyle.Trim.Both
            )
        ),
        color = if (textBrush == null) color else Color.Unspecified,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            translationY = offsetY.value
            this.alpha = alpha.value
            transformOrigin = TransformOrigin(1f, 1f)
            rotationZ = 7f
        }
    )
}

@Composable
fun LevelUpBadge(
    event: FocusUiEffect.ShowLevelUp?,
    onAnimationEnd: () -> Unit
) {
    val customColors = LocalCustomColors.current
    val shape        = RoundedCornerShape(50)

    // Keep last non-null value so text doesn't blank during the exit animation
    val displayEvent = remember { mutableStateOf(event) }
    LaunchedEffect(event) { if (event != null) displayEvent.value = event }

    // Auto-dismiss after 2.5s
    LaunchedEffect(event) {
        if (event != null) {
            delay(2500)
            onAnimationEnd()
        }
    }

    AnimatedVisibility(
        visible = event != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec  = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMedium
            )
        ) + scaleIn(
            initialScale  = 0.7f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMedium
            )
        ) + fadeIn(tween(150)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250, easing = FastOutLinearInEasing)
        ) + fadeOut(tween(200))
    ) {
        displayEvent.value?.let { ev ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(shape)
                    .background(customColors.xp.copy(alpha = 0.12f))
                    .glassBorder(shape, color = customColors.xp)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    painter            = painterResource(IconPack.Upgrade),
                    contentDescription = null,
                    tint               = customColors.xp,
                    modifier           = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text       = "Lv. ${ev.previousLevel} → ${ev.newLevel}",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = customColors.xp
                )
            }
        }
    }
}



// ========== Previews ==========

@Preview(showBackground = true)
@Composable
private fun XpLabelCompletionPreview() {
    MonoTaskTheme {
        var visible by remember { mutableStateOf(false) }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(64.dp)
        ) {
            Button(onClick = { visible = true }) {
                Text("Complete Task")
            }
            XpLabelCompletion(
                xpDelta = 100,
                visible = visible,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-16).dp)
            )
        }

        LaunchedEffect(visible) {
            if (visible) {
                delay(2000)
                visible = false
            }
        }
    }
}
