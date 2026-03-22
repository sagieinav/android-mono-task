package dev.sagi.monotask.data.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color

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
    val bronze     : AchievementMilestone,
    val silver     : AchievementMilestone,
    val gold       : AchievementMilestone
) {
    val isLocked: Boolean get() = earnedTier == null

    // Label shown below the badge: earned tier's name, or bronze as first target when locked
    val displayName: String get() = when (earnedTier) {
        AchievementTier.GOLD   -> gold.name
        AchievementTier.SILVER -> silver.name
        AchievementTier.BRONZE -> bronze.name
        null                   -> bronze.name
    }

    val nextTier: AchievementMilestone? get() = when (earnedTier) {
        null                   -> bronze
        AchievementTier.BRONZE -> silver
        AchievementTier.SILVER -> gold
        AchievementTier.GOLD   -> null
    }

    val earnedMilestone: AchievementMilestone? get() = when (earnedTier) {
        AchievementTier.BRONZE -> bronze
        AchievementTier.SILVER -> silver
        AchievementTier.GOLD   -> gold
        null                   -> null
    }

    val tierColor: Color get() = when (earnedTier) {
        AchievementTier.GOLD   -> AchievementColorGold
        AchievementTier.SILVER -> AchievementColorSilver
        AchievementTier.BRONZE -> AchievementColorBronze
        null                   -> AchievementColorLocked
    }

    val nextTierColor: Color? get() = when (nextTier?.tier) {
        AchievementTier.BRONZE -> AchievementColorBronze
        AchievementTier.SILVER -> AchievementColorSilver
        AchievementTier.GOLD   -> AchievementColorGold
        null                   -> null
    }

    fun isEarned(tier: AchievementTier): Boolean = when (tier) {
        AchievementTier.BRONZE -> earnedTier != null
        AchievementTier.SILVER -> earnedTier == AchievementTier.SILVER || earnedTier == AchievementTier.GOLD
        AchievementTier.GOLD   -> earnedTier == AchievementTier.GOLD
    }
}

// Static tier colors (not theme-adaptive). Fixed metallic shades
val AchievementColorGold   = Color(0xFFDAA321)
val AchievementColorSilver = Color(0xFF9CA3A0)
val AchievementColorBronze = Color(0xFFa97142)
val AchievementColorLocked = Color(0xFFE1E1E1)
