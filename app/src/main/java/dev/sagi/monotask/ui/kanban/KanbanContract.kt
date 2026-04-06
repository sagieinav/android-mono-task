package dev.sagi.monotask.ui.kanban

import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.designsystem.theme.IconPack

// ========== Sort Order ==========

enum class SortOrder(val label: String, val directionIcon: Int) {
    DUE_ASC("Due date",  IconPack.ArrowNarrowUp),
    DUE_DESC("Due date", IconPack.ArrowNarrowDown),
    CREATED_ASC("Created",  IconPack.ArrowNarrowUp),
    CREATED_DESC("Created", IconPack.ArrowNarrowDown)
}

// ========== UI States ==========

sealed class KanbanUiState {
    object Loading : KanbanUiState()
    object Locked  : KanbanUiState()
    data class Ready(
        val highTasks: List<Task>,
        val mediumTasks: List<Task>,
        val lowTasks: List<Task>,
        val isArchive: Boolean,
        val sortOrder: SortOrder = SortOrder.CREATED_DESC
    ) : KanbanUiState()
}

// ========== Event Callbacks ==========

sealed interface KanbanEvent {
    object ToggleArchive : KanbanEvent
    object ResetArchive  : KanbanEvent
    data class OpenEditSheet(val task: Task? = null) : KanbanEvent
    object DismissEditSheet : KanbanEvent
    data class UpdateTask(val task: Task) : KanbanEvent
    data class DeleteTask(val taskId: String) : KanbanEvent
    data class FocusNow(val task: Task) : KanbanEvent
    data class RestoreTask(val task: Task) : KanbanEvent
}

// ========== One-Shot UI Effects ==========

sealed interface KanbanUiEffect {
    data class ShowError(val message: String) : KanbanUiEffect
    object NavigateToFocus : KanbanUiEffect
}
