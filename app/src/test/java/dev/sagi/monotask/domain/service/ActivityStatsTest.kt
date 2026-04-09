package dev.sagi.monotask.domain.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isTrue
import assertk.assertions.isNull
import dev.sagi.monotask.data.model.DailyActivity
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ActivityStatsTest {

    private val today: LocalDate = LocalDate.now()

    private fun activityFor(daysBack: Long, xp: Int = 100, tasks: Int = 1) = DailyActivity(
        dateEpochDay = today.minusDays(daysBack).toEpochDay(),
        xpEarned = xp,
        tasksCompleted = tasks
    )

    // ========== buildXpPoints ==========

    @Nested inner class BuildXpPoints {

        @Test fun `returns exactly 7 points`() {
            assertThat(ActivityStats.buildXpPoints(emptyList()).size).isEqualTo(7)
        }

        @Test fun `missing days fill with 0`() {
            val points = ActivityStats.buildXpPoints(emptyList())
            assertThat(points.all { it.value == 0f }).isTrue()
        }

        @Test fun `today maps to last point`() {
            val data = listOf(activityFor(0, xp = 200))
            val points = ActivityStats.buildXpPoints(data)
            assertThat(points.last().value).isEqualTo(200f)
        }

        @Test fun `yesterday maps to second-to-last point`() {
            val data = listOf(activityFor(1, xp = 150))
            val points = ActivityStats.buildXpPoints(data)
            assertThat(points[5].value).isEqualTo(150f)
        }
    }

    // ========== buildTaskPoints ==========

    @Nested inner class BuildTaskPoints {

        @Test fun `returns exactly 7 points`() {
            assertThat(ActivityStats.buildTaskPoints(emptyList()).size).isEqualTo(7)
        }

        @Test fun `known task count maps correctly`() {
            val data = listOf(activityFor(0, tasks = 5))
            val points = ActivityStats.buildTaskPoints(data)
            assertThat(points.last().value).isEqualTo(5f)
        }
    }

    // ========== computeXpTrend ==========

    @Nested inner class ComputeXpTrend {

        @Test fun `positive when recent 3 days average exceeds previous 3 days`() {
            // buildXpPoints: index 0=6 days ago ... index 6=today
            // computeTrend drops today (last), then: recent=days 1-3, previous=days 4-6
            val data = (1..6).map { daysBack ->
                activityFor(daysBack.toLong(), xp = if (daysBack <= 3) 200 else 100)
            }
            assertThat(ActivityStats.computeXpTrend(data)).isGreaterThan(0)
        }

        @Test fun `returns 0 when all values are zero`() {
            assertThat(ActivityStats.computeXpTrend(emptyList())).isEqualTo(0)
        }
    }

    // ========== computeRecordStreak ==========

    @Nested inner class ComputeRecordStreak {

        @Test fun `empty list returns 0`() {
            assertThat(ActivityStats.computeRecordStreak(emptyList())).isEqualTo(0)
        }

        @Test fun `3 consecutive active days returns 3`() {
            val data = (0L..2L).map { activityFor(it) }
            assertThat(ActivityStats.computeRecordStreak(data)).isEqualTo(3)
        }

        @Test fun `gap resets streak`() {
            val data = listOf(activityFor(0), activityFor(2)) // day 1 missing
            assertThat(ActivityStats.computeRecordStreak(data)).isEqualTo(1)
        }

        @Test fun `days with 0 tasks completed do not count`() {
            val data = listOf(
                activityFor(0, tasks = 0),
                activityFor(1, tasks = 1)
            )
            assertThat(ActivityStats.computeRecordStreak(data)).isEqualTo(1)
        }

        @Test fun `range filter restricts data`() {
            val data = (0L..10L).map { activityFor(it) }
            val range = today.minusDays(3).toEpochDay()..today.toEpochDay()
            assertThat(ActivityStats.computeRecordStreak(data, range)).isEqualTo(4)
        }
    }

    // ========== last7Days ==========

    @Nested inner class Last7Days {

        @Test fun `returns exactly 7 entries`() {
            assertThat(ActivityStats.last7Days(emptyList()).size).isEqualTo(7)
        }

        @Test fun `all entries are null when no data provided`() {
            val result = ActivityStats.last7Days(emptyList())
            assertThat(result.all { it == null }).isTrue()
        }

        @Test fun `today maps to last entry`() {
            val data = listOf(activityFor(0, xp = 500))
            val result = ActivityStats.last7Days(data)
            assertThat(result.last()?.xpEarned).isEqualTo(500)
        }

        @Test fun `missing day resolves to null`() {
            val data = listOf(activityFor(0))
            val result = ActivityStats.last7Days(data)
            assertThat(result.first()).isNull()
        }
    }
}
