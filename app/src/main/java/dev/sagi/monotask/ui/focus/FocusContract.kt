package dev.sagi.monotask.ui.focus

import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.domain.service.XpEngine

// ========== UI States ==========
sealed class FocusUiState {
    data object Loading : FocusUiState()
    data object Empty   : FocusUiState()
    data class Active(
        val focusTask: Task,
        val queue: List<Task>,
        val workspace: Workspace,
        val restoreVersion: Int = 0
    ) : FocusUiState()
}

// ========== Event Callbacks ==========
sealed interface FocusEvent {
    data object CompleteTask : FocusEvent
    data object OpenSnooze : FocusEvent
    data object DismissSnooze : FocusEvent
    data class ExecuteSnooze(val option: XpEngine.SnoozeOption) : FocusEvent
    data object UndoCompleteTask : FocusEvent
    data object UndoSnoozeTask : FocusEvent
    data object OpenEditSheet : FocusEvent
    data object DismissEditSheet : FocusEvent
    data class UpdateTask(val task: Task) : FocusEvent
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