package dev.sagi.monotask.designsystem.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.glassBackground
import dev.sagi.monotask.designsystem.theme.glassBorder
import kotlin.math.roundToInt


@Composable
fun MonoSlider(
    value                 : Float,
    onValueChange         : (Float) -> Unit,
    modifier              : Modifier = Modifier,
    onValueChangeFinished : (Float) -> Unit = {},
    stepCount             : Int = 11,
    showTicks             : Boolean = true,
    hapticEnabled         : Boolean = true,
    trackHeight           : Dp = 10.dp,
    thumbWidth            : Dp = 6.dp,
    thumbHeight           : Dp = 24.dp,
) {
    val density   = LocalDensity.current
    val haptic    = LocalHapticFeedback.current

    // Snap incoming value to the nearest step position
    val currentStep  = (value * (stepCount - 1)).roundToInt().coerceIn(0, stepCount - 1)
    val snappedValue = currentStep / (stepCount - 1).toFloat()

    // Layout-dependent usable width stored in a stable ref
    val usableWidthPxRef = remember { mutableFloatStateOf(0f) }

    // Raw drag offset in pixels: updated every frame during drag
    var rawOffsetPx by remember { mutableFloatStateOf(0f) }
    var isDragging  by remember { mutableStateOf(false) }

    // Sync thumb position with external value changes when not dragging
    LaunchedEffect(snappedValue, usableWidthPxRef.floatValue) {
        if (!isDragging) rawOffsetPx = snappedValue * usableWidthPxRef.floatValue
    }

    // Fraction to display: live during drag, snapped otherwise
    val displayFraction = if (isDragging && usableWidthPxRef.floatValue > 0f)
        (rawOffsetPx / usableWidthPxRef.floatValue).coerceIn(0f, 1f)
    else
        snappedValue

    // Haptic tick on each discrete step change while dragging
    val displayStep = (displayFraction * (stepCount - 1)).roundToInt().coerceIn(0, stepCount - 1)
    var lastHapticStep by remember { mutableIntStateOf(-1) }
    LaunchedEffect(displayStep) {
        if (hapticEnabled && lastHapticStep != -1 && displayStep != lastHapticStep) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        lastHapticStep = displayStep
    }

    BoxWithConstraints(modifier = modifier) {
        val thumbWidthPx  = with(density) { thumbWidth.toPx() }
        val usableWidthPx = constraints.maxWidth.toFloat() - thumbWidthPx
        usableWidthPxRef.floatValue = usableWidthPx

        val thumbOffsetPx = displayFraction * usableWidthPx

        // Animate:
        // During drag: near-instant (StiffnessVeryHigh) so thumb follows finger with no lag
        // After drag: spring to snapped position for a satisfying snap feel
        val animatedOffsetDp by animateDpAsState(
            targetValue   = with(density) { thumbOffsetPx.toDp() },
            animationSpec = if (isDragging)
                spring(stiffness = Spring.StiffnessHigh)
            else
                spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy),
            label = "mono_slider_thumb"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbHeight)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            val finalStep  = (rawOffsetPx / usableWidthPxRef.floatValue * (stepCount - 1))
                                .roundToInt().coerceIn(0, stepCount - 1)
                            val finalValue = finalStep / (stepCount - 1).toFloat()
                            rawOffsetPx    = finalValue * usableWidthPxRef.floatValue
                            onValueChange(finalValue)
                            onValueChangeFinished(finalValue)
                        },
                        onDragCancel = {
                            isDragging  = false
                            rawOffsetPx = snappedValue * usableWidthPxRef.floatValue
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            rawOffsetPx = (rawOffsetPx + dragAmount).coerceIn(0f, usableWidthPxRef.floatValue)
                            onValueChange(rawOffsetPx / usableWidthPxRef.floatValue)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Track (fills full width, centered vertically in the touch target)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .align(Alignment.Center)
            ) {
                // Inactive track: full width, drawn first (behind active)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .glassBorder(CircleShape, width = 1.dp)
                        .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceVariant)
                )

                // Active track: left portion up to the thumb center
                val activeWidthDp = animatedOffsetDp + thumbWidth / 2
                Box(
                    modifier = Modifier
                        .width(activeWidthDp)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .glassBorder(CircleShape, width = 1.dp, color = MaterialTheme.colorScheme.primaryContainer)
                        .glassBackground(baseColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                )

                // Tick marks: dots on both filled and unfilled regions
                if (showTicks && stepCount > 1) {
                    val tickUnfilled  = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    val tickFilled    = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                    val thumbCenterPx = thumbOffsetPx + thumbWidthPx / 2
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val spacing  = size.width / (stepCount - 1)
                        val dotRadius = 1.5.dp.toPx()
                        for (i in 1 until stepCount - 1) {
                            val x     = i * spacing
                            val color = if (x < thumbCenterPx - 4f) tickFilled else tickUnfilled
                            drawCircle(
                                color = color,
                                radius = dotRadius,
                                center = Offset(x, size.height / 2f)
                            )
                        }
                    }
                }
            }

            // Thumb pill: positioned via offset, drawn on top of track
            Box(
                modifier = Modifier
                    .offset(x = animatedOffsetDp)
                    .width(thumbWidth)
                    .height(thumbHeight)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.3f),
                        spotColor = Color.Black.copy(alpha = 0.6f)
                    )
                    .clip(CircleShape)
                    .glassBorder(CircleShape)
                    .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainerLow)
            )
        }
    }
}


// =========================================
// Previews
// =========================================

@Preview(showBackground = true, name = "MonoSlider - start")
@Composable
private fun MonoSliderStartPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MonoSlider(
                value         = 0f,
                onValueChange = {},
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "MonoSlider - middle")
@Composable
private fun MonoSliderMiddlePreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MonoSlider(
                value         = 0.5f,
                onValueChange = {},
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "MonoSlider - end")
@Composable
private fun MonoSliderEndPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MonoSlider(
                value = 1f,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

