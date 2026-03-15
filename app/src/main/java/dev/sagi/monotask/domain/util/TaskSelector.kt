package dev.sagi.monotask.domain.util

import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import com.google.firebase.Timestamp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.math.exp
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant

object TaskSelector {
    // Returns the single highest-priority task from a list (the task displayed on the Focus Hub)
    fun getTopTask(tasks: List<Task>, workspace: Workspace, excludeId: String? = null): Task? {
        if (tasks.isEmpty()) return null

        val candidates = if (excludeId != null) tasks.filter { it.id != excludeId } else tasks
        val pool = candidates.ifEmpty { tasks } // fallback if only 1 task exists

        val nonSnoozed = pool.filter { it.snoozeCount == 0 }
        return (nonSnoozed.ifEmpty { pool })
            .maxByOrNull { priorityScore(it, workspace) }
    }

    // Add alongside getTopTask()
    fun getTopTaskByDueDate(
        tasks: List<Task>,
        workspace: Workspace,
        excludeId: String? = null
    ): Task? {
        val candidates = tasks.filter { it.id != excludeId }
        if (candidates.isEmpty()) return null

        val withDueDate = candidates.filter { it.dueDate != null }

        return if (withDueDate.isNotEmpty()) {
            withDueDate.sortedWith(
                compareBy<Task> { it.dueDate!!.toDate() }      // 1. soonest due date
                    .thenBy { priorityScore(it, workspace) }   // 2. tiebreaker: normal priority
            ).first()
        } else {
            // Fallback: no tasks have a due date → normal priority
            getTopTask(candidates, workspace, excludeId = null)
        }
    }


    // Returns all tasks sorted by priority score descending
    fun getSortedTasks(tasks: List<Task>, workspace: Workspace): List<Task> =
        tasks.sortedByDescending { priorityScore(it, workspace) }

    /**
     * Core scoring function.
     * Deterministic except for the small randomness term.
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
    private fun priorityScore(task: Task, workspace: Workspace): Double {
        val dueDateScore = dueDateUrgency(task.dueDate)
        val importanceScore = importanceScore(task.importance)
        val noise = Random.nextDouble(
            -workspace.randomnessFactor.toDouble(),
            workspace.randomnessFactor.toDouble()
        )

        // This is the importance calculation:
        val rawScore = (workspace.dueDateWeight   * dueDateScore) +
                (workspace.importanceWeight * importanceScore) +
                noise

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
    private fun dueDateUrgency(dueDate: Timestamp?): Double {
        if (dueDate == null) return 0.5

        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val due = Instant.fromEpochMilliseconds(dueDate.toDate().time).toLocalDateTime(timeZone).date
        val daysUntilDue = today.daysUntil(due).toDouble()

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
