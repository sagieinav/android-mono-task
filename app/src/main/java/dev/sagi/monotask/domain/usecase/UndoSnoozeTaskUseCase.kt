package dev.sagi.monotask.domain.usecase

import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.repository.TaskRepository
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import javax.inject.Inject

/**
 * Atomically reverses a snooze: restores the task's snooze fields and re-pins it as the focus task.
 */
class UndoSnoozeTaskUseCase @Inject constructor(
    private val taskRepository : TaskRepository,
    private val workspaceRepository : WorkspaceRepository,
) {
    suspend operator fun invoke(userId: String, task: Task) {
        taskRepository.undoSnoozeFields(userId, task)
        workspaceRepository.setFocusTask(userId, task.workspaceId, task.id)
    }
}
