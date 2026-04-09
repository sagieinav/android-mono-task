package dev.sagi.monotask.ui.settings

import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.ui.common.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.domain.repository.UserRepository
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val auth: FirebaseAuth
) : BaseViewModel<SettingsUiState, SettingsEvent, SettingsUiEffect>() {

    override val initialState: SettingsUiState = SettingsUiState.Loading

    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
    val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var workspacesJob: Job? = null

    init {
        observeAuthState()
    }

    // ========== Event Dispatcher ==========

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.UpdateHyperfocusMode -> updateHyperfocusMode(event.enabled)
            is SettingsEvent.UpdatePriorityWeights -> updatePriorityWeights(event.dueDateWeight)
            is SettingsEvent.UpdateDisplayName -> updateDisplayName(event.name)
            is SettingsEvent.CreateWorkspace -> createWorkspace(event.name)
            is SettingsEvent.RenameWorkspace -> renameWorkspace(event.workspace, event.newName)
            is SettingsEvent.DeleteWorkspace -> deleteWorkspace(event.workspace)
            is SettingsEvent.SignOut -> auth.signOut()
        }
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
                val uid = AuthUtils.currentUidOrNull() ?: user.uid
                viewModelScope.launch { loadUserSettings(uid) }
                workspacesJob?.cancel()
                workspacesJob = viewModelScope.launch {
                    workspaceRepository.getWorkspaces(uid).collect { _workspaces.value = it }
                }
            }
        }
        authStateListener = listener
        auth.addAuthStateListener(listener)
    }

    private suspend fun loadUserSettings(uid: String) {
        _uiState.value = SettingsUiState.Loading
        try {
            val user = withTimeout(8000L) { userRepository.getUserOnce(uid) }
            if (user == null) {
                _uiState.value = SettingsUiState.Error("Failed to load settings")
                return
            }
            _uiState.value = SettingsUiState.Ready(
                hyperfocusModeEnabled = user.hyperfocusModeEnabled,
                dueDateWeight         = user.dueDateWeight,
                displayName           = user.displayName,
                email                 = user.email
            )
        } catch (e: TimeoutCancellationException) {
            _uiState.value = SettingsUiState.Error("Network timeout. Settings failed to load.")
        } catch (e: Exception) {
            _uiState.value = SettingsUiState.Error("Failed to load settings: ${e.message}")
        }
    }

    // ========== Settings Operations ==========

    private fun updateHyperfocusMode(enabled: Boolean) {
        val current = _uiState.value as? SettingsUiState.Ready ?: return
        val uid     = AuthUtils.currentUidOrNull() ?: return
        _uiState.value = current.copy(hyperfocusModeEnabled = enabled)
        viewModelScope.launch {
            try {
                userRepository.updateHyperfocusMode(uid, enabled)
            } catch (e: Exception) {
                _uiState.value = current
                sendEffect(SettingsUiEffect.ShowError("Failed to save settings: ${e.message}"))
            }
        }
    }

    private fun updatePriorityWeights(dueDateWeight: Float) {
        val current = _uiState.value as? SettingsUiState.Ready ?: return
        val uid     = AuthUtils.currentUidOrNull() ?: return
        _uiState.value = current.copy(dueDateWeight = dueDateWeight)
        viewModelScope.launch {
            try {
                userRepository.updatePriorityWeights(uid, dueDateWeight)
            } catch (e: Exception) {
                _uiState.value = current
                sendEffect(SettingsUiEffect.ShowError("Failed to save priority weights: ${e.message}"))
            }
        }
    }

    private fun updateDisplayName(name: String) {
        val current = _uiState.value as? SettingsUiState.Ready ?: return
        val uid     = AuthUtils.currentUidOrNull() ?: return
        _uiState.value = current.copy(displayName = name)
        viewModelScope.launch {
            try {
                userRepository.updateProfile(uid, name)
            } catch (e: Exception) {
                _uiState.value = current
                sendEffect(SettingsUiEffect.ShowError("Failed to update name: ${e.message}"))
            }
        }
    }

    // ========== Workspace Operations ==========

    private fun createWorkspace(name: String) {
        val uid = AuthUtils.currentUidOrNull() ?: return
        viewModelScope.launch {
            try {
                workspaceRepository.createWorkspace(uid, name)
            } catch (e: Exception) {
                sendEffect(SettingsUiEffect.ShowError("Failed to create workspace: ${e.message}"))
            }
        }
    }

    private fun renameWorkspace(workspace: Workspace, newName: String) {
        val uid = AuthUtils.currentUidOrNull() ?: return
        viewModelScope.launch {
            try {
                workspaceRepository.renameWorkspace(uid, workspace.id, newName)
            } catch (e: Exception) {
                sendEffect(SettingsUiEffect.ShowError("Failed to rename workspace: ${e.message}"))
            }
        }
    }

    private fun deleteWorkspace(workspace: Workspace) {
        if (_workspaces.value.size <= 1) {
            viewModelScope.launch { sendEffect(SettingsUiEffect.ShowError("You must keep at least one workspace.")) }
            return
        }
        val uid = AuthUtils.currentUidOrNull() ?: return
        viewModelScope.launch {
            try {
                workspaceRepository.deleteWorkspace(uid, workspace.id)
            } catch (e: Exception) {
                sendEffect(SettingsUiEffect.ShowError("Failed to delete workspace: ${e.message}"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        authStateListener?.let { auth.removeAuthStateListener(it) }
        workspacesJob?.cancel()
    }
}
