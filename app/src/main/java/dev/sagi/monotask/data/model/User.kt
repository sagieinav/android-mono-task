package dev.sagi.monotask.data.model

import dev.sagi.monotask.domain.util.DiceBearHelper

data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
//    val avatarSeed: String = "",   // "" = auto-generated from id; otherwise, chosen preset key
    val avatarPreset: Int = 0,   // 0 = auto-generated from uid; otherwise, R.drawable.avatar_micahXX
    val level: Int = 1,
    val xp: Int = 0,
    val currentWorkspaceId: String = "",
    val friends: List<String> = emptyList(),        // List of user IDs

    // Persistent user settings
    val onboarded: Boolean = false,               // For first-launch onboarding
    val hardcoreModeEnabled: Boolean = false,     // Critical for NavGuard
    val notificationsEnabled: Boolean = true,
    val dueSoonDays: Int = 3                        // User-defined urgency threshold
) {

    val isAutoAvatar: Boolean get() = avatarPreset == 0

    // Computed only in-memory, never stored in Firestore
    fun resolvedAvatarUrl(size: Int = 512): String = DiceBearHelper.getAvatarUrl(id, size)
}
