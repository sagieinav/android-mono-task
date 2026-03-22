package dev.sagi.monotask.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dev.sagi.monotask.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth) {

    val currentUser: FirebaseUser? get() = auth.currentUser
    val signedIn: Boolean          get() = auth.currentUser != null

    // ========== Sign In ==========

    // Exchanges a Google ID token for a Firebase session.
    // Returns the FirebaseUser on success, null on failure.
    suspend fun signInWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result     = auth.signInWithCredential(credential).await()
        return result.user
    }

    // Signs the user out of Firebase Auth
    fun signOut() {
        auth.signOut()
    }

    // ========== User Model ==========

    // Converts a FirebaseUser into a local User model
    fun buildUserModel(firebaseUser: FirebaseUser): User {
        return User(
            id          = firebaseUser.uid,
            displayName = firebaseUser.displayName ?: "MonoTask User",
            email       = firebaseUser.email ?: "",
        )
    }
}
