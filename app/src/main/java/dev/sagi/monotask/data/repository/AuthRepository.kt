package dev.sagi.monotask.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = MonoTaskApp.instance.auth

    // The currently signed-in user, or null if signed out
    val currentUser: FirebaseUser? get() = auth.currentUser

    val isSignedIn: Boolean get() = auth.currentUser != null

    // Signs in with a Google account token
    // Returns the FirebaseUser on success, null on failure
    suspend fun signInWithGoogle(account: GoogleSignInAccount): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user
    }

    // Signs the user out of Firebase Auth.
    fun signOut() {
        auth.signOut()
    }

    // Converts a FirebaseUser into a "local" User model.
    fun buildUserModel(firebaseUser: FirebaseUser): User =
        User(
            id            = firebaseUser.uid,
            displayName   = firebaseUser.displayName ?: "MonoTask User",
            email         = firebaseUser.email ?: "",
            profilePicUrl = firebaseUser.photoUrl?.toString() ?: ""
        )
}
