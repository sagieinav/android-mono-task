package dev.sagi.monotask.ui.focus.components

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.designsystem.animation.MonoAnimations
import dev.sagi.monotask.designsystem.gesture.SwipeExitDirection
import dev.sagi.monotask.designsystem.gesture.SwipeState
import dev.sagi.monotask.designsystem.gesture.swipeGestures
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.circleGlow
import dev.sagi.monotask.designsystem.theme.glassBackground
import dev.sagi.monotask.designsystem.theme.glassBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ========== Swipe Constants ==========
private val PILL_SIZE = 44.dp
private val GLOW_INSET = 24.dp // extra padding so shadow fits within the RenderNode texture
private val COMPLETE_COLOR = Color(0xFF4BB24F)
private val SNOOZE_COLOR = Color(0xFFFF511C)
const val SWIPE_THRESHOLD = 380f // drag distance (px) to trigger complete or snooze
private const val MAX_DRAG_DISTANCE = 400f // hard limit for drag range (px)
private const val MAX_ROTATION_DEG = 18f // card tilt at full drag
private const val EXIT_MULTIPLIER = 1.5f // off-screen exit distance multiplier
private const val PILL_MAX_SCALE = 1.8f
private const val PILL_START_SCALE = 1f / PILL_MAX_SCALE // pill scale at progress = 0


// ========== Swipeable Card (entry + swipe animations) ==========
@Composable
fun FocusCardSwipeable(
    task: Task,
    restoreVersion: Int,
    animState: FocusAnimationState,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: () -> Unit = {}
) {
    val swipe = remember { SwipeState() }
    val haptic = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = remember(configuration, density) {
        with(density) { configuration.screenWidthDp.dp.toPx() }
    }

    // ========== Entry Animation ==========

    SideEffect {
        animState.checkIfNeedsReset(task.id, restoreVersion)
    }

    LaunchedEffect(task.id, restoreVersion) {
        val isSlideIn = animState.resetCard()
        launch {
            delay(200)
            animState.border.animateTo(1f, tween(MonoAnimations.BORDER_ANIM_MS, easing = LinearEasing))
        }
        if (isSlideIn) {
            launch { animState.alpha.animateTo(1f, tween(MonoAnimations.CARD_ENTRY_FADE_MS)) }
            launch { animState.offsetX.animateTo(0f, tween(MonoAnimations.CARD_ENTRY_SLIDE_MS, easing = FastOutSlowInEasing)) }
        } else {
            val entrySpec = spring<Float>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
            launch { animState.alpha.animateTo(1f, tween(300)) }
            launch { animState.scale.animateTo(1f, entrySpec) }
        }
    }

    // ========== Swipe Exit ==========

    LaunchedEffect(animState.snoozeExitTrigger, task.id) {
        if (animState.snoozeExitTrigger == SwipeExitDirection.LEFT) swipe.exitDirection = SwipeExitDirection.LEFT
    }

    val cardTargetOffset = when (swipe.exitDirection) {
        SwipeExitDirection.RIGHT ->  screenWidthPx * EXIT_MULTIPLIER
        SwipeExitDirection.LEFT -> -screenWidthPx * EXIT_MULTIPLIER
        null ->  swipe.offsetX
    }

    val animatedOffset by animateFloatAsState(
        targetValue = cardTargetOffset,
        animationSpec = if (swipe.isExiting)
            tween(MonoAnimations.CARD_EXIT_MS, easing = FastOutLinearInEasing)
        else
            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe_offset"
    )
    val pillAlpha by animateFloatAsState(
        targetValue = if (swipe.isExiting) 0f else 1f,
        animationSpec = tween(120),
        label = "pill_alpha"
    )
    val lpScale by animateFloatAsState(
        targetValue = if (swipe.isLongPressPending) 0.975f else 1f,
        animationSpec = tween(150),
        label = "lp_scale"
    )
    val lpAlpha by animateFloatAsState(
        targetValue = if (swipe.isLongPressPending) 0.88f else 1f,
        animationSpec = tween(150),
        label = "lp_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = animState.displayAlpha
                scaleX = animState.displayScale
                scaleY = animState.displayScale
                translationX = animState.displayOffsetX
            },
        contentAlignment = Alignment.Center
    ) {
        FocusCard(
            task = task,
            entryProgressProvider = { animState.displayBorder },
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .graphicsLayer {
                    rotationZ = (animatedOffset / screenWidthPx) * MAX_ROTATION_DEG
                    scaleX = lpScale
                    scaleY = lpScale
                    alpha = lpAlpha
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                }
                .swipeGestures(
                    state = swipe,
                    haptic = haptic,
                    onSwipeRight = {
                        animState.setNextEntryDirection(SwipeExitDirection.RIGHT, screenWidthPx)
                        onSwipeRight()
                    },
                    onSwipeLeft = onSwipeLeft,
                    onLongPress = onLongPress,
                    swipeThreshold = SWIPE_THRESHOLD,
                    maxDragDistance = MAX_DRAG_DISTANCE
                )
        )

        if (swipe.isSwiping) {
            CompletePill(
                syncedOffset = animatedOffset,
                screenWidthPx = screenWidthPx,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .offset(x = -GLOW_INSET)   // shifts layout left so the outer hardware layer
                    .graphicsLayer { alpha = pillAlpha }  // has room for the glow on the left
            )
            SnoozePill(
                syncedOffset = animatedOffset,
                screenWidthPx = screenWidthPx,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .offset(x = GLOW_INSET)    // shifts layout right so the outer hardware layer
                    .graphicsLayer { alpha = pillAlpha }  // has room for the glow on the right
            )
        }
    }
}


