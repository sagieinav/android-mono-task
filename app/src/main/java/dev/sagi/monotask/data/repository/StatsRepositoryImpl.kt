package dev.sagi.monotask.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.model.UserStats
import dev.sagi.monotask.domain.repository.StatsRepository
import dev.sagi.monotask.domain.service.XpEngine
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : StatsRepository {

    private fun userDoc(userId: String) =
        db.collection("users").document(userId)

    override suspend fun addXp(userId: String, amount: Int, currentXp: Int, currentLevel: Int) {
        val newXp = (currentXp + amount).coerceAtLeast(0)
        val newLevel = XpEngine.levelForXp(newXp)
        userDoc(userId).update(mapOf("xp" to newXp, "level" to newLevel)).await()
    }

    override suspend fun removeXp(userId: String, amount: Int) {
        db.runTransaction { tx ->
            val currentXp = tx.get(userDoc(userId)).getLong("xp")?.toInt() ?: 0
            val newXp = (currentXp - amount).coerceAtLeast(0)
            tx.update(userDoc(userId), mapOf("xp" to newXp, "level" to XpEngine.levelForXp(newXp)))
        }.await()
    }

    override suspend fun updateUserStats(
        userId: String,
        xpGained: Int,
        wasAce: Boolean,
        newAchievements: List<Achievement>
    ) {
        val todayEpoch = LocalDate.now().toEpochDay()
        val weekStart = LocalDate.now().with(DayOfWeek.MONDAY).toEpochDay()

        db.runTransaction { tx ->
            val snap = tx.get(userDoc(userId))
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
                aceCount = current.aceCount + if (wasAce) 1 else 0,
                currentStreak = newStreak,
                longestStreak = maxOf(current.longestStreak, newStreak),
                weeklyXp = weeklyXp,
                weekStartEpochDay = weekStart,
                lastActiveEpochDay = todayEpoch,
                earnedAchievements = updatedAchievements
            )
            tx.update(userDoc(userId), "stats", updated)
        }.await()
    }

    override suspend fun undoUserStats(userId: String, wasAce: Boolean) {
        db.runTransaction { tx ->
            val current = tx.get(userDoc(userId)).toObject(User::class.java)?.stats ?: UserStats()
            val updated = current.copy(
                totalTasksCompleted = (current.totalTasksCompleted - 1).coerceAtLeast(0),
                aceCount = if (wasAce) (current.aceCount - 1).coerceAtLeast(0) else current.aceCount
            )
            tx.update(userDoc(userId), "stats", updated)
        }.await()
    }

    override suspend fun patchStatsCount(userId: String, correctTotal: Int, correctAceCount: Int) {
        userDoc(userId).update(mapOf(
            "stats.totalTasksCompleted" to correctTotal,
            "stats.aceCount" to correctAceCount
        )).await()
    }

    override suspend fun patchEarnedStats(
        userId: String,
        longestStreak: Int,
        earnedAchievements: Map<String, String>
    ) {
        userDoc(userId).update(mapOf(
            "stats.longestStreak" to longestStreak,
            "stats.earnedAchievements" to earnedAchievements
        )).await()
    }
}
