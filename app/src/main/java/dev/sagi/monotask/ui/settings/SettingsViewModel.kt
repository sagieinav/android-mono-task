package dev.sagi.monotask.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import kotlinx.coroutines.Job
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
        val dueDateWeight: Float = 0.5f,
        val importanceWeight: Float = 0.5f,
        val displayName: String = "",
        val email: String = ""
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

    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
    val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var workspacesJob: Job? = null

    init {
        observeAuthState()
    }

    // ========== Initialization ==========

    private fun observeAuthState() {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                _uiState.value = SettingsUiState.Ready()
                workspacesJob?.cancel()
                _workspaces.value = emptyList()
            } else {
                viewModelScope.launch {
                    loadUserSettings(user.uid)
                }
                workspacesJob?.cancel()
                workspacesJob = viewModelScope.launch {
                    workspaceRepository.getWorkspaces(user.uid).collect { _workspaces.value = it }
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
                dueSoonDays = user.dueSoonDays,
                dueDateWeight = user.dueDateWeight,
                importanceWeight = user.importanceWeight,
                displayName = user.displayName,
                email = user.email
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
                _uiState.value = current
                _errorEvent.emit("Failed to save settings: ${e.message}")
            }
        }
    }

    fun updatePriorityWeights(dueDateWeight: Float, importanceWeight: Float) {
        val current = _uiState.value as? SettingsUiState.Ready ?: return
        val currentUserId = auth.currentUser?.uid ?: return

        // Optimistic UI update
        _uiState.value = current.copy(
            dueDateWeight = dueDateWeight,
            importanceWeight = importanceWeight
        )

        viewModelScope.launch {
            try {
                userRepository.updatePriorityWeights(currentUserId, dueDateWeight, importanceWeight)
            } catch (e: Exception) {
                _uiState.value = current
                _errorEvent.emit("Failed to save priority weights: ${e.message}")
            }
        }
    }

    fun updateDisplayName(name: String) {
        val current = _uiState.value as? SettingsUiState.Ready ?: return
        val uid = auth.currentUser?.uid ?: return
        _uiState.value = current.copy(displayName = name)
        viewModelScope.launch {
            try {
                userRepository.updateProfile(uid, name)
            } catch (e: Exception) {
                _uiState.value = current
                _errorEvent.emit("Failed to update name: ${e.message}")
            }
        }
    }

    // ========== Workspace Operations ==========

    fun createWorkspace(name: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                workspaceRepository.createWorkspace(uid, name)
            } catch (e: Exception) {
                _errorEvent.emit("Failed to create workspace: ${e.message}")
            }
        }
    }

    fun renameWorkspace(workspace: Workspace, newName: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                workspaceRepository.renameWorkspace(uid, workspace.id, newName)
            } catch (e: Exception) {
                _errorEvent.emit("Failed to rename workspace: ${e.message}")
            }
        }
    }

    fun deleteWorkspace(workspace: Workspace) {
        if (_workspaces.value.size <= 1) {
            viewModelScope.launch { _errorEvent.emit("You must keep at least one workspace.") }
            return
        }
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                workspaceRepository.deleteWorkspace(uid, workspace.id)
            } catch (e: Exception) {
                _errorEvent.emit("Failed to delete workspace: ${e.message}")
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        authStateListener?.let { auth.removeAuthStateListener(it) }
        workspacesJob?.cancel()
    }
}
