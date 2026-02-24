package dev.sagi.monotask.domain.util

import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import com.google.firebase.Timestamp
import kotlin.math.exp
import kotlin.random.Random

object PriorityCalculator {
    // Returns the single highest-priority task from a list (the task displayed on the Focus Hub)
    fun getTopTask(tasks: List<Task>, workspace: Workspace): Task? {
        if (tasks.isEmpty()) return null
        return tasks.maxByOrNull { score(it, workspace) }
    }

    // Returns all tasks sorted by priority score descending
    fun getSortedTasks(tasks: List<Task>, workspace: Workspace): List<Task> =
        tasks.sortedByDescending { score(it, workspace) }

    /**
     * Core scoring function. Pure — no side effects, no I/O.
     * Deterministic except for the small randomness term.
     *
     * Due Date Score:
     *   Uses a sigmoid-style urgency curve so that tasks due very soon
     *   score much higher, but tasks with no due date aren't penalized
     *   to zero — they just score at a neutral midpoint (0.5).
     *
     * Importance Score:
     *   LOW = 0.33, MEDIUM = 0.66, HIGH = 1.0
     *   Simple linear scale — straightforward to explain and defend.
     */
    private fun score(task: Task, workspace: Workspace): Double {
        val dueDateScore = dueDateUrgency(task.dueDate)
        val importanceScore = importanceScore(task.importance)
        val noise = Random.nextDouble(
            -workspace.randomnessFactor.toDouble(),
            workspace.randomnessFactor.toDouble()
        )

        // This is the importance calculation function
        return (workspace.dueDateWeight   * dueDateScore) +
                (workspace.importanceWeight * importanceScore) +
                noise
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
        if (dueDate == null) return 0.5  // no due date = neutral

        val now = System.currentTimeMillis()
        val dueMillis = dueDate.toDate().time
        val daysUntilDue = (dueMillis - now) / (1000.0 * 60 * 60 * 24)

        // Sigmoid func centered at 3 days. smooth curve, no hard cutoffs
        return 1.0 / (1.0 + exp(0.5 * (daysUntilDue - 3)))
    }

    private fun importanceScore(importance: Importance): Double =
        when (importance) {
            Importance.LOW    -> 0.33
            Importance.MEDIUM -> 0.67
            Importance.HIGH   -> 1.0
        }
}
