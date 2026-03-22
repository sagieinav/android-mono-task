package dev.sagi.monotask.data.model

import androidx.annotation.Keep

enum class AchievementTier { BRONZE, SILVER, GOLD }

enum class AchievementCategory {
    STREAKS, TASK_VOLUME, DISCIPLINE, XP_LEVELING;

    val displayName: String get() = when (this) {
        STREAKS     -> "Consistency"
        TASK_VOLUME -> "Task Volume"
        DISCIPLINE  -> "Discipline"
        XP_LEVELING -> "XP & Leveling"
    }
}

@Keep
data class AchievementMilestone(
    val tier        : AchievementTier,
    val name        : String,
    val description : String
)

data class Achievement(
    val category   : AchievementCategory,
    val iconRes    : Int,
    val earnedTier : AchievementTier?,
    val milestones : List<AchievementMilestone>  // ordered: [BRONZE, SILVER, GOLD]. Indexed by AchievementTier.ordinal
) {
    val isLocked: Boolean get() = earnedTier == null

    // Label shown below the badge: earned tier's name, or first tier as target when locked
    val displayName: String get() = earnedMilestone?.name ?: milestones.first().name

    val earnedMilestone: AchievementMilestone? get() = earnedTier?.let { milestones[it.ordinal] }

    val nextTier: AchievementMilestone? get() = milestones.getOrNull((earnedTier?.ordinal ?: -1) + 1)

    fun isEarned(tier: AchievementTier): Boolean =
        earnedTier != null && earnedTier.ordinal >= tier.ordinal
}

