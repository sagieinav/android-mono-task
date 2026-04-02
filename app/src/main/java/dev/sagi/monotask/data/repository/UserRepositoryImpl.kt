package dev.sagi.monotask.data.repository

import android.util.Log
import androidx.annotation.DrawableRes
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.model.UserStats
import dev.sagi.monotask.domain.repository.UserRepository
import dev.sagi.monotask.domain.service.XpEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : UserRepository {

    private fun userDoc(userId: String) =
        db.collection("users").document(userId)

    override fun getUserStream(userId: String): Flow<User?> =
        userDoc(userId)
            .snapshots()
            .map {
                it.toObject(User::class.java)?.copy(id = it.id)
                    ?: run { Log.w("UserRepository", "Failed to deserialize user doc ${it.id}"); null }
            }

    override suspend fun getUserOnce(userId: String): User? {
        val doc = userDoc(userId).get().await()
        return doc.toObject(User::class.java)?.copy(id = doc.id)
            ?: run { Log.w("UserRepository", "Failed to deserialize user doc ${doc.id}"); null }
    }

    override suspend fun createUserIfNotExists(user: User) {
        val doc = userDoc(user.id).get().await()
        if (!doc.exists()) userDoc(user.id).set(user).await()
    }

    override suspend fun updateProfile(userId: String, displayName: String) {
        userDoc(userId).update("displayName", displayName).await()
    }

    override suspend fun updateAvatarPreset(userId: String, @DrawableRes preset: Int) {
        userDoc(userId).update("avatarPreset", preset).await()
    }

    override suspend fun completeOnboarding(userId: String) {
        userDoc(userId).update("onboarded", true).await()
    }

    override suspend fun addXp(userId: String, amount: Int, currentXp: Int, currentLevel: Int) {
        val newXp    = (currentXp + amount).coerceAtLeast(0)
        val newLevel = XpEngine.levelForXp(newXp)
        userDoc(userId).update(mapOf("xp" to newXp, "level" to newLevel)).await()
    }

    override suspend fun removeXp(userId: String, amount: Int) {
        db.runTransaction { tx ->
            val currentXp = tx.get(userDoc(userId)).getLong("xp")?.toInt() ?: 0
            val newXp     = (currentXp - amount).coerceAtLeast(0)
            tx.update(userDoc(userId), mapOf("xp" to newXp, "level" to XpEngine.levelForXp(newXp)))
        }.await()
    }

    override suspend fun logDailyActivity(userId: String, xpEarned: Int, tasksCompleted: Int) {
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

    override suspend fun removeDailyActivity(
        userId: String,
        xpToSubtract: Int,
        tasksToSubtract: Int,
        dateEpochDay: Long
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

    override fun getActivity(userId: String, range: ClosedRange<Long>?): Flow<List<DailyActivity>> {
        val col   = userDoc(userId).collection("activity")
        val query = if (range != null)
            col.whereGreaterThanOrEqualTo("dateEpochDay", range.start)
               .whereLessThanOrEqualTo("dateEpochDay", range.endInclusive)
        else col
        return query.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { it.toObject(DailyActivity::class.java) }
        }
    }

    override suspend fun getActivityOnce(userId: String, range: ClosedRange<Long>?): List<DailyActivity> {
        val col   = userDoc(userId).collection("activity")
        val query = if (range != null)
            col.whereGreaterThanOrEqualTo("dateEpochDay", range.start)
               .whereLessThanOrEqualTo("dateEpochDay", range.endInclusive)
        else col
        return query.get().await().mapNotNull { it.toObject(DailyActivity::class.java) }
    }

    override suspend fun getTopPerformanceDay(userId: String): DailyActivity? =
        userDoc(userId).collection("activity")
            .orderBy("xpEarned", Query.Direction.DESCENDING)
            .limit(1)
            .get().await()
            .mapNotNull { it.toObject(DailyActivity::class.java) }
            .firstOrNull()

    override suspend fun updateHyperfocusMode(userId: String, enabled: Boolean) {
        userDoc(userId).update("hyperfocusModeEnabled", enabled).await()
    }

    override suspend fun updatePriorityWeights(userId: String, dueDateWeight: Float) {
        userDoc(userId).update("dueDateWeight", dueDateWeight).await()
    }

    override suspend fun getUserById(uid: String): User? = getUserOnce(uid)

    override suspend fun addFriend(userId: String, friendId: String) {
        userDoc(userId).update("friends", FieldValue.arrayUnion(friendId)).await()
    }

    override suspend fun addFriendBatch(userId: String, friendId: String) {
        db.batch().apply {
            update(userDoc(userId),   "friends", FieldValue.arrayUnion(friendId))
            update(userDoc(friendId), "friends", FieldValue.arrayUnion(userId))
        }.commit().await()
    }

    override suspend fun removeFriendBatch(userId: String, friendId: String) {
        db.batch().apply {
            update(userDoc(userId),   "friends", FieldValue.arrayRemove(friendId))
            update(userDoc(friendId), "friends", FieldValue.arrayRemove(userId))
        }.commit().await()
    }

    override suspend fun updateUserStats(
        userId: String,
        xpGained: Int,
        wasAce: Boolean,
        newAchievements: List<Achievement>
    ) {
        val todayEpoch = LocalDate.now().toEpochDay()
        val weekStart  = LocalDate.now().with(DayOfWeek.MONDAY).toEpochDay()

        db.runTransaction { tx ->
            val snap    = tx.get(userDoc(userId))
            val current = snap.toObject(User::class.java)?.stats ?: UserStats()

            val weeklyXp = if (current.weekStartEpochDay < weekStart) xpGained
                           else current.weeklyXp + xpGained

            val yesterday = todayEpoch - 1
            val newStreak = when {
                current.lastActiveEpochDay == todayEpoch -> current.currentStreak
                current.lastActiveEpochDay == yesterday  -> current.currentStreak + 1
                else                                     -> 1
            }

            val updatedAchievements = current.earnedAchievements.toMutableMap()
            newAchievements.forEach { a ->
                if (a.earnedTier != null) updatedAchievements[a.category.name] = a.earnedTier.name
            }

            val updated = current.copy(
                totalTasksCompleted = current.totalTasksCompleted + 1,
                aceCount            = current.aceCount + if (wasAce) 1 else 0,
                currentStreak       = newStreak,
                longestStreak       = maxOf(current.longestStreak, newStreak),
                weeklyXp            = weeklyXp,
                weekStartEpochDay   = weekStart,
                lastActiveEpochDay  = todayEpoch,
                earnedAchievements  = updatedAchievements
            )
            tx.update(userDoc(userId), "stats", updated)
        }.await()
    }

    override suspend fun undoUserStats(userId: String, wasAce: Boolean) {
        db.runTransaction { tx ->
            val current = tx.get(userDoc(userId)).toObject(User::class.java)?.stats ?: UserStats()
            val updated = current.copy(
                totalTasksCompleted = (current.totalTasksCompleted - 1).coerceAtLeast(0),
                aceCount            = if (wasAce) (current.aceCount - 1).coerceAtLeast(0) else current.aceCount
            )
            tx.update(userDoc(userId), "stats", updated)
        }.await()
    }

    override suspend fun patchStatsCount(userId: String, correctTotal: Int, correctAceCount: Int) {
        userDoc(userId).update(mapOf(
            "stats.totalTasksCompleted" to correctTotal,
            "stats.aceCount"            to correctAceCount
        )).await()
    }

    override suspend fun searchUsers(query: String): List<User> {
        val result = db.collection("users")
            .whereGreaterThanOrEqualTo("displayName", query)
            .whereLessThanOrEqualTo("displayName", query + "\uF8FF")
            .get().await()
        return result.documents.mapNotNull {
            it.toObject(User::class.java)?.copy(id = it.id)
                ?: run { Log.w("UserRepository", "Failed to deserialize user doc ${it.id}"); null }
        }
    }

    override suspend fun patchEarnedStats(
        userId: String,
        longestStreak: Int,
        earnedAchievements: Map<String, String>
    ) {
        userDoc(userId).update(mapOf(
            "stats.longestStreak"      to longestStreak,
            "stats.earnedAchievements" to earnedAchievements
        )).await()
    }
}
