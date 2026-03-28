package dev.sagi.monotask.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.sagi.monotask.domain.util.XpEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ========== Animation State ==========

@Stable
class FocusAnimationState(
    private val onFocusEvent: (FocusEvent) -> Unit
) {
    // ========== Snooze Exit State ==========

    var snoozeExitTrigger by mutableStateOf<SwipeExitDirection?>(null)
        private set

    // ========== Entry Animation ==========

    val alpha  = Animatable(0f)
    val scale  = Animatable(0.22f)
    val border = Animatable(0f)

    private var needsReset  by mutableStateOf(true)
    private var lastCardKey : Pair<String, Int>? = null

    val displayAlpha  : Float get() = if (needsReset) 0f    else alpha.value
    val displayScale  : Float get() = if (needsReset) 0.22f else scale.value
    val displayBorder : Float get() = if (needsReset) 0f    else border.value

    fun checkIfNeedsReset(taskId: String, restoreVersion: Int) {
        val key = taskId to restoreVersion
        if (key != lastCardKey) {
            lastCardKey = key
            needsReset  = true
        }
    }

    suspend fun resetCard() {
        alpha.snapTo(0f)
        scale.snapTo(0.22f)
        border.snapTo(0f)
        needsReset = false
    }

    // ========== Snooze Actions ==========

    fun onSnoozeConfirmed(option: XpEvents.SnoozeOption, scope: CoroutineScope) {
        scope.launch {
            onFocusEvent(FocusEvent.DismissSnooze)
            delay(100)
            snoozeExitTrigger = SwipeExitDirection.LEFT
            delay(300L) // EXIT_ANIM_DURATION (280ms) + small buffer
            snoozeExitTrigger = null
            onFocusEvent(FocusEvent.ExecuteSnooze(option))
        }
    }
}

@Composable
fun rememberFocusAnimationState(onFocusEvent: (FocusEvent) -> Unit): FocusAnimationState {
    return remember(onFocusEvent) { FocusAnimationState(onFocusEvent) }
}
