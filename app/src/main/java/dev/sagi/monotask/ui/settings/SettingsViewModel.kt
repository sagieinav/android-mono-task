package dev.sagi.monotask.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

// ========== UI States ==========
sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Ready(
        val hardcoreModeEnabled: Boolean = false,
        val notificationsEnabled: Boolean = true,
        val dueSoonDays: Int = 3,
        val editingWorkspace: Workspace? = null
    ) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

class SettingsViewModel(
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

//    private lateinit var userId: String
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
//        viewModelScope.launch {
//            userId = AuthUtils.awaitUid()
//            loadUserSettings()
//        }
        observeAuthState()
    }

    // ========== Initialization ==========

    private fun observeAuthState() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                // User is logged out. Emit default Ready state to unblock the UI
//                userId = null
                _uiState.value = SettingsUiState.Ready()
            } else {
                // User is logged in. Save the ID and fetch their actual settings
//                userId = user.uid
                viewModelScope.launch {
                    loadUserSettings(user.uid)
                }
            }
        }
        MonoTaskApp.instance.auth.addAuthStateListener(authStateListener!!)
    }

    private suspend fun loadUserSettings(uid: String) {
        _uiState.value = SettingsUiState.Loading // Show loading while fetching
        try {
            val user = withTimeout(8000L) {
                userRepository.getUserOnce(uid)
            }
            if (user == null) {
                _uiState.value = SettingsUiState.Error("Failed to load settings")
                return
            }
            _uiState.value = SettingsUiState.Ready(
                hardcoreModeEnabled = user.hardcoreModeEnabled,
                notificationsEnabled = user.notificationsEnabled,
                dueSoonDays = user.dueSoonDays
            )
        } catch (e: TimeoutCancellationException) {
            _uiState.value = SettingsUiState.Error("Network timeout. Settings failed to load.")
        } catch (e: Exception) {
            _uiState.value = SettingsUiState.Error("Failed to load settings: ${e.message}")
        }
    }

    // ========== Settings Operations ==========

    fun updateUserPreferences(
        hardcoreModeEnabled: Boolean? = null,
        notificationsEnabled: Boolean? = null,
        dueSoonDays: Int? = null
    ) {
        val current = _uiState.value as? SettingsUiState.Ready ?: return

        // Grab the ID straight from Firebase right when the user clicks save
        val currentUserId = MonoTaskApp.instance.auth.currentUser?.uid ?: return

        // Optimistic UI update
        _uiState.value = current.copy(
            hardcoreModeEnabled = hardcoreModeEnabled ?: current.hardcoreModeEnabled,
            notificationsEnabled = notificationsEnabled ?: current.notificationsEnabled,
            dueSoonDays = dueSoonDays ?: current.dueSoonDays
        )

        // Sync with Firestore
        viewModelScope.launch {
            try {
                userRepository.updatePreferences(
                    currentUserId,
                    hardcoreModeEnabled,
                    notificationsEnabled,
                    dueSoonDays
                )
            } catch (e: Exception) {
                // Revert to previous state on failure
                _uiState.value = current
                _errorEvent.emit("Failed to save settings: ${e.message}")
            }
        }
    }

    fun selectWorkspaceForEditing(workspace: Workspace) {
        val current = _uiState.value as? SettingsUiState.Ready ?: return
        _uiState.value = current.copy(editingWorkspace = workspace)
    }

    override fun onCleared() {
        super.onCleared()
        authStateListener?.let {
            MonoTaskApp.instance.auth.removeAuthStateListener(it)
        }
    }

}
