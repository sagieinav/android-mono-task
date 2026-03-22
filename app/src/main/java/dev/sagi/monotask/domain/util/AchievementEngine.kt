package dev.sagi.monotask.domain.util

import androidx.compose.ui.res.painterResource
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.AchievementCategory
import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.data.model.AchievementMilestone
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.UserStats

object AchievementEngine {

    fun evaluateFromStats(stats: UserStats, level: Int): List<Achievement> {
        val templates = evaluate(emptyList(), level)
        return templates.map { template ->
            val computedTier = when (template.category) {
                AchievementCategory.STREAKS     -> when {
                    stats.longestStreak >= 30 -> AchievementTier.GOLD
                    stats.longestStreak >= 7  -> AchievementTier.SILVER
                    stats.longestStreak >= 3  -> AchievementTier.BRONZE
                    else                      -> null
                }
                AchievementCategory.TASK_VOLUME -> when {
                    stats.totalTasksCompleted >= 500 -> AchievementTier.GOLD
                    stats.totalTasksCompleted >= 100 -> AchievementTier.SILVER
                    stats.totalTasksCompleted >= 5   -> AchievementTier.BRONZE
                    else                             -> null
                }
                AchievementCategory.DISCIPLINE  -> if (stats.totalTasksCompleted >= 20) {
                    val ratio = stats.aceCount.toFloat() / stats.totalTasksCompleted
                    when {
                        ratio >= 0.90f -> AchievementTier.GOLD
                        ratio >= 0.70f -> AchievementTier.SILVER
                        ratio >= 0.50f -> AchievementTier.BRONZE
                        else           -> null
                    }
                } else null
                AchievementCategory.XP_LEVELING -> when {
                    level >= 30 -> AchievementTier.GOLD
                    level >= 15 -> AchievementTier.SILVER
                    level >= 5  -> AchievementTier.BRONZE
                    else        -> null
                }
            }
            // earnedAchievements is the source of truth for previously-reached tiers.
            // Raw counter fields (e.g. longestStreak) can be stale for older users,
            // so we take the maximum of both to never downgrade a tier.
            val storedTier = stats.earnedAchievements[template.category.name]
                ?.let { runCatching { AchievementTier.valueOf(it) }.getOrNull() }
            template.copy(earnedTier = maxTier(computedTier, storedTier))
        }
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
        val streak = maxStreak(tasks)
        val earned = when {
            streak >= 30 -> AchievementTier.GOLD
            streak >= 7  -> AchievementTier.SILVER
            streak >= 3  -> AchievementTier.BRONZE
            else         -> null
        }
        return Achievement(
            category   = AchievementCategory.STREAKS,
            iconRes       = R.drawable.ic_fire,
            earnedTier = earned,
            bronze     = AchievementMilestone(AchievementTier.BRONZE, "First Flame",      "Active 3 days in a row"),
            silver     = AchievementMilestone(AchievementTier.SILVER, "Consistency King", "Active 7 days in a row"),
            gold       = AchievementMilestone(AchievementTier.GOLD,   "Unstoppable",      "Active 30 days in a row")
        )
    }

    // ========== Task Volume ==========

    private fun evaluateTaskVolume(tasks: List<Task>): Achievement {
        val count  = tasks.size
        val earned = when {
            count >= 500 -> AchievementTier.GOLD
            count >= 100 -> AchievementTier.SILVER
            count >= 5   -> AchievementTier.BRONZE
            else         -> null
        }
        return Achievement(
            category   = AchievementCategory.TASK_VOLUME,
            iconRes    = R.drawable.ic_task_alt,
            earnedTier = earned,
            bronze     = AchievementMilestone(AchievementTier.BRONZE, "Warming Up",   "Complete 5 tasks"),
            silver     = AchievementMilestone(AchievementTier.SILVER, "Century",      "Complete 100 tasks"),
            gold       = AchievementMilestone(AchievementTier.GOLD,   "Task Machine", "Complete 500 tasks")
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
                else              -> null
            }
        } else null

        return Achievement(
            category   = AchievementCategory.DISCIPLINE,
            iconRes       = R.drawable.ic_bolt,
            earnedTier = earned,
            bronze     = AchievementMilestone(AchievementTier.BRONZE, "No Excuses",    "50%+ ace ratio"),
            silver     = AchievementMilestone(AchievementTier.SILVER, "Iron Will",     "70%+ ace ratio"),
            gold       = AchievementMilestone(AchievementTier.GOLD,   "Denial Denier", "90%+ ace ratio")
        )
    }

    // ========== XP & Leveling ==========

    private fun evaluateXpLeveling(level: Int): Achievement {
        val earned = when {
            level >= 30 -> AchievementTier.GOLD
            level >= 15 -> AchievementTier.SILVER
            level >= 5  -> AchievementTier.BRONZE
            else        -> null
        }
        return Achievement(
            category   = AchievementCategory.XP_LEVELING,
            iconRes       = R.drawable.ic_medal,
            earnedTier = earned,
            bronze     = AchievementMilestone(AchievementTier.BRONZE, "Rising Star", "Reach level 5"),
            silver     = AchievementMilestone(AchievementTier.SILVER, "Veteran",     "Reach level 15"),
            gold       = AchievementMilestone(AchievementTier.GOLD,   "Legend",      "Reach level 30")
        )
    }

    // ========== Helpers ==========

    fun computeLongestStreak(tasks: List<Task>): Int = maxStreak(tasks)

    // Longest consecutive active-day streak across all-time completed tasks
    private fun maxStreak(tasks: List<Task>): Int {
        val days = tasks
            .mapNotNull { it.completedAt }
            .map { ts ->
                java.time.Instant.ofEpochMilli(ts.toDate().time)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .toEpochDay()
            }
            .toSortedSet()
            .toList()

        if (days.isEmpty()) return 0
        var maxRun = 1
        var run    = 1
        for (i in 1 until days.size) {
            run = if (days[i] - days[i - 1] == 1L) run + 1 else 1
            if (run > maxRun) maxRun = run
        }
        return maxRun
    }
}