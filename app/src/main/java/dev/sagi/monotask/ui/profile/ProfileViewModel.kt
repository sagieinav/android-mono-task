package dev.sagi.monotask.ui.profile

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.domain.util.AchievementEngine
import dev.sagi.monotask.domain.util.XpEvents
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository : UserRepository,
    private val taskRepository : TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _friendUsers = MutableStateFlow<List<User>?>(null)
    val friendUsers: StateFlow<List<User>?> = _friendUsers.asStateFlow()

    private val _friendActivities = MutableStateFlow<Map<String, List<DailyActivity>>>(emptyMap())
    val friendActivities: StateFlow<Map<String, List<DailyActivity>>> = _friendActivities.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ProfileUiEffect>()
    val uiEffect: SharedFlow<ProfileUiEffect> = _uiEffect.asSharedFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var userId: String = ""

    private var observing        = false  // safe: only touched on Main dispatcher
    private var statisticsLoaded = false  // guard: loadStatisticsData runs once

    init {
        viewModelScope.launch { userId = AuthUtils.awaitUid() }
    }

    // ==========================================================================
    // Event Dispatcher
    // ==========================================================================

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.RefreshPage         -> refreshPage()
            is ProfileEvent.UpdateProfile       -> updateProfile(event.displayName)
            is ProfileEvent.SelectAvatar        -> selectAvatar(event.preset)
            is ProfileEvent.OpenAvatarPicker    -> setAvatarPicker(true)
            is ProfileEvent.DismissAvatarPicker -> setAvatarPicker(false)
            is ProfileEvent.ResetAvatar         -> selectAvatar(0)
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

                val level        = XpEvents.levelForXp(user.xp)
                val progress     = XpEvents.progressToNextLevel(user.xp)
                val xpForNext    = XpEvents.xpRequiredForLevel(level + 1)
                val xpForCurrent = XpEvents.xpRequiredForLevel(level)
                val xpIntoLevel  = user.xp - xpForCurrent

                // Preserve existing statistics fields on subsequent user-doc updates
                // (e.g. XP change after completing a task) so widgets don't reset to zero
                val existing = _uiState.value as? ProfileUiState.Ready
                _uiState.value = existing?.copy(
                    user           = user,
                    level          = level,
                    levelProgress  = progress,
                    xpIntoLevel    = xpIntoLevel,
                    xpForNextLevel = xpForNext - xpForCurrent,
                ) ?: ProfileUiState.Ready(
                    user           = user,
                    level          = level,
                    levelProgress  = progress,
                    xpIntoLevel    = xpIntoLevel,
                    xpForNextLevel = xpForNext - xpForCurrent,
                    achievements   = emptyList(),
                )

                loadStatisticsData(user.id)
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
                    userRepository.getActivityOnce(id, UserRepository.thisMonthRange)
                }
            }
            .launchIn(viewModelScope)
    }

    // ==========================================================================
    // Statistics data: one-shot fetches, loaded once when user is known
    // ==========================================================================

    private fun loadStatisticsData(uid: String) {
        if (statisticsLoaded) return
        statisticsLoaded = true
        fetchStatistics(uid)
    }

    private fun refreshPage() {
        if (userId.isEmpty()) return
        _isRefreshing.value = true
        fetchStatistics(userId)
    }

    private fun fetchStatistics(uid: String) {
        viewModelScope.launch {
            coroutineScope {
                launch {
                    val activity = userRepository.getActivityOnce(uid, UserRepository.thisMonthRange)
                    val current  = _uiState.value as? ProfileUiState.Ready ?: return@launch
                    _uiState.value = current.copy(activityData = activity)
                }
                launch {
                    val topDay  = userRepository.getTopPerformanceDay(uid)
                    val current = _uiState.value as? ProfileUiState.Ready ?: return@launch
                    _uiState.value = current.copy(topPerformanceDay = topDay)
                }
                launch {
                    val tasks        = taskRepository.getAllCompletedTasksOnce(uid)
                    val current      = _uiState.value as? ProfileUiState.Ready ?: return@launch
                    val achievements = AchievementEngine.evaluate(tasks, current.level)
                    _uiState.value = current.copy(
                        completedTasks = tasks,
                        achievements   = achievements
                    )

                    // Patch Firestore if stats are stale (one-time migration for pre-UserStats data).
                    // Only writes when the computed values exceed what's stored.
                    val computedStreak   = AchievementEngine.computeLongestStreak(tasks)
                    val computedTotal    = tasks.size
                    val computedAceCount = tasks.count { it.isAce }
                    val storedStats      = current.user.stats
                    val earnedMap        = achievements
                        .filter { it.earnedTier != null }
                        .associate { it.category.name to it.earnedTier!!.name }
                    if (computedStreak > storedStats.longestStreak || earnedMap != storedStats.earnedAchievements) {
                        userRepository.patchEarnedStats(
                            userId             = uid,
                            longestStreak      = maxOf(computedStreak, storedStats.longestStreak),
                            earnedAchievements = storedStats.earnedAchievements + earnedMap
                        )
                    }
                    // Heal inflated/deflated task counts. Bidirectional check (!=) handles both
                    // over-counts (missed undos) and under-counts (deleted completed tasks)
                    if (computedTotal != storedStats.totalTasksCompleted ||
                        computedAceCount != storedStats.aceCount) {
                        userRepository.patchStatsCount(
                            userId          = uid,
                            correctTotal    = computedTotal,
                            correctAceCount = computedAceCount
                        )
                    }
                }
            }
            _isRefreshing.value = false
        }
    }

    // ==========================================================================
    // Profile editing
    // ==========================================================================

    private fun updateProfile(displayName: String) {
        viewModelScope.launch {
            try {
                userRepository.updateProfile(userId, displayName)
            } catch (e: Exception) {
                _uiEffect.emit(ProfileUiEffect.ShowError("Failed to update profile: ${e.message}"))
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
                _uiEffect.emit(ProfileUiEffect.ShowError("Failed to update avatar: ${e.message}"))
            }
        }
    }

    // ==========================================================================
    // Friends & Invite
    // ==========================================================================

    fun generateInviteLink(): String = "monotask://invite?uid=$userId"

    fun shareInviteLink(context: Context) {
        val link   = generateInviteLink()
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
        // launchIn(viewModelScope) in startObserving(), and any in-flight statistics fetches
    }
}
