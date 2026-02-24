package dev.sagi.monotask.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.repository.AuthRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = MonoTaskApp.instance.authRepository,
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository
) : ViewModel() {

    // Private, writeable state-flow (only used by the ViewModel):
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    // Public, immutable state-flow (used/observed by the UI):
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Check if user is already signed in when ViewModel is created
        // This handles the "returning user" case: skip auth screen entirely
        if (authRepository.isSignedIn) {
            _uiState.value = AuthUiState.SignedIn
        } else {
            _uiState.value = AuthUiState.SignedOut
        }
    }

    // Called from the UI after Google Sign-In SDK returns a result
    // Handles both new users (creates Firestore doc) and returning users
    fun onGoogleSignInResult(account: GoogleSignInAccount?) {
        if (account == null) {
            _uiState.value = AuthUiState.Error("Sign-in cancelled")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val firebaseUser = authRepository.signInWithGoogle(account)
                if (firebaseUser == null) {
                    _uiState.value = AuthUiState.Error("Authentication failed")
                    return@launch //
                }

                // Build the User model from the Firebase result
                val user = authRepository.buildUserModel(firebaseUser)

                userRepository.createUserIfNotExists(user)

                // Only create default workspaces for brand new users
                if (!user.isOnboarded) {
                    workspaceRepository.createDefaultWorkspaces(user.id)
                }

                _uiState.value = AuthUiState.SignedIn

            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown AUTHENTICATION error")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState.SignedOut
    }
}

// Sealed class: every possible state of the auth screen
// Only one state can be active at a time
sealed class AuthUiState {
    object Loading  : AuthUiState()  // spinner showing
    object SignedIn : AuthUiState()  // navigate to main app
    object SignedOut: AuthUiState()  // show sign-in button
    data class Error(val message: String) : AuthUiState()  // show error
}
