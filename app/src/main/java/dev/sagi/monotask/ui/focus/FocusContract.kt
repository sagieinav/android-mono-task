package dev.sagi.monotask.ui.focus

import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.domain.util.XpEvents

// ========== UI States ==========
sealed class FocusUiState {
    object Loading : FocusUiState()
    object Empty : FocusUiState()
    data class Active(
        val focusTask: Task,
        val queue: List<Task>,
        val workspace: Workspace,
        val showSnoozeSheet: Boolean = false,
        val restoreVersion: Int = 0
    ) : FocusUiState()
}

// ========== Event Callbacks ==========
sealed interface FocusEvent {
    object CompleteTask : FocusEvent
    object OpenSnooze : FocusEvent
    object DismissSnooze : FocusEvent
    data class ExecuteSnooze(val option: XpEvents.SnoozeOption) : FocusEvent
    object UndoCompleteTask : FocusEvent
    object UndoSnoozeTask : FocusEvent
}

// ========== One-Shot UI Effects ==========
sealed interface FocusUiEffect {
    data class ShowError(val message: String) : FocusUiEffect
    data class ShowUndoComplete(val message: String) : FocusUiEffect
    data class ShowUndoSnooze(val message: String) : FocusUiEffect
    // Emitted once per newly earned tier after task completion
    data class ShowAchievementUnlocked(
        val name : String,
        val tier : AchievementTier
    ) : FocusUiEffect
    data class ShowLevelUp(val previousLevel: Int, val newLevel: Int) : FocusUiEffect
}