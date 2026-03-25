package dev.sagi.monotask.data.model

import androidx.annotation.Keep
import dev.sagi.monotask.domain.util.DiceBearHelper

@Keep
data class UserStats(
    val totalTasksCompleted : Int                 = 0,
    val aceCount            : Int                 = 0,
    val currentStreak       : Int                 = 0,
    val longestStreak       : Int                 = 0,
    val weeklyXp            : Int                 = 0,
    val weekStartEpochDay   : Long                = 0L,
    val lastActiveEpochDay  : Long                = 0L,
    val earnedAchievements  : Map<String, String> = emptyMap()
)

@Keep
data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarPreset: Int = 0,   // 0 = auto-generated from uid; otherwise, R.drawable.avatar_micahXX
    val level: Int = 1,
    val xp: Int = 0,
    val currentWorkspaceId: String = "",
    val friends: List<String> = emptyList(),        // List of user IDs
    val stats  : UserStats   = UserStats(),

    // Persistent user settings
    val onboarded: Boolean = false,               // For first-launch onboarding
    val hardcoreModeEnabled: Boolean = false,     // Critical for NavGuard
    val notificationsEnabled: Boolean = true,
    val dueSoonDays: Int = 3,                       // User-defined urgency threshold
    val dueDateWeight: Float = 0.5f,
    val importanceWeight: Float = 0.5f
) {

    val isAutoAvatar: Boolean get() = avatarPreset == 0

    // Computed only in-memory, never stored in Firestore
    fun resolvedAvatarUrl(size: Int = 512): String = DiceBearHelper.getAvatarUrl(id, size)
}
