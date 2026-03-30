package dev.sagi.monotask.data.model

import androidx.annotation.Keep

// ========== Achievement Tier ==========
enum class AchievementTier {
    BRONZE, SILVER, GOLD
}

// ========== Achievement Category ==========
enum class AchievementCategory {
    STREAKS, TASK_VOLUME, DISCIPLINE, XP_LEVELING;

    val displayName: String get() = when (this) {
        STREAKS     -> "Consistency"
        TASK_VOLUME -> "Task Volume"
        DISCIPLINE  -> "Discipline"
        XP_LEVELING -> "XP & Leveling"
    }
}

/** A single tier milestone within an [Achievement]: its tier, name, and description. */
@Keep // Prevents R8 from stripping fields used reflectively (Firestore deserialization)
data class AchievementMilestone(
    val tier        : AchievementTier,
    val name        : String,
    val description : String
)

/**
 * Represents a player's progress in a single achievement category.
 * @param milestones Ordered [BRONZE, SILVER, GOLD]. indexed by [AchievementTier.ordinal].
 * @param earnedTier The highest tier the user has reached, or null if not yet earned.
 */
data class Achievement(
    val category   : AchievementCategory,
    val iconRes    : Int,
    val earnedTier : AchievementTier?,
    val milestones : List<AchievementMilestone>  // ordered: [BRONZE, SILVER, GOLD]. Indexed by AchievementTier.ordinal
) {
    val isLocked: Boolean get() = earnedTier == null

    /** Earned tier's name, or the first tier's name as a locked target. */
    val displayName: String get() = earnedMilestone?.name ?: milestones.first().name

    val earnedMilestone: AchievementMilestone? get() = earnedTier?.let { milestones[it.ordinal] }

    /** The next milestone to unlock, or null if Gold is already earned. */
    val nextMilestone: AchievementMilestone? get() = milestones.getOrNull((earnedTier?.ordinal ?: -1) + 1)

    /** True if [tier] has been reached or surpassed. Uses enum's natural ordinal ordering. */
    fun isEarned(tier: AchievementTier): Boolean = earnedTier != null && earnedTier >= tier
}

