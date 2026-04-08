package dev.sagi.monotask.data.model

import androidx.annotation.Keep
import dev.sagi.monotask.designsystem.theme.IconPack

@Keep
data class UserStats(
    val totalTasksCompleted: Int = 0,
    val aceCount: Int  = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,

    /** Resets each week alongside [weekStartEpochDay]. */
    val lastActiveEpochDay: Long = 0L,
    val weekStartEpochDay: Long = 0L,
    val weeklyXp: Int = 0,

    /**
     * Firestore-serialized achievement progress.
     * Key: [AchievementCategory.name], Value: [AchievementTier.name]
     * e.g. "STREAKS" → "GOLD"
     */
    val earnedAchievements: Map<String, String> = emptyMap()
)

@Keep
data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",

    val avatarPreset: Int = 1,

    val level: Int = 1,
    val xp: Int = 0,
    val currentWorkspaceId: String = "",
    val friends: List<String> = emptyList(), // List of user IDs
    val stats: UserStats   = UserStats(),

    // ========== Persistent user settings ==========
    /** True once the user has completed onboarding. Controls first-launch flow. */
    val onboarded: Boolean = false, // For first-launch onboarding

    /** When enabled, Kanban navigation is blocked. Checked by NavGuard. */
    val hyperfocusModeEnabled: Boolean = false, // Critical for NavGuard

    val dueDateWeight: Float = 0.5f
) {
    val avatarDrawableRes: Int
        get() = IconPack.AvatarPresets.getOrNull(avatarPreset - 1) ?: IconPack.AvatarPresets.first()
}
