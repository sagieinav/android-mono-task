package dev.sagi.monotask.util

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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
                    if (uid != null) {
                        auth.removeAuthStateListener(this)
                        if (cont.isActive) cont.resume(uid)
                    }
                }
            }
            auth.addAuthStateListener(listener)
            cont.invokeOnCancellation { auth.removeAuthStateListener(listener) }
        }
    }

    fun currentUidOrNull(): String? = auth.currentUser?.uid
}
