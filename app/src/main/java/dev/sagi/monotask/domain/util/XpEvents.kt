import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import kotlin.math.pow

object XpEvents {
    // ── Actions ──
    const val BASE_COMPLETION = 100

    // ── Bonuses ──
    const val BONUS_ACE = 50
    const val BONUS_HIGH_IMPORTANCE = 30
    const val BONUS_MEDIUM_IMPORTANCE = 10

    // ── Penalties (Immediate Deduction) ──
    const val SNOOZE_MANUAL = -30
    const val SNOOZE_RANDOM = -20
    const val SNOOZE_NEXT = -15
    const val SNOOZE_DUE_SOON = -10
    const val SNOOZE_HIGH_IMPORTANCE = -10

    // ── Calculation Helper ──
    fun calculateCompletionXp(task: Task): Int {
        var xp = BASE_COMPLETION

        // Importance Bonus
        xp += when (task.importance) {
            Importance.HIGH -> BONUS_HIGH_IMPORTANCE
            Importance.MEDIUM -> BONUS_MEDIUM_IMPORTANCE
            else -> 0
        }

        // ACE Bonus (only if never snoozed)
        if (task.isAce) {
            xp += BONUS_ACE
        }

        return xp.coerceAtLeast(10) // Set a minimum XP value of 10
    }

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
