package dev.sagi.monotask.domain.usecase

import dev.sagi.monotask.domain.repository.ActivityRepository
import dev.sagi.monotask.domain.repository.StatsRepository
import dev.sagi.monotask.domain.repository.TaskRepository
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import javax.inject.Inject

/**
 * Atomically reverses a task completion:
 * re-pins the task, restores it, removes the awarded XP, removes the activity log entry,
 * and rolls back the denormalized user stats.
 */
class UndoCompleteTaskUseCase @Inject constructor(
    private val taskRepository      : TaskRepository,
    private val statsRepository     : StatsRepository,
    private val activityRepository  : ActivityRepository,
    private val workspaceRepository : WorkspaceRepository,
) {
    suspend operator fun invoke(
        userId : String,
        taskId : String,
        workspaceId : String,
        xpToRemove : Int,
        wasAce : Boolean
    ) {
        // Re-pin before restoring: ensures the first post-undo Firestore snapshot
        // shows the restored task rather than the next one in queue.
        workspaceRepository.setFocusTask(userId, workspaceId, taskId)
        taskRepository.restoreTask(userId, taskId)
        statsRepository.removeXp(userId, xpToRemove)
        activityRepository.removeDailyActivity(userId, xpToRemove)
        statsRepository.undoUserStats(userId, wasAce)
    }
}
