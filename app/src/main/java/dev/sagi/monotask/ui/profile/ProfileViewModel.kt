package dev.sagi.monotask.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Badge
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val userId: String = MonoTaskApp.instance.auth.currentUser?.uid ?: ""
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Friend search results (separate from main profile state)
    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        // Only observe if user is logged in
        if (userId.isNotEmpty()) {
            observeProfile()
        }
    }

    // Observes the user profile in real-time
    // Every Firestore change (XP gain, level up...) automatically flows through to the UI
    private fun observeProfile() {
        viewModelScope.launch {
            userRepository.getUserStream(userId).collect { user ->
                if (user == null) {
                    _uiState.value = ProfileUiState.Error("User not found")
                    return@collect
                }

                // Derive level data from XP using the formula
                val level           = XpEvents.levelForXp(user.xp)
                val progress        = XpEvents.progressToNextLevel(user.xp)
                val xpForNext       = XpEvents.xpRequiredForLevel(level + 1)
                val xpForCurrent    = XpEvents.xpRequiredForLevel(level)
                val xpIntoLevel     = user.xp - xpForCurrent

                _uiState.value = ProfileUiState.Ready(
                    user            = user,
                    level           = level,
                    levelProgress   = progress,
                    xpIntoLevel     = xpIntoLevel,
                    xpForNextLevel  = xpForNext - xpForCurrent,
                    // Badges and activity hardcoded for now — wired in a later task
                    badges          = emptyList(),
                    activityData    = emptyList()
                )
            }
        }
    }

    // ========== Profile Editing ==========

    fun updateProfile(displayName: String, profilePicUrl: String) {
        viewModelScope.launch {
            userRepository.updateProfile(userId, displayName, profilePicUrl)
        }
    }

    // ========== Friends ==========

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val results = userRepository.searchUsers(query)
                .filter { it.id != userId } // don't show yourself in results
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun addFriend(friendId: String) {
        viewModelScope.launch {
            userRepository.addFriend(userId, friendId)
            // Clear search after adding
            _searchResults.value = emptyList()
        }
    }
}

// ========== UI States ==========
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    data class Ready(
        val user           : User,
        val level          : Int,
        val levelProgress  : Float,   // 0.0 - 1.0 for the progress bar
        val xpIntoLevel    : Int,
        val xpForNextLevel : Int,
        val badges         : List<Badge>,
        val activityData   : List<DailyActivity>
    ) : ProfileUiState()
}
