package dev.sagi.monotask.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.repository.AuthRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ========== UI States ==========

sealed class AuthUiState {
    object Loading : AuthUiState()
    object SignedOut : AuthUiState()
    data class SignedIn(val requiresOnboarding: Boolean) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

data class GoogleSignInData(val idToken: String, val profilePicUrl: String?)

// ========== ViewModel ==========

class AuthViewModel(
    private val authRepository: AuthRepository = MonoTaskApp.instance.authRepository,
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var isSigningIn = false

    init {
        observeAuthState()
    }

    // ========== Auth State Observation ==========

    private fun observeAuthState() {
        MonoTaskApp.instance.auth.addAuthStateListener { firebaseAuth ->
            if (isSigningIn) return@addAuthStateListener
            val user = firebaseAuth.currentUser
            if (user == null) {
                _uiState.value = AuthUiState.SignedOut
            } else {
                viewModelScope.launch {
                    try {
                        val storedUser = userRepository.getUserOnce(user.uid)
                        _uiState.value = AuthUiState.SignedIn(
                            requiresOnboarding = storedUser?.onboarded == false
                        )
                    } catch (e: Exception) {
                        _uiState.value = AuthUiState.Error("Failed to check user status: ${e.message}")
                    }
                }
            }
        }
    }

    // ========== Sign In ==========

    // Launches the Credential Manager Google Sign-In dialog.
    // Returns a GoogleSignInData with the id token and profile pic URL, or null on failure/cancellation.
    suspend fun launchGoogleSignIn(context: Context): String? {
        try {
            val credentialManager = CredentialManager.create(context)
            val option = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val result     = credentialManager.getCredential(context, request)
            val credential = result.credential

            // Credential Manager wraps GoogleIdTokenCredential inside CustomCredential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                return googleCred.idToken
            } else {
                Log.e("Auth", "Unexpected credential type: ${credential.type}")
                return null
            }
        } catch (e: GetCredentialException) {
            Log.e("Auth", "GetCredentialException: ${e.message}")
            return null
        } catch (e: Exception) {
            Log.e("Auth", "Sign-in failed: ${e.message}")
            return null
        }
    }

    fun onGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            isSigningIn = true
            _uiState.value = AuthUiState.Loading
            try {
                val firebaseUser = authRepository.signInWithGoogle(idToken) ?: run {
                    _uiState.value = AuthUiState.Error("Authentication failed")
                    return@launch
                }

                val userModel = authRepository.buildUserModel(firebaseUser)
                userRepository.createUserIfNotExists(userModel)
//                userRepository.syncGoogleProfile(
//                    userId        = firebaseUser.uid,
//                    displayName   = userModel.displayName,
//                    profilePicUrl = userModel.profilePicUrl
//                )

                val storedUser = userRepository.getUserOnce(firebaseUser.uid) ?: run {
                    _uiState.value = AuthUiState.Error("Failed to load user profile")
                    return@launch
                }

                if (!storedUser.onboarded) {
                    workspaceRepository.createDefaultWorkspaces(storedUser.id)
                }

                _uiState.value = AuthUiState.SignedIn(requiresOnboarding = !storedUser.onboarded)

            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown authentication error")
            } finally {
                isSigningIn = false
            }
        }
    }

    // ========== Other Actions ==========

    fun completeOnboarding() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                userRepository.completeOnboarding(userId)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Failed to complete onboarding: ${e.message}")
            }
        }
    }

    // Single source of truth for sign-out.
    // SettingsScreen calls this through a callback, not its own logout path.
    fun signOut() {
        authRepository.signOut()
    }
}
