package dev.sagi.monotask.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.util.XpEvents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class UserRepository {

    private val db = MonoTaskApp.instance.db

    private fun userDoc(userId: String) =
        db.collection("users").document(userId)

    // ========== Reads ==========

    fun getUserStream(userId: String): Flow<User?> =
        userDoc(userId)
            .snapshots()
            .map { it.toObject(User::class.java)?.copy(id = it.id) }

    suspend fun getUserOnce(userId: String): User? {
        val doc = userDoc(userId).get().await()
        return doc.toObject(User::class.java)?.copy(id = doc.id)
    }

    // ========== User Lifecycle ==========

    suspend fun createUserIfNotExists(user: User) {
        val doc = userDoc(user.id).get().await()
        if (!doc.exists()) userDoc(user.id).set(user).await()
    }

    suspend fun updateProfile(userId: String, displayName: String, profilePicUrl: String) {
        userDoc(userId).update(mapOf(
            "displayName"   to displayName,
            "profilePicUrl" to profilePicUrl
        )).await()
    }

    suspend fun completeOnboarding(userId: String) {
        userDoc(userId).update("onboarded", true).await()
    }

    // ========== XP ==========

    suspend fun addXp(userId: String, amount: Int, currentXp: Int, currentLevel: Int) {
        val newXp    = (currentXp + amount).coerceAtLeast(0)
        val newLevel = XpEvents.levelForXp(newXp)
        userDoc(userId).update(mapOf("xp" to newXp, "level" to newLevel)).await()
    }

    suspend fun removeXp(userId: String, amount: Int) {
        db.runTransaction { tx ->
            val currentXp = tx.get(userDoc(userId)).getLong("xp")?.toInt() ?: 0
            val newXp     = (currentXp - amount).coerceAtLeast(0)
            tx.update(userDoc(userId), mapOf("xp" to newXp, "level" to XpEvents.levelForXp(newXp)))
        }.await()
    }

    // ========== Daily Activity ==========

    suspend fun logDailyActivity(userId: String, xpEarned: Int, tasksCompleted: Int) {
        val today = LocalDate.now().toEpochDay()
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

    suspend fun removeDailyActivity(userId: String, xpToSubtract: Int, tasksToSubtract: Int = 1) {
        val today = LocalDate.now().toEpochDay()
        userDoc(userId).collection("activity").document(today.toString())
            .set(
                mapOf(
                    "xpEarned"       to FieldValue.increment(-xpToSubtract.toLong()),
                    "tasksCompleted" to FieldValue.increment(-tasksToSubtract.toLong())
                ),
                SetOptions.merge()
            ).await()
    }

    // Fetches last 7 days for the weekly charts
    suspend fun getActivityLast7Days(userId: String): List<DailyActivity> {
        val today     = LocalDate.now()
        val epochDays = (0..6).map { today.minusDays(it.toLong()).toEpochDay() }
        val docs      = userDoc(userId).collection("activity")
            .whereIn("dateEpochDay", epochDays)
            .get().await()
        return docs.mapNotNull { it.toObject(DailyActivity::class.java) }
    }

    // Fetches all activity docs for the current calendar month.
    // Uses a range query instead of whereIn to avoid the 30-item limit
    fun getActivityForCurrentMonth(userId: String): Flow<List<DailyActivity>> {
        val today      = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).toEpochDay()
        val monthEnd   = today.toEpochDay()
        return userDoc(userId).collection("activity")
            .whereGreaterThanOrEqualTo("dateEpochDay", monthStart)
            .whereLessThanOrEqualTo("dateEpochDay", monthEnd)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    it.toObject(DailyActivity::class.java)
                }
            }
    }
    suspend fun getActivityForCurrentMonthOnce(userId: String): List<DailyActivity> {
        val today      = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).toEpochDay()
        val monthEnd   = today.toEpochDay()   // only up to today; future days have no data
        val docs       = userDoc(userId).collection("activity")
            .whereGreaterThanOrEqualTo("dateEpochDay", monthStart)
            .whereLessThanOrEqualTo("dateEpochDay", monthEnd)
            .get().await()
        return docs.mapNotNull { it.toObject(DailyActivity::class.java) }
    }


    // ========== Settings ==========

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

    suspend fun addFriend(userId: String, friendId: String) {
        userDoc(userId).update("friends", FieldValue.arrayUnion(friendId)).await()
    }

    suspend fun searchUsers(query: String): List<User> {
        val result = db.collection("users")
            .whereGreaterThanOrEqualTo("displayName", query)
            .whereLessThanOrEqualTo("displayName", query + "\uF8FF")
            .get().await()
        return result.documents.mapNotNull {
            it.toObject(User::class.java)?.copy(id = it.id)
        }
    }

    // ========== Admin ==========

    suspend fun updateHardcoreMode(userId: String, enabled: Boolean) {
        userDoc(userId).update("hardcoreModeEnabled", enabled).await()
    }
}
