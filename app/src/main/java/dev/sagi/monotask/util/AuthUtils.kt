package dev.sagi.monotask.util

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// This singleton object is used instead of relying on Hilt injection, to eliminate the need of injecting AuthRepository into EVERY ViewModel

object AuthUtils {
    private val auth get() = FirebaseAuth.getInstance()

    // Suspends until a Firebase UID is available
    suspend fun awaitUid(): String {
        //  If already signed in, return immediately
        auth.currentUser?.uid?.let { return it }

        return suspendCancellableCoroutine { cont ->
            val listener = object : FirebaseAuth.AuthStateListener {
                override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
                    val uid = firebaseAuth.currentUser?.uid
                    uid?.let {
                        auth.removeAuthStateListener(this)
                        if (cont.isActive) cont.resume(it)
                    }
                }
            }
            auth.addAuthStateListener(listener)
            cont.invokeOnCancellation { auth.removeAuthStateListener(listener) } // Prevent memory leaks upon mid-operation cancellation
        }
    }

    fun currentUidOrNull(): String? = auth.currentUser?.uid
}