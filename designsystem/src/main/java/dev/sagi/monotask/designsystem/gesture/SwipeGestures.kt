package dev.sagi.monotask.designsystem.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.abs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// ========== Swipe Direction ==========

enum class SwipeExitDirection { RIGHT, LEFT }


// ========== Swipe State ==========

@Stable
class SwipeState {
    var offsetX by mutableFloatStateOf(0f)
    var exitDirection by mutableStateOf<SwipeExitDirection?>(null)
    var isLongPressPending by mutableStateOf(false)

    val isExiting: Boolean get() = exitDirection != null
    val isSwiping: Boolean get() = offsetX != 0f || isExiting
}


// ========== Swipe Gestures Modifier ==========

/**
 * Adds horizontal swipe and long-press gesture handling to any composable.
 *
 * Fires [onSwipeRight] / [onSwipeLeft] when the drag crosses [swipeThreshold].
 * Sets haptic feedback exactly once at threshold crossing in either direction.
 * Cancels long-press if a horizontal drag starts before [longPressTimeoutMillis] elapses.
 *
 * @param state Shared [SwipeState] that tracks offset and exit direction.
 * @param haptic Haptic feedback handle (from [LocalHapticFeedback]).
 * @param swipeThreshold Drag distance (px) required to trigger an action.
 * @param maxDragDistance Hard limit on how far the element can be dragged (px).
 */
fun Modifier.swipeGestures(
    state: SwipeState,
    haptic: HapticFeedback,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onLongPress: () -> Unit = {},
    swipeThreshold: Float = 380f,
    maxDragDistance: Float = 400f,
): Modifier = this.pointerInput(state.isExiting) {
    if (state.isExiting) return@pointerInput
    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
    val touchSlop = viewConfiguration.touchSlop

    coroutineScope {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var isDragging = false
            var accumulatedDrag = 0f
            state.isLongPressPending = true

            val longPressJob = launch {
                delay(longPressTimeout)
                state.isLongPressPending = false
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongPress()
            }

            try {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                    if (!change.pressed) {
                        longPressJob.cancel()
                        state.isLongPressPending = false
                        change.consume()
                        if (isDragging) {
                            when {
                                state.offsetX > swipeThreshold -> {
                                    state.exitDirection = SwipeExitDirection.RIGHT
                                    onSwipeRight() // fire immediately so freeze is set before any snapshot
                                }
                                state.offsetX < -swipeThreshold -> {
                                    state.offsetX = 0f
                                    onSwipeLeft()
                                }
                                else -> state.offsetX = 0f
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
                                state.isLongPressPending = false
                            }
                        }

                        if (isDragging) {
                            change.consume()
                            val newOffset = (state.offsetX + dx).coerceIn(-maxDragDistance, maxDragDistance)
                            // Fire haptic exactly once when the threshold is first crossed in either direction
                            if (abs(newOffset) >= swipeThreshold && abs(state.offsetX) < swipeThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            state.offsetX = newOffset
                        }
                    }
                }
            } catch (_: CancellationException) {
                longPressJob.cancel()
                state.isLongPressPending = false
                if (isDragging) state.offsetX = 0f
            }
        }
    }
}
