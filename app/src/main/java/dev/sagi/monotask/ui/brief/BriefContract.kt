package dev.sagi.monotask.ui.brief

import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User

sealed interface BriefUiState {
    object Loading : BriefUiState
    data class Ready(
        val overdueTasks: List<Task>,
        val dueTodayTasks: List<Task>,
        val pendingCount: Int,
        val workspaceNames: Map<String, String> = emptyMap(),
        val user: User? = null
    ) : BriefUiState
}
