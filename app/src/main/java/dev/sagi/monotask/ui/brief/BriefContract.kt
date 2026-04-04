package dev.sagi.monotask.ui.brief

import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User

sealed interface BriefUiState {
    object Loading : BriefUiState
    data class Ready(
        val overdueTasks: List<Task>,
        val dueTodayTasks: List<Task>,
        val pendingCount: Int,
        val briefStatus: BriefStatus,
        val workspaceNames: Map<String, String> = emptyMap(),
        val user: User? = null
    ) : BriefUiState
}

// ========== Event Callbacks ==========

sealed interface BriefEvent {
    object Refresh : BriefEvent
}

// ========== One-Shot UI Effects ==========

sealed interface BriefUiEffect {
    data class ShowError(val message: String) : BriefUiEffect
}
