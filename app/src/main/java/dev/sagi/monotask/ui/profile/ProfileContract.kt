package dev.sagi.monotask.ui.profile

import dev.sagi.monotask.data.model.Badge
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.model.Workspace

// ========== UI States ==========

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    data class Ready(
        val user           : User,
        val level          : Int,
        val levelProgress  : Float,
        val xpIntoLevel    : Int,
        val xpForNextLevel : Int,
        val badges         : List<Badge>,
        val activityData   : List<DailyActivity>   = emptyList(),
        val completedTasks : List<Task>            = emptyList(),
        val workspaces     : List<Workspace>       = emptyList(),
        val monthActivityData: List<DailyActivity> = emptyList()
    ) : ProfileUiState()
}

// ========== Event Callbacks ==========

sealed interface ProfileEvent {
    data class SearchUsers(val query: String) : ProfileEvent
    data class AddFriend(val friendId: String) : ProfileEvent
    data class UpdateProfile(val displayName: String, val profilePicUrl: String) : ProfileEvent
}

// ========== One-Shot UI Effects ==========

sealed interface ProfileUiEffect {
    data class ShowError(val message: String) : ProfileUiEffect
}
