package dev.sagi.monotask.ui.component.core

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.penaltyRed
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// Dynamic layout constants
private val MAX_PILL_WIDTH         = 84.dp
private val PILL_GAP               = 8.dp
private const val PILL_WIDTH_RATIO = 1.25f // Pill width will be 1.25x the row's height
private const val REVEAL_THRESHOLD = 0.7f  // Must swipe 70% of the pill's width to trigger

data class SwipeRevealAction(
    val color      : Color,
    @DrawableRes val icon : Int,
    val label      : String,
    val onTriggered: () -> Unit
)

@Composable
fun SwipeRevealRow(
    modifier    : Modifier           = Modifier,
    shape       : Shape              = CircleShape,
    endAction   : SwipeRevealAction? = null,
    startAction : SwipeRevealAction? = null,
    content     : @Composable () -> Unit
) {
    val density = LocalDensity.current
    val haptic  = LocalHapticFeedback.current

    var pillWidthDp by remember { mutableStateOf(MAX_PILL_WIDTH) }
    val pillPx      = with(density) { pillWidthDp.toPx() }
    val maxDragPx   = with(density) { (pillWidthDp + PILL_GAP).toPx() }
    val threshPx    = pillPx * REVEAL_THRESHOLD

    val offsetX = remember { Animatable(0f) }
    val scope   = rememberCoroutineScope()

    val gestureModifier = if (endAction != null || startAction != null) {
        Modifier.pointerInput(endAction, startAction, pillWidthDp) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    val offset = offsetX.value
                    when {
                        offset < -threshPx && endAction   != null -> endAction.onTriggered()
                        offset >  threshPx && startAction != null -> startAction.onTriggered()
                    }
                    scope.launch {
                        offsetX.animateTo(0f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow))
                    }
                },
                onDragCancel = {
                    scope.launch { offsetX.animateTo(0f, spring(Spring.DampingRatioNoBouncy)) }
                },
                onHorizontalDrag = { _, dragAmount ->
                    val minOff = if (endAction   != null) -maxDragPx else 0f
                    val maxOff = if (startAction != null)  maxDragPx else 0f

                    val prevOffset = offsetX.value
                    val targetOffset = (prevOffset + dragAmount).coerceIn(minOff, maxOff)

                    // Check for threshold crossing in THIS specific frame only
                    val wasOver = abs(prevOffset) >= threshPx
                    val isOver  = abs(targetOffset) >= threshPx

                    if (!wasOver && isOver) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }

                    scope.launch {
                        offsetX.snapTo(targetOffset)
                    }
                }
            )
        }
    } else Modifier

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { size ->
                val heightDp = with(density) { size.height.toDp() }
                val calculatedWidth = heightDp * PILL_WIDTH_RATIO
                pillWidthDp = minOf(MAX_PILL_WIDTH, calculatedWidth)
            }
            .then(gestureModifier)
    ) {

        if (endAction != null) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                SwipePill(
                    action   = endAction,
                    shape    = shape,
                    modifier = Modifier.fillMaxHeight().width(pillWidthDp),
                    alpha    = { (-offsetX.value / pillPx).coerceIn(0f, 1f) }
                )
            }
        }

        if (startAction != null) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                SwipePill(
                    action   = startAction,
                    shape    = shape,
                    modifier = Modifier.fillMaxHeight().width(pillWidthDp),
                    alpha    = { (offsetX.value / pillPx).coerceIn(0f, 1f) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        ) {
            content()
        }
    }
}

@Composable
private fun SwipePill(
    action  : SwipeRevealAction,
    shape   : Shape,
    modifier: Modifier = Modifier,
    alpha   : () -> Float
) {
    GlassSurface(
        modifier    = modifier.graphicsLayer { this.alpha = alpha() },
        shape       = shape,
        accentColor = action.color,
        blurred     = false
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter            = painterResource(action.icon),
                contentDescription = action.label,
                tint               = action.color.copy(alpha = 0.7f),
                modifier           = Modifier
                    .fillMaxHeight(0.45f)
                    .aspectRatio(1f)
            )
        }
    }
}

// ========== Preview ==========

@Preview(showBackground = true, name = "SwipeRevealRow – end action (delete)")
@Composable
private fun SwipeRevealRowEndPreview() {
    MonoTaskTheme {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val defaultShape = RoundedCornerShape(16.dp)

            SwipeRevealRow(
                shape = defaultShape,
                endAction = SwipeRevealAction(
                    color       = penaltyRed,
                    icon        = R.drawable.ic_delete,
                    label       = "Delete",
                    onTriggered = {}
                )
            ) {
                GlassSurface(modifier = Modifier.fillMaxWidth(), shape = defaultShape) {
                    Text(
                        text     = "Swipe left to delete",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}