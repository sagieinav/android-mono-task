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
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dev.sagi.monotask.R

// ========== UI States ==========

sealed class AuthUiState {
    object Loading  : AuthUiState()
    data class SignedIn(val requiresOnboarding: Boolean) : AuthUiState() // Now holds routing data
    object SignedOut: AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
class AuthViewModel(
    private val authRepository: AuthRepository = MonoTaskApp.instance.authRepository,
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    // ========== Session Management ==========

    private fun observeAuthState() {
        MonoTaskApp.instance.auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                // Covers: cache clear, sign out, token expiry, account deletion
                _uiState.value = AuthUiState.SignedOut
            } else {
                viewModelScope.launch {
                    val storedUser = userRepository.getUserOnce(user.uid)
                    val requiresOnboarding = storedUser?.onboarded == false
                    _uiState.value = AuthUiState.SignedIn(requiresOnboarding)
                }
            }
        }
    }

    // ========== Sign In Logic ==========
    fun onGoogleSignInResult(account: GoogleSignInAccount?) {
        if (account == null) {
            _uiState.value = AuthUiState.Error("Sign-in cancelled")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val firebaseUser = authRepository.signInWithGoogle(account) ?: run {
                    _uiState.value = AuthUiState.Error("Authentication failed")
                    return@launch
                }

                val userModel = authRepository.buildUserModel(firebaseUser)
                userRepository.createUserIfNotExists(userModel)

                // Fetch the actual stored user record
                val storedUser = userRepository.getUserOnce(firebaseUser.uid) ?: run {
                    _uiState.value = AuthUiState.Error("Failed to load user profile")
                    return@launch
                }

                if (!storedUser.onboarded) {
                    workspaceRepository.createDefaultWorkspaces(storedUser.id)
                }

                // Pass the flag to the UI state
                _uiState.value = AuthUiState.SignedIn(requiresOnboarding = !storedUser.onboarded)

            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown AUTHENTICATION error")
            }
        }
    }

    fun completeOnboarding() {
        val userId = MonoTaskApp.instance.auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                userRepository.completeOnboarding(userId)
            } catch (e: Exception) {
                // TODO log error
            }
        }
    }

    fun getGoogleSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }

    fun signOut() {
        authRepository.signOut()
//        _uiState.value = AuthUiState.SignedOut
    }
}

