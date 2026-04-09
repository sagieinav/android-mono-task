package dev.sagi.monotask.domain.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.AchievementCategory
import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.UserStats
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class AchievementEngineTest {

    private val emptyStats = UserStats()

    // ========== evaluateFromStats ==========

    @Nested inner class EvaluateFromStats {

        @Test fun `below all thresholds returns no earned tiers`() {
            val results = AchievementEngine.evaluateFromStats(emptyStats, level = 1)
            assertThat(results.all { it.earnedTier == null }).isTrue()
        }

        @Test fun `longestStreak 3 earns STREAKS BRONZE`() {
            val stats = emptyStats.copy(longestStreak = 3)
            val streaks = AchievementEngine.evaluateFromStats(stats, level = 1)
                .first { it.category == AchievementCategory.STREAKS }
            assertThat(streaks.earnedTier).isEqualTo(AchievementTier.BRONZE)
        }

        @Test fun `longestStreak 7 earns STREAKS SILVER`() {
            val stats = emptyStats.copy(longestStreak = 7)
            val streaks = AchievementEngine.evaluateFromStats(stats, level = 1)
                .first { it.category == AchievementCategory.STREAKS }
            assertThat(streaks.earnedTier).isEqualTo(AchievementTier.SILVER)
        }

        @Test fun `longestStreak 30 earns STREAKS GOLD`() {
            val stats = emptyStats.copy(longestStreak = 30)
            val streaks = AchievementEngine.evaluateFromStats(stats, level = 1)
                .first { it.category == AchievementCategory.STREAKS }
            assertThat(streaks.earnedTier).isEqualTo(AchievementTier.GOLD)
        }

        @Test fun `totalTasksCompleted 5 earns TASK_VOLUME BRONZE`() {
            val stats = emptyStats.copy(totalTasksCompleted = 5)
            val vol = AchievementEngine.evaluateFromStats(stats, level = 1)
                .first { it.category == AchievementCategory.TASK_VOLUME }
            assertThat(vol.earnedTier).isEqualTo(AchievementTier.BRONZE)
        }

        @Test fun `discipline requires minimum 20 tasks`() {
            val stats = emptyStats.copy(totalTasksCompleted = 19, aceCount = 19)
            val disc = AchievementEngine.evaluateFromStats(stats, level = 1)
                .first { it.category == AchievementCategory.DISCIPLINE }
            assertThat(disc.earnedTier).isNull()
        }

        @Test fun `90 percent ace ratio with 20 tasks earns DISCIPLINE GOLD`() {
            val stats = emptyStats.copy(totalTasksCompleted = 20, aceCount = 18) // 90%
            val disc = AchievementEngine.evaluateFromStats(stats, level = 1)
                .first { it.category == AchievementCategory.DISCIPLINE }
            assertThat(disc.earnedTier).isEqualTo(AchievementTier.GOLD)
        }

        @Test fun `50 percent ace ratio earns DISCIPLINE BRONZE`() {
            val stats = emptyStats.copy(totalTasksCompleted = 20, aceCount = 10) // 50%
            val disc = AchievementEngine.evaluateFromStats(stats, level = 1)
                .first { it.category == AchievementCategory.DISCIPLINE }
            assertThat(disc.earnedTier).isEqualTo(AchievementTier.BRONZE)
        }

        @Test fun `level 5 earns XP_LEVELING BRONZE`() {
            val xpLevel = AchievementEngine.evaluateFromStats(emptyStats, level = 5)
                .first { it.category == AchievementCategory.XP_LEVELING }
            assertThat(xpLevel.earnedTier).isEqualTo(AchievementTier.BRONZE)
        }

        @Test fun `level 30 earns XP_LEVELING GOLD`() {
            val xpLevel = AchievementEngine.evaluateFromStats(emptyStats, level = 30)
                .first { it.category == AchievementCategory.XP_LEVELING }
            assertThat(xpLevel.earnedTier).isEqualTo(AchievementTier.GOLD)
        }

        @Test fun `tier is computed purely from stats with no lock-in`() {
            // longestStreak=0 must produce null regardless of any historical context
            val stats = emptyStats.copy(longestStreak = 0)
            val streaks = AchievementEngine.evaluateFromStats(stats, level = 1)
                .first { it.category == AchievementCategory.STREAKS }
            assertThat(streaks.earnedTier).isNull()
        }
    }

    // ========== computeLongestStreak ==========

    @Nested inner class ComputeLongestStreak {

        @Test fun `empty list returns 0`() {
            assertThat(AchievementEngine.computeLongestStreak(emptyList())).isEqualTo(0)
        }

        @Test fun `single completed task returns 1`() {
            assertThat(AchievementEngine.computeLongestStreak(listOf(taskCompletedAt(today())))).isEqualTo(1)
        }

        @Test fun `3 consecutive days returns 3`() {
            val tasks = listOf(
                taskCompletedAt(today()),
                taskCompletedAt(today().minusDays(1)),
                taskCompletedAt(today().minusDays(2))
            )
            assertThat(AchievementEngine.computeLongestStreak(tasks)).isEqualTo(3)
        }

        @Test fun `gap in days resets streak`() {
            val tasks = listOf(
                taskCompletedAt(today()),
                taskCompletedAt(today().minusDays(2)) // day 1 missing
            )
            assertThat(AchievementEngine.computeLongestStreak(tasks)).isEqualTo(1)
        }

        @Test fun `multiple tasks on same day count as one active day`() {
            val tasks = listOf(taskCompletedAt(today()), taskCompletedAt(today()))
            assertThat(AchievementEngine.computeLongestStreak(tasks)).isEqualTo(1)
        }

        @Test fun `longest run is returned when multiple streaks exist`() {
            val tasks = listOf(
                taskCompletedAt(today()),
                taskCompletedAt(today().minusDays(1)),
                taskCompletedAt(today().minusDays(2)),
                // gap
                taskCompletedAt(today().minusDays(10)),
                taskCompletedAt(today().minusDays(11))
            )
            assertThat(AchievementEngine.computeLongestStreak(tasks)).isEqualTo(3)
        }
    }

    // ========== evaluate (list-based) ==========

    @Nested inner class Evaluate {

        @Test fun `empty task list returns 4 achievements all with null tier`() {
            val results = AchievementEngine.evaluate(emptyList(), level = 1)
            assertThat(results.size).isEqualTo(4)
            assertThat(results.all { it.earnedTier == null }).isTrue()
        }

        @Test fun `5 completed tasks earns TASK_VOLUME BRONZE`() {
            val tasks = (1..5).map { Task(id = "t$it") }
            val vol = AchievementEngine.evaluate(tasks, level = 1)
                .first { it.category == AchievementCategory.TASK_VOLUME }
            assertThat(vol.earnedTier).isEqualTo(AchievementTier.BRONZE)
        }

        @Test fun `4 completed tasks does not earn TASK_VOLUME`() {
            val tasks = (1..4).map { Task(id = "t$it") }
            val vol = AchievementEngine.evaluate(tasks, level = 1)
                .first { it.category == AchievementCategory.TASK_VOLUME }
            assertThat(vol.earnedTier).isNull()
        }
    }

    // ========== Helpers ==========

    private fun today(): LocalDate = LocalDate.now()

    private fun taskCompletedAt(date: LocalDate): Task {
        val epochSeconds = date.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
        return Task(completedAt = Timestamp(epochSeconds, 0))
    }
}
