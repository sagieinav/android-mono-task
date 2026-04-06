package dev.sagi.monotask.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.ui.common.BaseViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.R
import dev.sagi.monotask.domain.repository.AuthRepository
import dev.sagi.monotask.domain.repository.UserRepository
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

// ========== ViewModel ==========

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val auth: FirebaseAuth
) : BaseViewModel<AuthUiState, AuthEvent, AuthUiEffect>() {

    override val initialState: AuthUiState = AuthUiState.Loading
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var isSigningIn = false  // safe: only read/written on Main dispatcher

    init {
        observeAuthState()
    }

    // ========== Event Dispatcher ==========

    override fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.OnGoogleSignInResult -> onGoogleSignInResult(event.idToken)
            is AuthEvent.CompleteOnboarding -> completeOnboarding()
            is AuthEvent.SignOut -> signOut()
        }
    }

    // ========== Auth State Observation ==========
    private fun observeAuthState() {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (isSigningIn) return@AuthStateListener

            val user = firebaseAuth.currentUser
            if (user == null) {
                _uiState.value = AuthUiState.SignedOut
            } else {
                viewModelScope.launch {
                    try {
                        // Enforce an 8-second timeout for the network/cache fetch
                        val storedUser = withTimeout(8000L) {
                            userRepository.getUserOnce(user.uid)
                        }
                        _uiState.value = AuthUiState.SignedIn(
                            requiresOnboarding = storedUser?.onboarded == false
                        )
                    } catch (e: TimeoutCancellationException) {
                        _uiState.value = AuthUiState.Error("Network timeout. Please check your connection.")
                    } catch (e: Exception) {
                        _uiState.value = AuthUiState.Error("Failed to check user status: ${e.message}")
                    }
                }
            }
        }
        authStateListener = listener
        auth.addAuthStateListener(listener)
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

            val result = credentialManager.getCredential(context, request)
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
                val userModel = authRepository.signInWithGoogle(idToken) ?: run {
                    _uiState.value = AuthUiState.Error("Authentication failed")
                    return@launch
                }

                userRepository.createUserIfNotExists(userModel)

                val storedUser = userRepository.getUserOnce(userModel.id) ?: run {
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
        val userId = AuthUtils.currentUidOrNull() ?: return
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

    override fun onCleared() {
        super.onCleared()
        authStateListener?.let { auth.removeAuthStateListener(it) }
    }
}
