package dev.sagi.monotask.data.model

data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val profilePicUrl: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    val currentWorkspaceId: String = "",
    val friends: List<String> = emptyList(),        // List of user IDs

    // Persistent user settings
    val onboarded: Boolean = false,               // For first-launch onboarding
    val hardcoreModeEnabled: Boolean = false,     // Critical for NavGuard
    val notificationsEnabled: Boolean = true,
    val dueSoonDays: Int = 3                        // User-defined urgency threshold
)
