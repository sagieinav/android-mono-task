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
    val offsetX = Animatable(0f)

    private var needsReset  by mutableStateOf(true)
    private var lastCardKey : Pair<String, Int>? = null

    // Pending horizontal offset for directional slide-in entry.
    // Set to ±screenWidthPx before a new card arrives; consumed and cleared in resetCard().
    private var pendingOffsetX: Float = 0f

    val displayAlpha  : Float get() = if (needsReset) 0f    else alpha.value
    val displayScale  : Float get() = if (needsReset) 0.22f else scale.value
    val displayBorder : Float get() = if (needsReset) 0f    else border.value
    val displayOffsetX: Float get() = if (needsReset) pendingOffsetX else offsetX.value

    fun checkIfNeedsReset(taskId: String, restoreVersion: Int) {
        val key = taskId to restoreVersion
        if (key != lastCardKey) {
            lastCardKey = key
            needsReset  = true
        }
    }

    /**
     * Sets the direction from which the next card should slide in.
     * The new card enters from the OPPOSITE side of where the old card exited.
     * Call this just before triggering the action that will cause a new card to appear.
     */
    fun setNextEntryDirection(exitDirection: SwipeExitDirection, screenWidthPx: Float) {
        pendingOffsetX = if (exitDirection == SwipeExitDirection.RIGHT) -screenWidthPx else screenWidthPx
    }

    /**
     * Cancels any pending directional entry. Call before undo operations so the
     * restored card uses the normal pop-in animation instead of a slide-in.
     */
    fun cancelPendingEntryDirection() {
        pendingOffsetX = 0f
    }

    /**
     * Snaps all Animatables to their initial values for the incoming card.
     * Returns true if this card should slide in horizontally (pendingOffsetX was set),
     * or false if it should use the normal pop-in animation.
     */
    suspend fun resetCard(): Boolean {
        val isSlideIn = pendingOffsetX != 0f
        if (isSlideIn) {
            alpha.snapTo(0.85f)  // nearly opaque — slide feels more physical than fading from nothing
            scale.snapTo(1f)     // no pop-scale during slide
        } else {
            alpha.snapTo(0f)
            scale.snapTo(0.22f)
        }
        border.snapTo(0f)
        offsetX.snapTo(pendingOffsetX)
        pendingOffsetX = 0f
        needsReset = false
        return isSlideIn
    }

    // ========== Snooze Actions ==========

    fun onSnoozeConfirmed(option: XpEvents.SnoozeOption, scope: CoroutineScope, screenWidthPx: Float) {
        // Set slide-in direction and fire action immediately so the ViewModel freeze
        // is in place before any Firestore snapshot can arrive and interrupt the exit animation.
        setNextEntryDirection(SwipeExitDirection.LEFT, screenWidthPx)
        onFocusEvent(FocusEvent.DismissSnooze)
        onFocusEvent(FocusEvent.ExecuteSnooze(option))

        // Visual-only: drive the card exit animation. Must clear the trigger before the
        // ViewModel's minimum visual delay (380ms) ends, so the incoming card doesn't
        // inherit a stale LEFT trigger. Timeline: set at 80ms, clear at 360ms (80+280).
        scope.launch {
            delay(80)            // brief pause for sheet dismiss animation
            snoozeExitTrigger = SwipeExitDirection.LEFT
            delay(280L)          // EXIT_ANIM_DURATION — exit animation is now complete
            snoozeExitTrigger = null
        }
    }
}

@Composable
fun rememberFocusAnimationState(onFocusEvent: (FocusEvent) -> Unit): FocusAnimationState {
    return remember(onFocusEvent) { FocusAnimationState(onFocusEvent) }
}
