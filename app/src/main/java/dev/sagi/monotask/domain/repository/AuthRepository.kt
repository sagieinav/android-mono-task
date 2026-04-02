package dev.sagi.monotask.domain.repository

import dev.sagi.monotask.data.model.User

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): User?
    fun signOut()
}
