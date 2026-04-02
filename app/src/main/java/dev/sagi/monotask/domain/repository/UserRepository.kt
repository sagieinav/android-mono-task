package dev.sagi.monotask.domain.repository

import androidx.annotation.DrawableRes
import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface UserRepository {

    companion object {
        val thisMonthRange: ClosedRange<Long>
            get() {
                val today = LocalDate.now()
                return today.withDayOfMonth(1).toEpochDay()..today.toEpochDay()
            }
    }

    fun getUserStream(userId: String): Flow<User?>
    suspend fun getUserOnce(userId: String): User?
    suspend fun createUserIfNotExists(user: User)
    suspend fun updateProfile(userId: String, displayName: String)
    suspend fun updateAvatarPreset(userId: String, @DrawableRes preset: Int)
    suspend fun completeOnboarding(userId: String)
    suspend fun addXp(userId: String, amount: Int, currentXp: Int, currentLevel: Int)
    suspend fun removeXp(userId: String, amount: Int)
    suspend fun logDailyActivity(userId: String, xpEarned: Int, tasksCompleted: Int)
    suspend fun removeDailyActivity(
        userId: String,
        xpToSubtract: Int,
        tasksToSubtract: Int = 1,
        dateEpochDay: Long = LocalDate.now().toEpochDay()
    )
    fun getActivity(userId: String, range: ClosedRange<Long>? = null): Flow<List<DailyActivity>>
    suspend fun getActivityOnce(userId: String, range: ClosedRange<Long>? = null): List<DailyActivity>
    suspend fun getTopPerformanceDay(userId: String): DailyActivity?
    suspend fun updateHyperfocusMode(userId: String, enabled: Boolean)
    suspend fun updatePriorityWeights(userId: String, dueDateWeight: Float)
    suspend fun getUserById(uid: String): User?
    suspend fun addFriend(userId: String, friendId: String)
    suspend fun addFriendBatch(userId: String, friendId: String)
    suspend fun removeFriendBatch(userId: String, friendId: String)
    suspend fun updateUserStats(
        userId: String,
        xpGained: Int,
        wasAce: Boolean,
        newAchievements: List<Achievement>
    )
    suspend fun undoUserStats(userId: String, wasAce: Boolean)
    suspend fun patchStatsCount(userId: String, correctTotal: Int, correctAceCount: Int)
    suspend fun searchUsers(query: String): List<User>
    suspend fun patchEarnedStats(
        userId: String,
        longestStreak: Int,
        earnedAchievements: Map<String, String>
    )
}
