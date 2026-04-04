package dev.sagi.monotask.domain.usecase

import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.repository.ActivityRepository
import dev.sagi.monotask.domain.repository.StatsRepository
import dev.sagi.monotask.domain.repository.TaskRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Restores a completed task back to the active queue:
 * removes its XP contribution, removes its activity log entry (on the original completion date),
 * and rolls back denormalized user stats.
 */
class RestoreCompletedTaskUseCase @Inject constructor(
    private val taskRepository     : TaskRepository,
    private val activityRepository : ActivityRepository,
    private val statsRepository    : StatsRepository,
) {
    suspend operator fun invoke(userId: String, task: Task) {
        val xpToRemove = task.currentXp
        val completionEpoch = task.completedAt
            ?.toDate()
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()
            ?.toEpochDay()
            ?: LocalDate.now().toEpochDay()

        taskRepository.restoreTask(userId, task.id)
        activityRepository.removeDailyActivity(userId, xpToRemove, dateEpochDay = completionEpoch)
        statsRepository.removeXp(userId, xpToRemove)
        statsRepository.undoUserStats(userId, task.isAce)
    }
}
