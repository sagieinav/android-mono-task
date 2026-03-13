package dev.sagi.monotask.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.theme.circleGlow
import dev.sagi.monotask.ui.theme.glassBorder
import kotlin.math.roundToInt

private val PILL_SIZE    = 44.dp
private val COMPLETE_COLOR = Color(0xFF4BB24F)
private val SNOOZE_COLOR   = Color(0xFFFF511C)
const val SNOOZE_THRESHOLD   = 380f
const val COMPLETE_THRESHOLD = 380f

// ========== Generic pill ==========
@Composable
fun SwipePill(
    iconRes: Int,
    iconTint: Color,
    progress: Float,
    offsetX: Float,
    baseScale: Float = 1f,
    maxScale: Float = 1.6f,
    modifier: Modifier = Modifier
) {
    val isTriggered = progress >= 1f
    val burstScale  = remember { Animatable(1f) }

    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            burstScale.snapTo(1.2f)
            burstScale.animateTo(
                targetValue   = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessLow
                )
            )
        } else {
            burstScale.snapTo(1f)
        }
    }

    val currentColor = lerp(
        start    = MaterialTheme.colorScheme.outlineVariant,
        stop     = iconTint,
        fraction = (progress * progress * 1.2f).coerceIn(0f, 1f)
    )

    Box(
        modifier = modifier
            .size(PILL_SIZE * maxScale)
            .graphicsLayer {
                val currentScale = lerp(baseScale / maxScale, 1f, progress) * burstScale.value
                scaleX       = currentScale
                scaleY       = currentScale
                translationX = offsetX
            }
            .circleGlow(color = currentColor.copy(alpha = 0.25f), radius = 20.dp, offsetY = 0.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f + progress / 2f))
            .glassBorder(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter            = painterResource(iconRes),
            contentDescription = null,
            tint               = currentColor,
            modifier           = Modifier.fillMaxSize().scale(1.43f)
        )
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
        iconRes  = R.drawable.ic_check_circle,
        iconTint = COMPLETE_COLOR,
        progress = progress,
        offsetX  = screenWidthPx * (1f - progress),
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
        iconRes  = R.drawable.ic_next_plan,
        iconTint = SNOOZE_COLOR,
        progress = progress,
        offsetX  = -screenWidthPx * (1f - progress),
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
    onSnoozeCardExited: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density       = LocalDensity.current
    val screenWidthPx = remember(configuration, density) {
        with(density) { configuration.screenWidthDp.dp.toPx() }
    }

    var badgeOffsetX  by remember { mutableFloatStateOf(0f) }
    var offsetX       by remember { mutableFloatStateOf(0f) }
    var exitDirection by remember { mutableStateOf<SwipeExitDirection?>(null) }
    val isExiting = exitDirection != null
    val isSwiping = offsetX != 0f || isExiting

    LaunchedEffect(exitTrigger, task.id) {
        if (exitTrigger == SwipeExitDirection.LEFT) {
            exitDirection = SwipeExitDirection.LEFT
        }
    }

    val pillAlpha by animateFloatAsState(
        targetValue   = if (isExiting) 0f else 1f,
        animationSpec = tween(120),
        label         = "pill_alpha"
    )

    val cardTargetOffset = when (exitDirection) {
        SwipeExitDirection.RIGHT -> screenWidthPx * 1.5f
        SwipeExitDirection.LEFT  -> -screenWidthPx * 1.5f
        null                     -> offsetX
    }

    val animatedOffset by animateFloatAsState(
        targetValue   = cardTargetOffset,
        animationSpec = if (isExiting)
            tween(280, easing = FastOutLinearInEasing)
        else
            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe_offset",
        finishedListener = {
            when (exitDirection) {
                SwipeExitDirection.RIGHT -> onSwipeRight()
                SwipeExitDirection.LEFT  -> onSnoozeCardExited()
                null                     -> {}
            }
        }
    )

    val haptic = LocalHapticFeedback.current

    var wasCompleteReady by remember { mutableStateOf(false) }
    var wasSnoozeReady   by remember { mutableStateOf(false) }
    val completeReady = offsetX > COMPLETE_THRESHOLD
    val snoozeReady   = offsetX < -SNOOZE_THRESHOLD

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
                .graphicsLayer { rotationZ = (animatedOffset / screenWidthPx) * 18f }
                .pointerInput(isExiting) {
                    if (isExiting) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > COMPLETE_THRESHOLD -> {
                                    badgeOffsetX = offsetX
                                    exitDirection = SwipeExitDirection.RIGHT
                                }

                                offsetX < -SNOOZE_THRESHOLD -> {
                                    offsetX = 0f; onSwipeLeft()
                                }

                                else -> offsetX = 0f
                            }
                        },
                        onDragCancel = { offsetX = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-400f, 400f)
                        }
                    )
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
                    .graphicsLayer { alpha = pillAlpha }
            )
            SnoozePill(
                syncedOffset = animatedOffset,
                screenWidthPx = screenWidthPx,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .graphicsLayer { alpha = pillAlpha }
            )
        }
    }
}


enum class SwipeExitDirection { RIGHT, LEFT }
