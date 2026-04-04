package dev.sagi.monotask.domain.usecase

import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.repository.ActivityRepository
import dev.sagi.monotask.domain.repository.StatsRepository
import dev.sagi.monotask.domain.repository.TaskRepository
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import dev.sagi.monotask.domain.service.AchievementEngine
import dev.sagi.monotask.domain.service.XpEngine
import javax.inject.Inject

data class CompleteTaskResult(
    val xpAwarded: Int,
    val previousLevel: Int,
    val newLevel: Int,
    val newlyUnlocked: List<Achievement>
)

/**
 * Orchestrates all data-layer operations for task completion:
 * marks the task done, awards XP, logs activity, and evaluates achievements.
 * Returns a [CompleteTaskResult] so the ViewModel can emit UI effects (level-up, achievement unlocked).
 */
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository      : TaskRepository,
    private val statsRepository     : StatsRepository,
    private val activityRepository  : ActivityRepository,
    private val workspaceRepository : WorkspaceRepository,
) {
    suspend operator fun invoke(
        userId : String,
        task : Task,
        workspaceId : String,
        user : User
    ): CompleteTaskResult {
        val xpGained = task.currentXp

        // Snapshot BEFORE completion so we can diff which achievements are newly earned
        val tasksBefore = taskRepository.getAllCompletedTasksOnce(userId)
        val achievementsBefore = AchievementEngine.evaluate(tasksBefore, user.level)

        taskRepository.markTaskCompleted(userId, task.id)
        workspaceRepository.setFocusTask(userId, workspaceId, null)
        statsRepository.addXp(userId, xpGained, user.xp, user.level)
        activityRepository.logDailyActivity(userId, xpGained, tasksCompleted = 1)

        val levelAfter = XpEngine.levelForXp(user.xp + xpGained)
        // Append in-memory to avoid a second Firestore fetch
        val tasksAfter = tasksBefore + task
        val achievementsAfter = AchievementEngine.evaluate(tasksAfter, levelAfter)

        val newlyUnlocked = achievementsAfter.filter { newProgress ->
            val oldProgress = achievementsBefore.find { it.category == newProgress.category }
            newProgress.earnedTier != null && newProgress.earnedTier != oldProgress?.earnedTier
        }

        // Non-critical: failure does not affect task completion
        try {
            statsRepository.updateUserStats(userId, xpGained, task.isAce, newlyUnlocked)
        }
        catch (_: Exception) {}

        return CompleteTaskResult(
            xpAwarded = xpGained,
            previousLevel = user.level,
            newLevel = levelAfter,
            newlyUnlocked = newlyUnlocked
        )
    }
}