// ========== Generic Pill ==========
@Composable
private fun SwipePill(
    iconRes: Int,
    iconTint: Color,
    progress: Float,
    offsetX: Float,
    modifier: Modifier = Modifier
) {
    val isTriggered = progress >= 1f
    val burstScale = remember { Animatable(1f) }

    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            burstScale.snapTo(1.2f)
            burstScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            burstScale.snapTo(1f)
        }
    }

    val dynamicColor = lerp(
        start = MaterialTheme.colorScheme.outlineVariant,
        stop = iconTint,
        fraction = (progress * progress * 1.2f).coerceIn(0f, 1f)
    )

    // Outer box enlarged by GLOW_INSET so the shadow from circleGlow fits within the
    // RenderNode bounds and doesn't get clipped to a rectangle.
    Box(
        modifier = modifier
            .size(PILL_SIZE * PILL_MAX_SCALE + GLOW_INSET * 2)
            .graphicsLayer {
                val currentScale = lerp(PILL_START_SCALE, 1f, progress) * burstScale.value
                scaleX = currentScale
                scaleY = currentScale
                translationX = offsetX
            }
            .circleGlow(
                color = dynamicColor.copy(alpha = 0.3f),
                radius = 32.dp,
                circleRadius = PILL_SIZE * PILL_MAX_SCALE / 2,
                offsetY = 0.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(PILL_SIZE * PILL_MAX_SCALE)
                .clip(CircleShape)
                .glassBackground(
                    accentColor = dynamicColor,
                    baseColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f)
                )
                .glassBorder(CircleShape, dynamicColor, width = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = dynamicColor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


// ========== Complete Pill ==========
@Composable
private fun CompletePill(
    syncedOffset: Float,
    screenWidthPx: Float,
    modifier: Modifier = Modifier
) {
    val progress = (syncedOffset / SWIPE_THRESHOLD).coerceIn(0f, 1f)
    SwipePill(
        iconRes = IconPack.Check,
        iconTint = COMPLETE_COLOR,
        progress = progress,
        offsetX = screenWidthPx * (1f - progress),
        modifier = modifier
    )
}


// ========== Snooze Pill ==========
@Composable
private fun SnoozePill(
    syncedOffset: Float,
    screenWidthPx: Float,
    modifier: Modifier = Modifier
) {
    val progress = (-syncedOffset / SWIPE_THRESHOLD).coerceIn(0f, 1f)
    SwipePill(
        iconRes = IconPack.NextTaskAlt,
        iconTint = SNOOZE_COLOR,
        progress = progress,
        offsetX = -screenWidthPx * (1f - progress),
        modifier = modifier
    )
}


// ========== Preview ==========
@Preview(showBackground = true, name = "SwipePill, Idle")
@Composable
private fun SwipePillPreview() {
    MonoTaskTheme {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SwipePill(
                iconRes = IconPack.Check,
                iconTint = Color(0xFF4BB24F),
                progress = 0.3f,
                offsetX  = 0f
            )
            SwipePill(
                iconRes = IconPack.NextTaskAlt,
                iconTint = Color(0xFFFF511C),
                progress = 1.0f,
                offsetX = 0f
            )
        }
    }
}
