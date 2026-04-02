package dev.sagi.monotask.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): User? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result     = auth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: return null
        return User(
            id          = firebaseUser.uid,
            displayName = firebaseUser.displayName ?: "MonoTask User",
            email       = firebaseUser.email ?: "",
        )
    }

    override fun signOut() {
        auth.signOut()
    }
}
