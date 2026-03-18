package dev.sagi.monotask.ui.profile

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.domain.util.XpEvents
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository : UserRepository = MonoTaskApp.instance.userRepository,
    private val taskRepository : TaskRepository = MonoTaskApp.instance.taskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ProfileUiEffect>()
    val uiEffect: SharedFlow<ProfileUiEffect> = _uiEffect.asSharedFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private lateinit var userId: String

    private var observing          = false  // safe: only touched on Main dispatcher
    private var statisticsLoaded   = false  // guard: loadStatisticsData runs once

    init {
        viewModelScope.launch { userId = AuthUtils.awaitUid() }
    }

    // ==========================================================================
    // Event Dispatcher
    // ==========================================================================

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.RefreshPage   -> refreshPage()
            is ProfileEvent.SearchUsers   -> searchUsers(event.query)
            is ProfileEvent.AddFriend     -> addFriend(event.friendId)
            is ProfileEvent.UpdateProfile -> updateProfile(event.displayName)
            is ProfileEvent.SelectAvatar  -> selectAvatar(event.preset)
            is ProfileEvent.OpenAvatarPicker  -> setAvatarPicker(true)
            is ProfileEvent.DismissAvatarPicker -> setAvatarPicker(false)
            is ProfileEvent.ResetAvatar   -> selectAvatar(0)
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
                )
                    ?: ProfileUiState.Ready(
                        user           = user,
                        level          = level,
                        levelProgress  = progress,
                        xpIntoLevel    = xpIntoLevel,
                        xpForNextLevel = xpForNext - xpForCurrent,
                        badges         = emptyList(),
                    )

                // Load statistics data after user is known, runs in parallel
                loadStatisticsData(user.id)
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
        if (!::userId.isInitialized) return
        _isRefreshing.value = true
        fetchStatistics(userId)
    }

    private fun fetchStatistics(uid: String) {
        viewModelScope.launch {
            // All fetches run concurrently
            launch {
                val activity = userRepository.getActivityOnce(uid, UserRepository.thisMonthRange)
                val current = _uiState.value as? ProfileUiState.Ready ?: return@launch
                _uiState.value = current.copy(activityData = activity)
            }
            launch {
                val topDay = userRepository.getTopPerformanceDay(uid)
                val current = _uiState.value as? ProfileUiState.Ready ?: return@launch
                _uiState.value = current.copy(topPerformanceDay = topDay)
            }
            launch {
                val tasks = taskRepository.getAllCompletedTasksOnce(uid)
                val current = _uiState.value as? ProfileUiState.Ready ?: return@launch
                _uiState.value = current.copy(completedTasks = tasks)
            }

            // Wait for all child coroutines to complete, then clear refreshing
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
                setAvatarPicker(false)   // auto-dismiss after selection
            } catch (e: Exception) {
                _uiEffect.emit(ProfileUiEffect.ShowError("Failed to update avatar: ${e.message}"))
            }
        }
    }

    // ==========================================================================
    // Friends
    // ==========================================================================

    private fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                _isSearching.value = true
                val results = userRepository.searchUsers(query)
                    .filter { it.id != userId }
                _searchResults.value = results
            } catch (e: Exception) {
                _uiEffect.emit(ProfileUiEffect.ShowError("Search failed: ${e.message}"))
            } finally {
                _isSearching.value = false
            }
        }
    }

    private fun addFriend(friendId: String) {
        viewModelScope.launch {
            try {
                userRepository.addFriend(userId, friendId)
                _searchResults.value = emptyList()
            } catch (e: Exception) {
                _uiEffect.emit(ProfileUiEffect.ShowError("Failed to add friend: ${e.message}"))
            }
        }
    }
}
