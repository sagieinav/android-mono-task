package dev.sagi.monotask.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
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
    private val userRepository      : UserRepository      = MonoTaskApp.instance.userRepository,
    private val workspaceRepository : WorkspaceRepository = MonoTaskApp.instance.workspaceRepository,
    private val taskRepository      : TaskRepository      = MonoTaskApp.instance.taskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ProfileUiEffect>()
    val uiEffect: SharedFlow<ProfileUiEffect> = _uiEffect.asSharedFlow()

    private lateinit var userId: String
    private var observing = false   // safe: only touched on Main dispatcher

    init {
        viewModelScope.launch { userId = AuthUtils.awaitUid() }
    }

    // ==========================================================================
    // Event Dispatcher
    // ==========================================================================

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.SearchUsers   -> searchUsers(event.query)
            is ProfileEvent.AddFriend     -> addFriend(event.friendId)
            is ProfileEvent.UpdateProfile -> updateProfile(event.displayName, event.profilePicUrl)
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

                _uiState.value = ProfileUiState.Ready(
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
    // Statistics data
    //
    // Workspaces are a live flow (subscribe once).
    // Activity + completed tasks are one-shot fetches (fresh on each profile open).
    // Both update the Ready state via copy() — safe since we're on the same
    // coroutine scope with no concurrent writes.
    // ==========================================================================

    private fun loadStatisticsData(uid: String) {
        // Live workspace list: updates state whenever workspaces change
        workspaceRepository.getWorkspaces(uid)
            .onEach { workspaces ->
                val current = _uiState.value as? ProfileUiState.Ready ?: return@onEach
                _uiState.value = current.copy(workspaces = workspaces)
            }
            .launchIn(viewModelScope)

        userRepository.getActivityForCurrentMonth(uid)
            .onEach { monthActivity ->
                val current = _uiState.value as? ProfileUiState.Ready ?: return@onEach
                _uiState.value = current.copy(monthActivityData = monthActivity)
            }
            .launchIn(viewModelScope)

        // One-shot fetches for activity + completed tasks
        viewModelScope.launch {
            try {
                val activity = userRepository.getActivityLast7Days(uid)
                val tasks    = taskRepository.getAllCompletedTasksOnce(uid)
                val current  = _uiState.value as? ProfileUiState.Ready ?: return@launch
                _uiState.value = current.copy(
                    activityData   = activity,
                    completedTasks = tasks
                )
            } catch (e: Exception) {
                _uiEffect.emit(ProfileUiEffect.ShowError("Failed to load statistics: ${e.message}"))
            }
        }
    }

    // ==========================================================================
    // Profile editing
    // ==========================================================================

    private fun updateProfile(displayName: String, profilePicUrl: String) {
        viewModelScope.launch {
            try {
                userRepository.updateProfile(userId, displayName, profilePicUrl)
            } catch (e: Exception) {
                _uiEffect.emit(ProfileUiEffect.ShowError("Failed to update profile: ${e.message}"))
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
