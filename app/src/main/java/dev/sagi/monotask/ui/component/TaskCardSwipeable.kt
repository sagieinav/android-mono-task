package dev.sagi.monotask.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Task
import kotlin.math.roundToInt



@Composable
fun TaskCardSwipeable(
    task: Task,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe"
    )

    val snoozeThreshold = 300f
    val completeThreshold = 360f

    val completeReady = offsetX > completeThreshold
    val snoozeReady = offsetX < -snoozeThreshold

    val bgColor by animateColorAsState(
        targetValue = when {
            offsetX > 60f  -> Color(0xFF4CAF50).copy(alpha = (offsetX / completeThreshold).coerceIn(0f, 0.4f))
            offsetX < -60f -> Color(0xFFF44336).copy(alpha = (-offsetX / snoozeThreshold).coerceIn(0f, 0.4f))
            else -> Color.Transparent
        },
        label = "swipe_bg"
    )

    Box(modifier = modifier.fillMaxWidth()) {

        // Background color layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(28.dp))
                .background(bgColor)
        )

        // Complete hint
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = "Complete",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            AnimatedVisibility(visible = completeReady) {
                Text("Release\nto complete", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Snooze hint
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_skip),
                contentDescription = "Snooze",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            AnimatedVisibility(visible = snoozeReady) {
                Text("Release\nto snooze", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Task card
        TaskCard(
            task = task,
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > completeThreshold -> { onSwipeRight(); offsetX = 0f }
                                offsetX < -snoozeThreshold  -> { onSwipeLeft();  offsetX = 0f }
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
    }
}







@Composable
fun TaskCardSwipeableOld(
    task: Task,
    onSwipeRight: () -> Unit,  // complete
    onSwipeLeft: () -> Unit,   // snooze
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe"
    )
    val snoozeThreshold = 220f
    val completeThreshold = 320f

    // Background color based on drag direction
    val bgColor by animateColorAsState(
        targetValue = when {
            offsetX > 60f  -> Color(0xFF4CAF50).copy(alpha = (offsetX).coerceIn(0f, 0.4f))
            offsetX < -60f -> Color(0xFFF44336).copy(alpha = (-offsetX).coerceIn(0f, 0.4f))
            else -> Color.Transparent
        },
        label = "swipe_bg"
    )


    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(28.dp))  // TODO change to M3 shape, not hardcoded
                .background(bgColor)
        )


        // Hint icons behind the card
        Icon(
            painter = painterResource(R.drawable.ic_check_circle),
            contentDescription = "Complete",
            tint = Color.White,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp).size(32.dp)
        )
        Icon(
            painter = painterResource(R.drawable.ic_skip),
            contentDescription = "Snooze",
            tint = Color.White,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp).size(32.dp)
        )

        TaskCard(
            task = task,
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > completeThreshold  -> { onSwipeRight(); offsetX = 0f }
                                offsetX < -snoozeThreshold -> { onSwipeLeft();  offsetX = 0f }
                                else -> offsetX = 0f // snap back
                            }
                        },
                        onDragCancel = { offsetX = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-400f, 400f)
                        }
                    )
                }
        )
    }
}
