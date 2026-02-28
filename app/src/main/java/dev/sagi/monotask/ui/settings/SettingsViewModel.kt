package dev.sagi.monotask.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import dev.sagi.monotask.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // Required for atomic updates
import kotlinx.coroutines.launch

// ========== UI State Data Class ==========
data class SettingsUiState(
    val hardcoreModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val dueSoonDays: Int = 3,
    val loading: Boolean = false,
    val editingWorkspace: Workspace? = null
)

class SettingsViewModel(
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository,
    private val authRepository: AuthRepository = MonoTaskApp.instance.authRepository,
    private val userId: String = MonoTaskApp.instance.auth.currentUser?.uid ?: ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Only load user settings if user is logged in
        if (userId.isNotEmpty()) {
            loadUserSettings()
        }
    }

// ========== Initialization ==========

    private fun loadUserSettings() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            val user = userRepository.getUserOnce(userId)
            user?.let {
                _uiState.update { currentState ->
                    currentState.copy(
                        hardcoreModeEnabled = it.hardcoreModeEnabled,
                        notificationsEnabled = it.notificationsEnabled,
                        dueSoonDays = it.dueSoonDays,
                        loading = false
                    )
                }
            }
        }
    }

// ========== Settings Operations ==========

    fun updateUserPreferences(
        hardcoreModeEnabled: Boolean? = null,
        notificationsEnabled: Boolean? = null,
        dueSoonDays: Int? = null
    ) {
        // Update local UI state
        _uiState.update { currentState ->
            currentState.copy(
                hardcoreModeEnabled = hardcoreModeEnabled ?: currentState.hardcoreModeEnabled,
                notificationsEnabled = notificationsEnabled ?: currentState.notificationsEnabled,
                dueSoonDays = dueSoonDays ?: currentState.dueSoonDays
            )
        }

        // Sync with Firestore
        viewModelScope.launch {
            userRepository.updatePreferences(
                userId,
                hardcoreModeEnabled,
                notificationsEnabled,
                dueSoonDays
            )
        }
    }

    fun selectWorkspaceForEditing(workspace: Workspace) {
        _uiState.update { currentState ->
            currentState.copy(editingWorkspace = workspace)
        }
    }

// ========== Authentication ==========

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}