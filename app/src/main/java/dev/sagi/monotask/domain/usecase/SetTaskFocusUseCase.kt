package dev.sagi.monotask.domain.usecase

import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.domain.repository.TaskRepository
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import dev.sagi.monotask.domain.service.XpEngine
import javax.inject.Inject

/**
 * Switches the workspace's focus task to [task].
 * If a different task is currently pinned, it is auto-snoozed with a MANUAL penalty first.
 */
class SetTaskFocusUseCase @Inject constructor(
    private val taskRepository      : TaskRepository,
    private val workspaceRepository : WorkspaceRepository,
) {
    suspend operator fun invoke(userId: String, task: Task, workspace: Workspace) {
        workspace.currentFocusTaskId?.let { currentId ->
            if (currentId != task.id) {
                val allTasks    = taskRepository.getActiveTasksOnce(userId, workspace.id)
                val currentTask = allTasks.find { it.id == currentId }
                currentTask?.let {
                    taskRepository.updateSnoozeFields(userId, it, XpEngine.SnoozeOption.MANUAL)
                }
            }
        }
        workspaceRepository.setFocusTask(userId, workspace.id, task.id)
    }
}
