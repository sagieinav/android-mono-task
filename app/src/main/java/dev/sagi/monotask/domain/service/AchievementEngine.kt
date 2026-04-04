package dev.sagi.monotask.domain.service

import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.AchievementCategory
import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.data.model.AchievementMilestone
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.UserStats
import java.time.Instant
import java.time.ZoneId

object AchievementEngine {

    fun evaluateFromStats(stats: UserStats, level: Int): List<Achievement> {
        val templates = evaluate(emptyList(), level)
        return templates.map { template ->
            val computedTier = when (template.category) {
                AchievementCategory.STREAKS -> tierFor(stats.longestStreak, bronze = 3, silver = 7, gold = 30)
                AchievementCategory.TASK_VOLUME -> tierFor(stats.totalTasksCompleted, bronze = 5, silver = 100, gold = 500)
                AchievementCategory.DISCIPLINE -> if (stats.totalTasksCompleted >= 20) {
                    val ratio = stats.aceCount.toFloat() / stats.totalTasksCompleted
                    when {
                        ratio >= 0.90f -> AchievementTier.GOLD
                        ratio >= 0.70f -> AchievementTier.SILVER
                        ratio >= 0.50f -> AchievementTier.BRONZE
                        else           -> null
                    }
                } else null
                AchievementCategory.XP_LEVELING -> tierFor(level, bronze = 5, silver = 15, gold = 30)
            }
            // earnedAchievements is the source of truth for previously-reached tiers.
            // Raw counter fields (e.g. longestStreak) can be stale for older users,
            // so we take the maximum of both to never downgrade a tier.
            val storedTier = stats.earnedAchievements[template.category.name]
                ?.let { runCatching { AchievementTier.valueOf(it) }.getOrNull() }
            template.copy(earnedTier = maxTier(computedTier, storedTier))
        }
    }

    private fun tierFor(value: Int, bronze: Int, silver: Int, gold: Int): AchievementTier? = when {
        value >= gold -> AchievementTier.GOLD
        value >= silver -> AchievementTier.SILVER
        value >= bronze -> AchievementTier.BRONZE
        else -> null
    }

    private fun maxTier(a: AchievementTier?, b: AchievementTier?): AchievementTier? {
        if (a == null) return b
        if (b == null) return a
        val order = listOf(AchievementTier.BRONZE, AchievementTier.SILVER, AchievementTier.GOLD)
        return if (order.indexOf(a) >= order.indexOf(b)) a else b
    }

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
            iconRes = R.drawable.ic_fire,
            earnedTier = tierFor(maxStreak(tasks), bronze = 3, silver = 7, gold = 30),
            milestones = listOf(
                AchievementMilestone(AchievementTier.BRONZE, "First Flame", "Active 3 days in a row"),
                AchievementMilestone(AchievementTier.SILVER, "Consistency King", "Active 7 days in a row"),
                AchievementMilestone(AchievementTier.GOLD,   "Unstoppable", "Active 30 days in a row")
            )
        )
    }

    // ========== Task Volume ==========

    private fun evaluateTaskVolume(tasks: List<Task>): Achievement {
        return Achievement(
            category = AchievementCategory.TASK_VOLUME,
            iconRes = R.drawable.ic_task_alt,
            earnedTier = tierFor(tasks.size, bronze = 5, silver = 100, gold = 500),
            milestones = listOf(
                AchievementMilestone(AchievementTier.BRONZE, "Warming Up", "Complete 5 tasks"),
                AchievementMilestone(AchievementTier.SILVER, "Century", "Complete 100 tasks"),
                AchievementMilestone(AchievementTier.GOLD,   "Task Machine", "Complete 500 tasks")
            )
        )
    }

    // ========== Discipline (ace ratio, minimum 20 tasks) ==========

    private fun evaluateDiscipline(tasks: List<Task>): Achievement {
        val earned = if (tasks.size >= 20) {
            val aceRatio = tasks.count { it.isAce }.toFloat() / tasks.size
            when {
                aceRatio >= 0.90f -> AchievementTier.GOLD
                aceRatio >= 0.70f -> AchievementTier.SILVER
                aceRatio >= 0.50f -> AchievementTier.BRONZE
                else -> null
            }
        } else null

        return Achievement(
            category = AchievementCategory.DISCIPLINE,
            iconRes = R.drawable.ic_bolt,
            earnedTier = earned,
            milestones = listOf(
                AchievementMilestone(AchievementTier.BRONZE, "No Excuses", "50%+ ace ratio"),
                AchievementMilestone(AchievementTier.SILVER, "Iron Will", "70%+ ace ratio"),
                AchievementMilestone(AchievementTier.GOLD,   "Denial Denier", "90%+ ace ratio")
            )
        )
    }

    // ========== XP & Leveling ==========

    private fun evaluateXpLeveling(level: Int): Achievement {
        return Achievement(
            category = AchievementCategory.XP_LEVELING,
            iconRes = R.drawable.ic_medal,
            earnedTier = tierFor(level, bronze = 5, silver = 15, gold = 30),
            milestones = listOf(
                AchievementMilestone(AchievementTier.BRONZE, "Rising Star", "Reach level 5"),
                AchievementMilestone(AchievementTier.SILVER, "Veteran", "Reach level 15"),
                AchievementMilestone(AchievementTier.GOLD,   "Legend", "Reach level 30")
            )
        )
    }

    // ========== Helpers ==========

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