package dev.sagi.monotask.domain.service

import dev.sagi.monotask.designsystem.theme.IconPack
import dev.sagi.monotask.data.model.AchievementCategory
import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.data.model.AchievementMilestone
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.UserStats
import java.time.Instant
import java.time.ZoneId

private object StreakRules {
    const val BRONZE = 3
    const val SILVER = 7
    const val GOLD = 30
}

private object VolumeRules {
    const val BRONZE = 5
    const val SILVER = 100
    const val GOLD = 300
}

private object DisciplineRules {
    const val MIN_TASKS = 20
    const val BRONZE_RATIO = 0.50f
    const val SILVER_RATIO = 0.70f
    const val GOLD_RATIO = 0.90f
}

private object LevelRules {
    const val BRONZE = 5
    const val SILVER = 15
    const val GOLD = 30
}

object AchievementEngine {

    fun evaluateFromStats(stats: UserStats, level: Int): List<Achievement> = listOf(
        evaluateStreaks(emptyList()).copy(
            earnedTier = tierFor(
                value = stats.longestStreak,
                bronze = StreakRules.BRONZE,
                silver = StreakRules.SILVER,
                gold = StreakRules.GOLD
            )
        ),

        evaluateTaskVolume(emptyList()).copy(
            earnedTier = tierFor(
                value = stats.totalTasksCompleted,
                bronze = VolumeRules.BRONZE,
                silver = VolumeRules.SILVER,
                gold = VolumeRules.GOLD
            )
        ),

        evaluateDiscipline(emptyList()).copy(
            earnedTier = if (stats.totalTasksCompleted >= DisciplineRules.MIN_TASKS) {
                val ratio = stats.aceCount.toFloat() / stats.totalTasksCompleted
                tierForRatio(
                    value = ratio,
                    bronze = DisciplineRules.BRONZE_RATIO,
                    silver = DisciplineRules.SILVER_RATIO,
                    gold = DisciplineRules.GOLD_RATIO
                )
            } else null
        ),

        evaluateXpLeveling(level)
    )

    fun evaluate(completedTasks: List<Task>, level: Int): List<Achievement> = listOf(
        evaluateStreaks(completedTasks),
        evaluateTaskVolume(completedTasks),
        evaluateDiscipline(completedTasks),
        evaluateXpLeveling(level)
    )


    // ========== Streaks ==========
    private fun evaluateStreaks(tasks: List<Task>): Achievement {
        return Achievement(
            category = AchievementCategory.STREAKS,
            iconRes = IconPack.Fire,
            earnedTier = tierFor(
                value = maxStreak(tasks),
                bronze = StreakRules.BRONZE,
                silver = StreakRules.SILVER,
                gold = StreakRules.GOLD
            ),
            milestones = listOf(
                AchievementMilestone(AchievementTier.BRONZE, "First Flame", "Active ${StreakRules.BRONZE} days in a row"),
                AchievementMilestone(AchievementTier.SILVER, "Consistency King", "Active ${StreakRules.SILVER} days in a row"),
                AchievementMilestone(AchievementTier.GOLD, "Unstoppable", "Active ${StreakRules.GOLD} days in a row")
            )
        )
    }


    // ========== Task Volume ==========
    private fun evaluateTaskVolume(tasks: List<Task>): Achievement {
        return Achievement(
            category = AchievementCategory.TASK_VOLUME,
            iconRes = IconPack.TaskAlt,
            earnedTier = tierFor(
                value = tasks.size,
                bronze = VolumeRules.BRONZE,
                silver = VolumeRules.SILVER,
                gold = VolumeRules.GOLD
            ),
            milestones = listOf(
                AchievementMilestone(AchievementTier.BRONZE, "Warming Up", "Complete ${VolumeRules.BRONZE} tasks"),
                AchievementMilestone(AchievementTier.SILVER, "Century", "Complete ${VolumeRules.SILVER} tasks"),
                AchievementMilestone(AchievementTier.GOLD, "Task Machine", "Complete ${VolumeRules.GOLD} tasks")
            )
        )
    }


    // ========== Discipline (ace ratio) ==========
    private fun evaluateDiscipline(tasks: List<Task>): Achievement {
        val earned = if (tasks.size >= DisciplineRules.MIN_TASKS) {
            val aceRatio = tasks.count { it.isAce }.toFloat() / tasks.size
            tierForRatio(
                value = aceRatio,
                bronze = DisciplineRules.BRONZE_RATIO,
                silver = DisciplineRules.SILVER_RATIO,
                gold = DisciplineRules.GOLD_RATIO
            )
        } else null

        // Convert the float ratios to clean integer percentages for the UI strings
        val bronzePercent = (DisciplineRules.BRONZE_RATIO * 100).toInt()
        val silverPercent = (DisciplineRules.SILVER_RATIO * 100).toInt()
        val goldPercent = (DisciplineRules.GOLD_RATIO * 100).toInt()

        return Achievement(
            category = AchievementCategory.DISCIPLINE,
            iconRes = IconPack.Bolt,
            earnedTier = earned,
            milestones = listOf(
                AchievementMilestone(AchievementTier.BRONZE, "No Excuses", "$bronzePercent%+ ace ratio"),
                AchievementMilestone(AchievementTier.SILVER, "Iron Will", "$silverPercent%+ ace ratio"),
                AchievementMilestone(AchievementTier.GOLD, "Denial Denier", "$goldPercent%+ ace ratio")
            )
        )
    }


    // ========== XP & Leveling ==========
    private fun evaluateXpLeveling(level: Int): Achievement {
        return Achievement(
            category = AchievementCategory.XP_LEVELING,
            iconRes = IconPack.Medal,
            earnedTier = tierFor(
                value = level,
                bronze = LevelRules.BRONZE,
                silver = LevelRules.SILVER,
                gold = LevelRules.GOLD
            ),
            milestones = listOf(
                AchievementMilestone(AchievementTier.BRONZE, "Rising Star", "Reach level ${LevelRules.BRONZE}"),
                AchievementMilestone(AchievementTier.SILVER, "Veteran", "Reach level ${LevelRules.SILVER}"),
                AchievementMilestone(AchievementTier.GOLD, "Legend", "Reach level ${LevelRules.GOLD}")
            )
        )
    }


    // ========== Helpers ==========
    private fun tierFor(value: Int, bronze: Int, silver: Int, gold: Int): AchievementTier? = when {
        value >= gold -> AchievementTier.GOLD
        value >= silver -> AchievementTier.SILVER
        value >= bronze -> AchievementTier.BRONZE
        else -> null
    }

    private fun tierForRatio(value: Float, bronze: Float, silver: Float, gold: Float): AchievementTier? = when {
        value >= gold -> AchievementTier.GOLD
        value >= silver -> AchievementTier.SILVER
        value >= bronze -> AchievementTier.BRONZE
        else -> null
    }

    fun computeLongestStreak(tasks: List<Task>): Int = maxStreak(tasks)


    // Longest consecutive active-day streak across all-time completed tasks
    private fun maxStreak(tasks: List<Task>): Int {
        val days = tasks
            .mapNotNull { it.completedAt }
            .map { ts ->
                Instant.ofEpochMilli(ts.toDate().time)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toEpochDay()
            }
            .toSortedSet()
            .toList()

        if (days.isEmpty()) return 0
        var maxRun = 1
        var run = 1
        for (i in 1 until days.size) {
            run = if (days[i] - days[i - 1] == 1L) run + 1 else 1
            if (run > maxRun) maxRun = run
        }
        return maxRun
    }
}