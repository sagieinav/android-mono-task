package dev.sagi.monotask.domain.usecase

import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.repository.TaskRepository
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import dev.sagi.monotask.domain.service.TaskSelector
import dev.sagi.monotask.domain.service.XpEngine
import javax.inject.Inject

/**
 * Applies a snooze to the current focus task:
 * updates snooze fields, selects the next task via [TaskSelector], and re-pins it.
 */
class SnoozeTaskUseCase @Inject constructor(
    private val taskRepository      : TaskRepository,
    private val workspaceRepository : WorkspaceRepository,
) {
    suspend operator fun invoke(
        userId : String,
        task : Task,
        workspaceId : String,
        option : XpEngine.SnoozeOption,
        dueDateWeight : Float
    ) {
        taskRepository.updateSnoozeFields(userId, task, option)

        val allTasks = taskRepository.getActiveTasksOnce(userId, workspaceId)
        val nextTask = when (option) {
            XpEngine.SnoozeOption.BY_DUE_DATE -> TaskSelector.getTopTaskByDueDate(
                allTasks, dueDateWeight, excludeId = task.id
            )
            else -> TaskSelector.getTopTask(
                allTasks, dueDateWeight, excludeId = task.id
            )
        }
        workspaceRepository.setFocusTask(userId, workspaceId, nextTask?.id)
    }
}
