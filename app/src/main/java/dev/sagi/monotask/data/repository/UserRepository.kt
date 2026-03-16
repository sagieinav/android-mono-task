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

    companion object {
        val thisMonthRange: ClosedRange<Long> get() {
            val today = LocalDate.now()
            return today.withDayOfMonth(1).toEpochDay()..today.toEpochDay()
        }
        val last7DaysRange: ClosedRange<Long> get() {
            val today = LocalDate.now()
            return today.minusDays(6).toEpochDay()..today.toEpochDay()
        }
    }

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

    suspend fun removeDailyActivity(
        userId: String,
        xpToSubtract: Int,
        tasksToSubtract: Int = 1,
        dateEpochDay: Long = LocalDate.now().toEpochDay()
    ) {
        userDoc(userId).collection("activity").document(dateEpochDay.toString())
            .set(
                mapOf(
                    "xpEarned"       to FieldValue.increment(-xpToSubtract.toLong()),
                    "tasksCompleted" to FieldValue.increment(-tasksToSubtract.toLong())
                ),
                SetOptions.merge()
            ).await()
    }

    // Live stream, optional time range
    fun getActivity(userId: String, range: ClosedRange<Long>? = null): Flow<List<DailyActivity>> {
        val col   = userDoc(userId).collection("activity")
        val query = if (range != null)
            col.whereGreaterThanOrEqualTo("dateEpochDay", range.start)
               .whereLessThanOrEqualTo("dateEpochDay", range.endInclusive)
        else col
        return query.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { it.toObject(DailyActivity::class.java) }
        }
    }

    // One-shot fetch, optional time range
    suspend fun getActivityOnce(userId: String, range: ClosedRange<Long>? = null): List<DailyActivity> {
        val col   = userDoc(userId).collection("activity")
        val query = if (range != null)
            col.whereGreaterThanOrEqualTo("dateEpochDay", range.start)
               .whereLessThanOrEqualTo("dateEpochDay", range.endInclusive)
        else col
        return query.get().await().mapNotNull { it.toObject(DailyActivity::class.java) }
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
