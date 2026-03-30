package dev.sagi.monotask.domain.service

import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import com.google.firebase.Timestamp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.math.exp
import kotlin.time.Clock
import kotlin.time.Instant

object TaskSelector {
    // Returns the single highest-priority task from a list (the task displayed on the Focus Hub)
    fun getTopTask(
        tasks: List<Task>,
        dueDateWeight: Float,
        excludeId: String? = null
    ): Task? {
        if (tasks.isEmpty()) return null
        val now = now()

        val candidates = if (excludeId != null) tasks.filter { it.id != excludeId } else tasks
        val pool = candidates.ifEmpty { tasks } // fallback if only 1 task exists

        val nonSnoozed = pool.filter { it.snoozeCount == 0 }
        return (nonSnoozed.ifEmpty { pool })
            .maxByOrNull { priorityScore(it, dueDateWeight, now) }
    }

    fun getTopTaskByDueDate(
        tasks: List<Task>,
        dueDateWeight: Float,
        excludeId: String? = null
    ): Task? {
        val candidates = tasks.filter { it.id != excludeId }
        if (candidates.isEmpty()) return null
        val now = now()

        val withDueDate = candidates.filter { it.dueDate != null }

        return if (withDueDate.isNotEmpty()) {
            withDueDate.sortedWith(
                compareBy<Task> { it.dueDate!!.toDate() }                  // 1. soonest due date
                    .thenBy { priorityScore(it, dueDateWeight, now) }       // 2. tiebreaker: normal priority
            ).first()
        } else {
            // Fallback: no tasks have a due date → normal priority
            getTopTask(candidates, dueDateWeight, excludeId = null)
        }
    }

    // Returns all tasks sorted by priority score descending
    fun getSortedTasks(
        tasks: List<Task>,
        dueDateWeight: Float
    ): List<Task> {
        val now = now()
        return tasks.sortedByDescending { priorityScore(it, dueDateWeight, now) }
    }

    // Computed once per scoring pass and threaded through to avoid repeated system calls
    private data class Now(val timeZone: TimeZone, val today: LocalDate)
    private fun now(): Now {
        val tz = TimeZone.currentSystemDefault()
        return Now(tz, Clock.System.now().toLocalDateTime(tz).date)
    }

    /**
     * Core scoring function. Fully deterministic.
     *
     * Due Date Score:
     *   Uses a sigmoid-style urgency curve so that tasks due very soon
     *   score much higher, but tasks with no due date aren't penalized
     *   to zero - they just score at a neutral midpoint (0.5).
     *
     * Importance Score:
     *   LOW = 0.33, MEDIUM = 0.66, HIGH = 1.0
     *   Simple linear scale - straightforward to explain and defend.
     */
    private fun priorityScore(
        task: Task,
        dueDateWeight: Float,
        now: Now
    ): Double {
        val importanceWeight = 1f - dueDateWeight
        val rawScore = dueDateWeight   * dueDateUrgency(task.dueDate, now) +
                       importanceWeight * importanceScore(task.importance)

        // Apply snooze penalty: each snooze reduces priority by ~33%
        val snoozePenalty = 1.0 / (1.0 + task.snoozeCount * 0.5)
        return rawScore * snoozePenalty
    }

    /**
     * Converts a due date into a 0.0–1.0 urgency score (Sigmoid func).
     *
     * Urgency curve (days until due → score):
     *   No due date → 0.5   (neutral, won't dominate or disappear)
     *   14+ days    → ~0.1  (low urgency)
     *   7 days      → ~0.5  (moderate urgency)
     *   3 days      → ~0.8  (high urgency)
     *   0 days      → ~1.0  (overdue or due today. max urgency)
     *   Overdue     → 1.0   (clamped at max)
     *
     * Formula: 1 / (1 + e^(0.5 * (daysUntilDue - 3)))
     * This is a logistic (sigmoid) function centered at 3 days.
     */
    private fun dueDateUrgency(dueDate: Timestamp?, now: Now): Double {
        if (dueDate == null) return 0.5

        val due = Instant.fromEpochMilliseconds(dueDate.toDate().time).toLocalDateTime(now.timeZone).date
        val daysUntilDue = now.today.daysUntil(due).toDouble()

        // Sigmoid func centered at 3 days
        return 1.0 / (1.0 + exp(0.5 * (daysUntilDue - 3)))
    }

    private fun importanceScore(importance: Importance): Double =
        when (importance) {
            Importance.LOW    -> 0.33
            Importance.MEDIUM -> 0.67
            Importance.HIGH   -> 1.0
        }
}
