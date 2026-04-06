package dev.sagi.monotask.ui.focus.components

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.circleGlow
import dev.sagi.monotask.designsystem.theme.glassBackground
import dev.sagi.monotask.designsystem.theme.glassBorder
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ========== Swipe Constants ==========
private val PILL_SIZE = 44.dp
private val GLOW_INSET = 24.dp // extra padding around pill so shadow fits within the RenderNode texture (shadow radius = 20dp + 4dp buffer)
private val COMPLETE_COLOR = Color(0xFF4BB24F)
private val SNOOZE_COLOR = Color(0xFFFF511C)
const val SNOOZE_THRESHOLD = 380f // drag distance (px) to trigger snooze
const val COMPLETE_THRESHOLD = 380f // drag distance (px) to trigger complete
private const val MAX_DRAG_DISTANCE = 400f // hard limit for drag range (px)
private const val MAX_ROTATION_DEG = 18f // card tilt at full drag
private const val EXIT_MULTIPLIER = 1.5f // off-screen exit distance multiplier
private const val EXIT_ANIM_DURATION = 280 // exit animation duration (ms)

// ========== Generic pill ==========
@Composable
fun SwipePill(
    iconRes: Int,
    iconTint: Color,
    progress: Float,
    offsetX: Float,
    modifier: Modifier = Modifier,
    baseScale: Float = 1f,
    maxScale: Float = 1.8f
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

    // Outer box is enlarged by GLOW_INSET on each side so the shadow from circleGlow
    // fits within the RenderNode bounds and doesn't get clipped to a rectangle.
    Box(
        modifier = modifier
            .size(PILL_SIZE * maxScale + GLOW_INSET * 2)
            .graphicsLayer {
                val currentScale = lerp(baseScale / maxScale, 1f, progress) * burstScale.value
                scaleX = currentScale
                scaleY = currentScale
                translationX = offsetX
            }
            .circleGlow(
                color = dynamicColor.copy(alpha = 0.3f),
                radius = 32.dp,
                circleRadius = PILL_SIZE * maxScale / 2,
                offsetY = 0.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner box: actual pill visual (clip + background)
        Box(
            modifier = Modifier
                .size(PILL_SIZE * maxScale)
                .clip(CircleShape)
                .glassBackground(
                    accentColor = dynamicColor,
                    baseColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f)
                )

                .glassBorder(CircleShape, dynamicColor, width = 4.dp)
            ,
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

// ========== Complete pill ==========
@Composable
fun CompletePill(
    syncedOffset: Float,
    screenWidthPx: Float,
    modifier: Modifier = Modifier
) {
    val progress = (syncedOffset / COMPLETE_THRESHOLD).coerceIn(0f, 1f)
    SwipePill(
        iconRes = IconPack.Check,
        iconTint = COMPLETE_COLOR,
        progress = progress,
        offsetX = screenWidthPx * (1f - progress),
        modifier = modifier
    )
}

// ========== Snooze pill ==========
@Composable
fun SnoozePill(
    syncedOffset: Float,
    screenWidthPx: Float,
    modifier: Modifier = Modifier
) {
    val progress = (-syncedOffset / SNOOZE_THRESHOLD).coerceIn(0f, 1f)
    SwipePill(
        iconRes = IconPack.NextTaskAlt,
        iconTint = SNOOZE_COLOR,
        progress = progress,
        offsetX = -screenWidthPx * (1f - progress),
        modifier = modifier
    )
}

// ========== Swipeable wrapper ==========
@Composable
fun FocusCardSwipeable(
    task: Task,
    exitTrigger: SwipeExitDirection?,
    borderFraction: Float,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = remember(configuration, density) {
        with(density) { configuration.screenWidthDp.dp.toPx() }
    }

    var badgeOffsetX by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var exitDirection by remember { mutableStateOf<SwipeExitDirection?>(null) }
    val isExiting = exitDirection != null
    val isSwiping = offsetX != 0f || isExiting

    LaunchedEffect(exitTrigger, task.id) {
        if (exitTrigger == SwipeExitDirection.LEFT) {
            exitDirection = SwipeExitDirection.LEFT
        }
    }

    val pillAlpha by animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = tween(120),
        label = "pill_alpha"
    )

    val cardTargetOffset = when (exitDirection) {
        SwipeExitDirection.RIGHT -> screenWidthPx * EXIT_MULTIPLIER
        SwipeExitDirection.LEFT -> -screenWidthPx * EXIT_MULTIPLIER
        null -> offsetX
    }

    val animatedOffset by animateFloatAsState(
        targetValue = cardTargetOffset,
        animationSpec = if (isExiting)
            tween(EXIT_ANIM_DURATION, easing = FastOutLinearInEasing)
        else
            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe_offset"
    )

    val haptic = LocalHapticFeedback.current

    var isLongPressPending by remember { mutableStateOf(false) }
    val lpScale by animateFloatAsState(
        if (isLongPressPending) 0.975f
                    else 1f,
        tween(150),
        label = "lp_scale"
    )
    val lpAlpha by animateFloatAsState(
        if (isLongPressPending) 0.88f
                    else 1f,
        tween(150),
        label = "lp_alpha"
    )

    var wasCompleteReady by remember { mutableStateOf(false) }
    var wasSnoozeReady by remember { mutableStateOf(false) }
    val completeReady = offsetX > COMPLETE_THRESHOLD
    val snoozeReady = offsetX < -SNOOZE_THRESHOLD

    LaunchedEffect(completeReady) {
        if (completeReady && !wasCompleteReady)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        wasCompleteReady = completeReady
    }
    LaunchedEffect(snoozeReady) {
        if (snoozeReady && !wasSnoozeReady)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        wasSnoozeReady = snoozeReady
    }

    val showXpBadge = exitDirection == SwipeExitDirection.RIGHT

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center) {
        FocusCard(
            task = task,
            borderFraction = borderFraction,
            hideXpLabel = showXpBadge,
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
                .pointerInput(isExiting) {
                    if (isExiting) return@pointerInput
                    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
                    val touchSlop = viewConfiguration.touchSlop

                    coroutineScope {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var isDragging = false
                            var accumulatedDrag = 0f
                            isLongPressPending = true

                            val longPressJob = launch {
                                delay(longPressTimeout)
                                isLongPressPending = false
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPress()
                            }

                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                    if (!change.pressed) {
                                        longPressJob.cancel()
                                        isLongPressPending = false
                                        change.consume()
                                        if (isDragging) {
                                            when {
                                                offsetX > COMPLETE_THRESHOLD -> {
                                                    badgeOffsetX = offsetX
                                                    exitDirection = SwipeExitDirection.RIGHT
                                                    onSwipeRight() // fire immediately so freeze is set before any Firestore snapshot
                                                }
                                                offsetX < -SNOOZE_THRESHOLD -> {
                                                    offsetX = 0f; onSwipeLeft()
                                                }
                                                else -> offsetX = 0f
                                            }
                                        }
                                        break
                                    }

                                    if (change.positionChanged()) {
                                        val dx = change.positionChange().x
                                        accumulatedDrag += dx

                                        if (!isDragging && !longPressJob.isCompleted) {
                                            if (abs(accumulatedDrag) > touchSlop) {
                                                isDragging = true
                                                longPressJob.cancel()
                                                isLongPressPending = false
                                            }
                                        }

                                        if (isDragging) {
                                            change.consume()
                                            offsetX = (offsetX + dx).coerceIn(-MAX_DRAG_DISTANCE, MAX_DRAG_DISTANCE)
                                        }
                                    }
                                }
                            } catch (_: CancellationException) {
                                longPressJob.cancel()
                                isLongPressPending = false
                                if (isDragging) offsetX = 0f
                            }
                        }
                    }
                }
        )

        if (showXpBadge) {
            XpLabelCompletion(
                xpDelta = task.currentXp,
                visible = true,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 20.dp, top = 20.dp)
                    .offset { IntOffset(badgeOffsetX.roundToInt(), 0) }
            )
        }

        if (isSwiping) {
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


enum class SwipeExitDirection { RIGHT, LEFT }


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
