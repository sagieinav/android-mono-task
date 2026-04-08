package dev.sagi.monotask.ui.profile

import androidx.annotation.DrawableRes
import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.service.ActivityStats

// ========== Friend Stats (pre-computed for FriendExpandedContent) ==========

data class FriendStats(
    val badges : List<Achievement>,
    val xpPoints : List<ActivityStats.ChartPoint>,
    val xpTrend : Int,
    val totalWeekXp : Int
)

// ========== UI States ==========

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    data class Ready(
        val user : User,
        val level : Int,
        val levelProgress : Float,
        val xpIntoLevel : Int,
        val xpForNextLevel : Int,
        val achievements : List<Achievement>,
        val showAvatarPicker: Boolean = false
    ) : ProfileUiState()
}

// ========== Event Callbacks ==========

sealed interface ProfileEvent {
    data object RefreshPage : ProfileEvent
    data class UpdateProfile(val displayName: String) : ProfileEvent
    data class SelectAvatar(@DrawableRes val preset: Int) : ProfileEvent
    object OpenAvatarPicker : ProfileEvent
    object DismissAvatarPicker : ProfileEvent
    data class RemoveFriend(val friendId: String) : ProfileEvent
}

// ========== One-Shot UI Effects ==========

sealed interface ProfileUiEffect {
    data class ShowError(val message: String) : ProfileUiEffect
}
