package dev.sagi.monotask.data.repository

import com.google.firebase.firestore.snapshots
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = MonoTaskApp.instance.db

    private fun userDoc(userId: String) =
        db.collection("users").document(userId)

    // Real-time stream of the current user's profile
    fun getUserStream(userId: String): Flow<User?> =
        userDoc(userId)
            .snapshots()
            .map { it.toObject(User::class.java)?.copy(id = it.id) }

    // Creates the user document on first login
    // Called once after Google Sign-In if the doc doesn't exist yet
    suspend fun createUserIfNotExists(user: User) {
        val doc = userDoc(user.id).get().await()
        if (!doc.exists()) {
            userDoc(user.id).set(user).await()
        }
    }

    // Adds XP and recalculates level
    suspend fun addXp(userId: String, amount: Int, currentXp: Int, currentLevel: Int) {
        val newXp = (currentXp + amount).coerceAtLeast(0) // XP never goes below 0
        val newLevel = XpEvents.levelForXp(newXp)
        userDoc(userId).update(
            mapOf(
                "xp"    to newXp,
                "level" to newLevel
            )
        ).await()
    }

    // Updates display name or profile picture URL.
    suspend fun updateProfile(userId: String, displayName: String, profilePicUrl: String) {
        userDoc(userId).update(
            mapOf(
                "displayName"   to displayName,
                "profilePicUrl" to profilePicUrl
            )
        ).await()
    }

    // Marks onboarding as complete. Called on the last onboarding page
    suspend fun completeOnboarding(userId: String) {
        userDoc(userId).update("isOnboarded", true).await()
    }

    // Adds a friend by storing their userId in the friends list.
    suspend fun addFriend(userId: String, friendId: String) {
        // `arrayUnion` prevents duplicates
        userDoc(userId).update(
            "friends", com.google.firebase.firestore.FieldValue.arrayUnion(friendId)
        ).await()
    }

    // Fetches a user profile once (not real-time)
    // Used for friend search results and read-only friend profile view
    suspend fun getUserOnce(userId: String): User? {
        val doc = userDoc(userId).get().await()
        return doc.toObject(User::class.java)?.copy(id = doc.id)
    }

    // Searches users by display name (for the Add Friend screen)
    // Firestore prefix search: finds names starting with `[query]`
    suspend fun searchUsers(query: String): List<User> {
        val result = db.collection("users")
            .whereGreaterThanOrEqualTo("displayName", query)
            .whereLessThanOrEqualTo("displayName", query + "\uF8FF")
            .get()
            .await()
        return result.documents.mapNotNull {
            it.toObject(User::class.java)?.copy(id = it.id)
        }
    }
}
