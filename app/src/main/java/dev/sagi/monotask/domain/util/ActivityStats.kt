package dev.sagi.monotask.domain.util

import dev.sagi.monotask.data.model.DailyActivity
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

object ActivityStats {

    // ========== Model ==========

    // Shared point model used by all statistics chart components.
    data class ChartPoint(val value: Float, val label: String)

    // ========== XP helpers ==========

    // Builds a 7-day XP window (oldest → newest, ending today).
    // Days with no recorded activity default to 0.
    fun buildXpPoints(data: List<DailyActivity>): List<ChartPoint> =
        buildWindow(data) { it.xpEarned.toFloat() }

    // Compares average XP of the last 3 complete days vs the 3 before those.
    // Today excluded — it's almost always lower mid-day.
    fun computeXpTrend(data: List<DailyActivity>): Int =
        computeTrend(buildXpPoints(data))

    // ========== Task helpers ==========

    // Builds a 7-day tasks-completed window (oldest → newest, ending today).
    fun buildTaskPoints(data: List<DailyActivity>): List<ChartPoint> =
        buildWindow(data) { it.tasksCompleted.toFloat() }

    // Compares average tasks completed of the last 3 complete days vs the 3 before those.
    fun computeTaskTrend(data: List<DailyActivity>): Int =
        computeTrend(buildTaskPoints(data))

    // ========== Streak helpers ==========

    // Current streak = consecutive active days going backwards from today.
    // Allows today to still count even if not yet active (checks yesterday as fallback start).
    fun computeCurrentStreak(data: List<DailyActivity>): Int {
        if (data.isEmpty()) return 0
        val days = data
            .filter { it.tasksCompleted > 0 }
            .map { it.dateEpochDay }
            .toSortedSet()
            .sortedDescending()

        var streak   = 0
        var expected = LocalDate.now().toEpochDay()
        for (day in days) {
            if (day == expected) {
                streak++
                expected = day - 1
            } else if (streak == 0 && day == expected - 1) {
                // Grace: today isnt over yet, start counting from yesterday
                streak++
                expected = day - 1
            } else break
        }
        return streak
    }

    // Record streak = longest consecutive active day run across all time/time range
    fun computeRecordStreak(
        data: List<DailyActivity>,
        range: ClosedRange<Long>? = null
    ): Int {
        if (data.isEmpty()) return 0
        val days = data
            .asSequence()
            .filter { it.tasksCompleted > 0 }
            .filter { range == null || it.dateEpochDay in range }
            .map { it.dateEpochDay }
            .toSortedSet()
            .sorted()
            .toList()

        var best    = 0
        var current = 0
        var prev    = Long.MIN_VALUE
        for (day in days) {
            current = if (day == prev + 1) current + 1 else 1
            if (current > best) best = current
            prev = day
        }
        return best
    }

    // ========== Date window helpers ==========

    // Returns the last 7 DailyActivity entries (null for days with no data), oldest → newest.
    fun last7Days(data: List<DailyActivity>): List<DailyActivity?> {
        val today = LocalDate.now()
        val map   = data.associateBy { it.dateEpochDay }
        return (6 downTo 0).map { daysBack ->
            map[today.minusDays(daysBack.toLong()).toEpochDay()]
        }
    }

    // Returns day-of-week labels (2-char) for the last 7 days, oldest → newest.
    fun last7DayLabels(): List<String> {
        val today = LocalDate.now()
        return (6 downTo 0).map { daysBack ->
            today.minusDays(daysBack.toLong())
                .dayOfWeek
                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                .take(2)
        }
    }

    // ========== Private internals ==========

    private fun buildWindow(
        data: List<DailyActivity>,
        selector: (DailyActivity) -> Float
    ): List<ChartPoint> {
        val today = LocalDate.now()
        val map   = data.associateBy { it.dateEpochDay }
        return (6 downTo 0).map { daysBack ->
            val date = today.minusDays(daysBack.toLong())
            ChartPoint(
                value = map[date.toEpochDay()]?.let(selector) ?: 0f,
                label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3)
            )
        }
    }

    private fun computeTrend(points: List<ChartPoint>): Int {
        val complete = points.dropLast(1)   // drop today
        if (complete.size < 4) return 0
        val recent      = complete.takeLast(3)
        val previous    = complete.dropLast(3).takeLast(3)
        if (previous.isEmpty()) return 0
        val avgRecent   = recent.map { it.value }.average()
        val avgPrevious = previous.map { it.value }.average()
        if (avgPrevious == 0.0) return 0
        return ((avgRecent - avgPrevious) / avgPrevious * 100).toInt()
    }
}
