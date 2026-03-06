package dev.sagi.monotask.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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

private val PILL_SIZE = 44.dp
private val COMPLETE_COLOR = Color(0xFF4BB24F)
private val SNOOZE_COLOR = Color(0xFFFF511C)
const val SNOOZE_THRESHOLD = 380f
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
    val burstScale = remember { Animatable(1f) }

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
                translationX = offsetX  //
            }
            .circleGlow(
                color   = currentColor.copy(alpha = 0.25f),
                radius  = 20.dp,
                offsetY = 0.dp
            )
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




// ========== Complete pill (slides in from right) ==========

@Composable
fun CompletePill(
    syncedOffset: Float,
    screenWidthPx: Float,
    modifier: Modifier = Modifier
) {
    val progress = (syncedOffset / COMPLETE_THRESHOLD).coerceIn(0f, 1f)
    SwipePill(
        iconRes = R.drawable.ic_check_circle,
        iconTint = COMPLETE_COLOR,
        progress = progress,
        offsetX = screenWidthPx * (1f - progress),
        modifier = modifier
    )
}

// ========== Snooze pill (slides in from left) ==========
@Composable
fun SnoozePill(
    syncedOffset: Float,
    screenWidthPx: Float,
    modifier: Modifier = Modifier
) {
    val progress = (-syncedOffset / SNOOZE_THRESHOLD).coerceIn(0f, 1f)
    SwipePill(
        iconRes = R.drawable.ic_next_plan,
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
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = remember(configuration, density) {
        with(density) { configuration.screenWidthDp.dp.toPx() }
    }
    var offsetX by remember { mutableFloatStateOf(0f) }

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe_offset"
    )
    val maxRotation = if (offsetX > COMPLETE_THRESHOLD || offsetX < -SNOOZE_THRESHOLD) 10f else 6f
    val rotation by animateFloatAsState(
        targetValue = (offsetX / 400f) * maxRotation,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe_rotation"
    )

    val completeReady = offsetX > COMPLETE_THRESHOLD
    val snoozeReady = offsetX < -SNOOZE_THRESHOLD

    // Haptic feedback upon swipe completion
    val haptic = LocalHapticFeedback.current
    val wasCompleteReady = remember { mutableStateOf(false) }
    val wasSnoozeReady = remember { mutableStateOf(false) }

    LaunchedEffect(completeReady) {
        if (completeReady && !wasCompleteReady.value)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        wasCompleteReady.value = completeReady
    }
    LaunchedEffect(snoozeReady) {
        if (snoozeReady && !wasSnoozeReady.value)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        wasSnoozeReady.value = snoozeReady
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        FocusCard(
            task = task,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .graphicsLayer { rotationZ = rotation }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > COMPLETE_THRESHOLD -> { onSwipeRight(); offsetX = 0f }
                                offsetX < -SNOOZE_THRESHOLD -> { onSwipeLeft(); offsetX = 0f }
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

        CompletePill(
            syncedOffset = animatedOffset,
            screenWidthPx = screenWidthPx,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
        )

        SnoozePill(
            syncedOffset = animatedOffset,
            screenWidthPx = screenWidthPx,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
        )
    }
}

