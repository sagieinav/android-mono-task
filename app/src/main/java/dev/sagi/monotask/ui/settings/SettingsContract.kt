package dev.sagi.monotask.ui.settings

import dev.sagi.monotask.data.model.Workspace

// ========== UI States ==========

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Ready(
        val hyperfocusModeEnabled: Boolean = false,
        val dueDateWeight: Float = 0.5f,
        val displayName: String = "",
        val email: String = "",
        val versionName: String = ""
    ) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

// ========== Event Callbacks ==========

sealed interface SettingsEvent {
    data class UpdateHyperfocusMode(val enabled: Boolean) : SettingsEvent
    data class UpdatePriorityWeights(val dueDateWeight: Float) : SettingsEvent
    data class UpdateDisplayName(val name: String) : SettingsEvent
    data class CreateWorkspace(val name: String) : SettingsEvent
    data class RenameWorkspace(val workspace: Workspace, val newName: String) : SettingsEvent
    data class DeleteWorkspace(val workspace: Workspace) : SettingsEvent
    object ClearArchive : SettingsEvent
    object SignOut : SettingsEvent
    object Retry : SettingsEvent
}

// ========== One-Shot UI Effects ==========

sealed interface SettingsUiEffect {
    data class ShowError(val message: String) : SettingsUiEffect
    data class ShowSuccess(val message: String) : SettingsUiEffect
}
