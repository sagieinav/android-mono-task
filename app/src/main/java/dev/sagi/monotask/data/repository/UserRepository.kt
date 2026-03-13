package dev.sagi.monotask.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.util.XpEvents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = MonoTaskApp.instance.db

    private fun userDoc(userId: String) =
        db.collection("users").document(userId)

    // ========== Reads ==========

    // Real-time stream of the current user's profile
    fun getUserStream(userId: String): Flow<User?> =
        userDoc(userId)
            .snapshots()
            .map { it.toObject(User::class.java)?.copy(id = it.id) }

    // One-shot fetch. Used for friend search and read-only profile views
    suspend fun getUserOnce(userId: String): User? {
        val doc = userDoc(userId).get().await()
        return doc.toObject(User::class.java)?.copy(id = doc.id)
    }

    // ========== User Lifecycle ==========

    // Creates the user document on first login. Safe to call on every login (no-op if already exists).
    suspend fun createUserIfNotExists(user: User) {
        val doc = userDoc(user.id).get().await()
        if (!doc.exists()) userDoc(user.id).set(user).await()
    }

    // Updates display name and profile picture from within the app (profile settings)
    suspend fun updateProfile(userId: String, displayName: String, profilePicUrl: String) {
        userDoc(userId).update(mapOf(
            "displayName"   to displayName,
            "profilePicUrl" to profilePicUrl
        )).await()
    }

    // Marks onboarding as complete. Called on the last onboarding page.
    suspend fun completeOnboarding(userId: String) {
        userDoc(userId).update("onboarded", true).await()
    }

    // ========== XP ==========

    // Adds XP and recalculates level
    suspend fun addXp(userId: String, amount: Int, currentXp: Int, currentLevel: Int) {
        val newXp    = (currentXp + amount).coerceAtLeast(0)
        val newLevel = XpEvents.levelForXp(newXp)
        userDoc(userId).update(mapOf("xp" to newXp, "level" to newLevel)).await()
    }

    // Deducts XP atomically (for undoing a task completion).
    // Uses a transaction to read the current XP from Firestore, preventing stale-value issues.
    suspend fun removeXp(userId: String, amount: Int) {
        db.runTransaction { tx ->
            val currentXp = tx.get(userDoc(userId)).getLong("xp")?.toInt() ?: 0
            val newXp     = (currentXp - amount).coerceAtLeast(0)
            tx.update(userDoc(userId), mapOf("xp" to newXp, "level" to XpEvents.levelForXp(newXp)))
        }.await()
    }

    // ========== Daily Activity ==========

    // Logs XP and task count for today. Used by the Profile activity chart.
    // Uses set+merge so it's safe to call multiple times per day.
    suspend fun logDailyActivity(userId: String, xpEarned: Int, tasksCompleted: Int) {
        val today = java.time.LocalDate.now().toEpochDay()
        userDoc(userId).collection("activity").document(today.toString())
            .set(
                mapOf(
                    "dateEpochDay"   to today,
                    "xpEarned"       to FieldValue.increment(xpEarned.toLong()),
                    "tasksCompleted" to FieldValue.increment(1L)
                ),
                SetOptions.merge()
            ).await()
    }

    // Reverses the daily activity log entry. Called when undoing a task completion.
    suspend fun removeDailyActivity(userId: String, xpToSubtract: Int, tasksToSubtract: Int = 1) {
        val today = java.time.LocalDate.now().toEpochDay()
        userDoc(userId).collection("activity").document(today.toString())
            .set(
                mapOf(
                    "xpEarned"       to FieldValue.increment(-xpToSubtract.toLong()),
                    "tasksCompleted" to FieldValue.increment(-tasksToSubtract.toLong())
                ),
                SetOptions.merge()
            ).await()
    }

    // ========== Settings ==========

    // Updates the user's app preferences. Only non-null params are written.
    suspend fun updatePreferences(
        userId: String,
        hardcoreModeEnabled: Boolean? = null,
        notificationsEnabled: Boolean? = null,
        dueSoonDays: Int? = null
    ) {
        val updates = mutableMapOf<String, Any>()
        hardcoreModeEnabled?.let  { updates["hardcoreModeEnabled"]  = it }
        notificationsEnabled?.let { updates["notificationsEnabled"] = it }
        dueSoonDays?.let          { updates["dueSoonDays"]          = it }
        if (updates.isNotEmpty()) userDoc(userId).update(updates).await()
    }

    // ========== Social ==========

    // Adds a friend by their userId. arrayUnion prevents duplicates.
    suspend fun addFriend(userId: String, friendId: String) {
        userDoc(userId).update("friends", FieldValue.arrayUnion(friendId)).await()
    }

    // Searches users by display name prefix. Used by the Add Friend screen.
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

    // ========== Admin ==========

    suspend fun updateHardcoreMode(userId: String, enabled: Boolean) {
        userDoc(userId).update("hardcoreModeEnabled", enabled).await()
    }
}
