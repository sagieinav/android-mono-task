package dev.sagi.monotask.ui.auth

// ========== UI States ==========

sealed class AuthUiState {
    object Loading : AuthUiState()
    object SignedOut : AuthUiState()
    data class SignedIn(val requiresOnboarding: Boolean) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

// ========== Event Callbacks ==========

sealed interface AuthEvent {
    data class OnGoogleSignInResult(val idToken: String) : AuthEvent
    object CompleteOnboarding : AuthEvent
    object SignOut : AuthEvent
}

// ========== One-Shot UI Effects ==========

sealed interface AuthUiEffect {
    object NavigateToMain : AuthUiEffect
    object NavigateToOnboarding : AuthUiEffect
    data class ShowError(val message: String) : AuthUiEffect
}
