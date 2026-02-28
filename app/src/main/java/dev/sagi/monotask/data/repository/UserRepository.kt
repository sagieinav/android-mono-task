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

    // Updates the user's preferences (app settings)
    suspend fun updatePreferences(
        userId: String,
        hardcoreModeEnabled: Boolean? = null,
        notificationsEnabled: Boolean? = null,
        dueSoonDays: Int? = null
    ) {
        val updates = mutableMapOf<String, Any>()

        // Only add to the map if the value is NOT null (only selected parameters)
        hardcoreModeEnabled?.let { updates["hardcoreModeEnabled"] = it }
        notificationsEnabled?.let { updates["notificationsEnabled"] = it }
        dueSoonDays?.let { updates["dueSoonDays"] = it }

        if (updates.isNotEmpty()) {
            userDoc(userId).update(updates).await()
        }
    }

    // Updates 'Hardcore Mode' State
    suspend fun updateHardcoreMode(userId: String, enabled: Boolean) {
        userDoc(userId).update(
            "hardcoreModeEnabled", enabled
        ).await()
    }

    // Marks onboarding as complete. Called on the last onboarding page
    suspend fun completeOnboarding(userId: String) {
        userDoc(userId).update("onboarded", true).await()
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

    // Logs XP and task count for today. Used by the Profile activity chart
    suspend fun logDailyActivity(userId: String, xpEarned: Int, tasksCompleted: Int) {
        val today = java.time.LocalDate.now().toEpochDay()
        val docRef = userDoc(userId).collection("activity").document(today.toString())

        // Use set with merge: safe to call multiple times per day
        docRef.set(
            mapOf(
                "dateEpochDay"   to today,
                "xpEarned"       to com.google.firebase.firestore.FieldValue.increment(xpEarned.toLong()),
                "tasksCompleted" to com.google.firebase.firestore.FieldValue.increment(1L)
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }
}
