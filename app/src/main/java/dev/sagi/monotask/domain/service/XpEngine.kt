package dev.sagi.monotask.domain.service

import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import kotlin.math.pow

object XpEngine {
    // ========== Actions==========
    const val BASE_COMPLETION = 100

    // ========== Bonuses ==========
    const val BONUS_ACE = 50
    const val BONUS_HIGH_IMPORTANCE = 30
    const val BONUS_MEDIUM_IMPORTANCE = 10

    // ========== Snooze Options (penalty baked in) ==========
    enum class SnoozeOption(val penalty: Int) {
        MANUAL(-30),        // triggered from Kanban card long-press
        NEXT_IN_QUEUE(-15), // snooze sheet option 1
        BY_DUE_DATE(-10)    // snooze sheet option 2
    }

    // Stacked penalty for snoozing a high-importance task (any option)
//    const val SNOOZE_HIGH_IMPORTANCE_EXTRA = -10



// ==============================
// TASK XP EVENTS
// ==============================


    // Called on task creation and edit. Computes XP from scratch
    fun calculateTaskXp(task: Task): Int {
        var xp = BASE_COMPLETION
        xp += when (task.importance) {
            Importance.HIGH   -> BONUS_HIGH_IMPORTANCE
            Importance.MEDIUM -> BONUS_MEDIUM_IMPORTANCE
            else              -> 0
        }
        if (task.isAce) xp += BONUS_ACE
        return xp.coerceAtLeast(10)
    }

    // Called on snooze. Applies penalty on top of current stored XP
    fun calculateXpAfterSnooze(task: Task, option: SnoozeOption): Int {
        val penalty = snoozePenalty(task, option)
        return (task.currentXp + penalty).coerceAtLeast(10)
    }

    // Penalty calculation logic
    private fun snoozePenalty(task: Task, option: SnoozeOption): Int {
        var penalty = option.penalty
        if (task.isAce) penalty -= 50  // Ace tasks lose less XP on snooze
        return penalty
    }



// ==============================
// USER XP EVENTS
// ==============================

    /**
     * Total XP required to REACH a given level.
     * Formula: 100 * (level - 1)^1.8
     *
     * Level 1  →     0 XP
     * Level 2  →   100 XP
     * Level 3  →   348 XP
     * Level 5  →  1212 XP
     * Level 10 →  5230 XP
     * Level 20 → 16,720 XP
     *
     * Curve is gentle early (rewarding for new users) and
     * progressively harder at high levels (rewarding for power users).
     */
    fun xpRequiredForLevel(level: Int): Int =
        if (level <= 1) 0
        else (100.0 * (level - 1).toDouble().pow(1.8)).toInt()

    // Returns what level a user is at given their total XP.
    fun levelForXp(xp: Int): Int {
        var level = 1
        while (xp >= xpRequiredForLevel(level + 1)) level++
        return level
    }

    // XP progress within the current level (0.0 to 1.0) — used for progress bar.
    fun progressToNextLevel(xp: Int): Float {
        val current = levelForXp(xp)
        val currentThreshold = xpRequiredForLevel(current).toFloat()
        val nextThreshold = xpRequiredForLevel(current + 1).toFloat()
        return ((xp - currentThreshold) / (nextThreshold - currentThreshold)).coerceIn(0f, 1f)
    }
}
