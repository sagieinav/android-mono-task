package dev.sagi.monotask.ui.profile

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.repository.ActivityRepository
import dev.sagi.monotask.domain.repository.UserRepository
import dev.sagi.monotask.domain.service.AchievementEngine
import dev.sagi.monotask.domain.service.ActivityStats
import dev.sagi.monotask.domain.service.XpEngine
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository : UserRepository,
    private val activityRepository: ActivityRepository,
) : BaseViewModel<ProfileUiState, ProfileEvent, ProfileUiEffect>() {

    override val initialState: ProfileUiState = ProfileUiState.Loading

    private val _friendUsers = MutableStateFlow<List<User>?>(null)
    val friendUsers: StateFlow<List<User>?> = _friendUsers.asStateFlow()

    private val _friendActivities = MutableStateFlow<Map<String, List<DailyActivity>>>(emptyMap())

    val friendStats: StateFlow<Map<String, FriendStats>> = combine(
        _friendUsers,
        _friendActivities
    ) { users, activitiesMap ->
        users?.associate { user ->
            val weekActivity = ActivityStats.weekActivity(activitiesMap[user.id] ?: emptyList())
            user.id to FriendStats(
                badges = AchievementEngine.evaluateFromStats(user.stats, XpEngine.levelForXp(user.xp)),
                xpPoints = ActivityStats.buildXpPoints(weekActivity),
                xpTrend = ActivityStats.computeXpTrend(weekActivity),
                totalWeekXp = weekActivity.sumOf { it.xpEarned }
            )
        } ?: emptyMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private var userId: String = ""
    private var observing = false  // safe: only touched on Main dispatcher

    init {
        viewModelScope.launch { userId = AuthUtils.awaitUid() }
    }

    // ==========================================================================
    // Event Dispatcher
    // ==========================================================================

    override fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.UpdateProfile -> updateProfile(event.displayName)
            is ProfileEvent.SelectAvatar -> selectAvatar(event.preset)
            is ProfileEvent.OpenAvatarPicker -> setAvatarPicker(true)
            is ProfileEvent.DismissAvatarPicker -> setAvatarPicker(false)
            is ProfileEvent.RemoveFriend -> removeFriend(event.friendId)
            is ProfileEvent.RefreshPage -> { /* handled by StatisticsViewModel */ }
        }
    }

    // ==========================================================================
    // User observation
    // ==========================================================================

    fun startObserving(userFlow: StateFlow<User?>) {
        if (observing) return
        observing = true

        userFlow
            .onEach { user ->
                if (user == null) {
                    _uiState.value = ProfileUiState.Loading
                    return@onEach
                }

                val level = XpEngine.levelForXp(user.xp)
                val progress = XpEngine.progressToNextLevel(user.xp)
                val xpForNext = XpEngine.xpRequiredForLevel(level + 1)
                val xpForCurrent = XpEngine.xpRequiredForLevel(level)
                val xpIntoLevel = user.xp - xpForCurrent

                val existing = _uiState.value as? ProfileUiState.Ready
                _uiState.value = existing?.copy(
                    user = user,
                    level = level,
                    levelProgress = progress,
                    xpIntoLevel = xpIntoLevel,
                    xpForNextLevel = xpForNext - xpForCurrent,
                ) ?: ProfileUiState.Ready(
                    user = user,
                    level = level,
                    levelProgress = progress,
                    xpIntoLevel = xpIntoLevel,
                    xpForNextLevel = xpForNext - xpForCurrent,
                    achievements = emptyList(),
                )

                loadAchievements(user, level)
            }
            .launchIn(viewModelScope)

        // Stream full User objects in real-time whenever the friends ID list changes
        userFlow
            .map { it?.friends ?: emptyList() }
            .distinctUntilChanged()
            .flatMapLatest { friendIds ->
                if (friendIds.isEmpty()) flowOf(emptyList())
                else combine(friendIds.map { id -> userRepository.getUserStream(id) }) { users ->
                    users.filterNotNull().toList()
                }
            }
            .onEach { users -> _friendUsers.value = users }
            .launchIn(viewModelScope)

        // One-shot activity fetch whenever the friends ID list changes
        userFlow
            .map { it?.friends ?: emptyList() }
            .distinctUntilChanged()
            .onEach { friendIds ->
                _friendActivities.value = friendIds.associateWith { id ->
                    activityRepository.getActivityOnce(id, ActivityRepository.thisMonthRange)
                }
            }
            .launchIn(viewModelScope)
    }

    // ==========================================================================
    // Achievements (loaded once per user, from stored stats)
    // ==========================================================================

    private var achievementsLoaded = false

    private fun loadAchievements(user: User, level: Int) {
        if (achievementsLoaded) return
        achievementsLoaded = true
        val current = _uiState.value as? ProfileUiState.Ready ?: return
        val achievements = AchievementEngine.evaluateFromStats(user.stats, level)
        _uiState.value = current.copy(achievements = achievements)
    }

    // ==========================================================================
    // Profile editing
    // ==========================================================================

    private fun updateProfile(displayName: String) {
        viewModelScope.launch {
            try {
                userRepository.updateProfile(userId, displayName)
            } catch (e: Exception) {
                sendEffect(ProfileUiEffect.ShowError("Failed to update profile: ${e.message}"))
            }
        }
    }

    private fun setAvatarPicker(show: Boolean) {
        val current = _uiState.value as? ProfileUiState.Ready ?: return
        _uiState.value = current.copy(showAvatarPicker = show)
    }

    private fun selectAvatar(@DrawableRes preset: Int) {
        viewModelScope.launch {
            try {
                userRepository.updateAvatarPreset(userId, preset)
                setAvatarPicker(false)
            } catch (e: Exception) {
                sendEffect(ProfileUiEffect.ShowError("Failed to update avatar: ${e.message}"))
            }
        }
    }

    // ==========================================================================
    // Friends & Invite
    // ==========================================================================

    private fun removeFriend(friendId: String) {
        viewModelScope.launch {
            try {
                userRepository.removeFriendBatch(userId, friendId)
            } catch (e: Exception) {
                sendEffect(ProfileUiEffect.ShowError("Failed to remove friend: ${e.message}"))
            }
        }
    }

    fun generateInviteLink(): String = "monotask://invite?uid=$userId"

    fun shareInviteLink(context: Context) {
        val link = generateInviteLink()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Join me on MonoTask! $link")
        }
        context.startActivity(Intent.createChooser(intent, "Share invite link"))
    }

    // ==========================================================================
    // Lifecycle
    // ==========================================================================

    override fun onCleared() {
        super.onCleared()
        // viewModelScope is canceled here, stopping the user stream launched via
        // launchIn(viewModelScope) in startObserving()
    }
}
