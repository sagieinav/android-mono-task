package dev.sagi.monotask.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
        observeAuthState()
    }

    // ========== Initialization ==========

    private fun observeAuthState() {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                _uiState.value = SettingsUiState.Ready()
            } else {
                viewModelScope.launch {
                    loadUserSettings(user.uid)
                }
            }
        }
        authStateListener = listener
        auth.addAuthStateListener(listener)
    }

    private suspend fun loadUserSettings(uid: String) {
        _uiState.value = SettingsUiState.Loading
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
        val currentUserId = auth.currentUser?.uid ?: return

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
        authStateListener?.let { auth.removeAuthStateListener(it) }
    }
}
