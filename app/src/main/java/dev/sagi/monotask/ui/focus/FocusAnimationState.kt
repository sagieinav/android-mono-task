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

    var isSnoozeExiting by mutableStateOf(false)
        private set

    var snoozeExitTrigger by mutableStateOf<SwipeExitDirection?>(null)
        private set

    private var pendingSnoozeAction: (() -> Unit)? = null

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
            pendingSnoozeAction  = { onFocusEvent(FocusEvent.ExecuteSnooze(option)) }
            isSnoozeExiting      = true
            snoozeExitTrigger    = SwipeExitDirection.LEFT
        }
    }

    fun onSnoozeCardExited() {
        pendingSnoozeAction?.invoke()
        pendingSnoozeAction = null
        snoozeExitTrigger   = null
        isSnoozeExiting     = false
    }
}

@Composable
fun rememberFocusAnimationState(onFocusEvent: (FocusEvent) -> Unit): FocusAnimationState {
    return remember(onFocusEvent) { FocusAnimationState(onFocusEvent) }
}
