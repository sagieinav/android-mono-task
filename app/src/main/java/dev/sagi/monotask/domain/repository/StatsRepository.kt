package dev.sagi.monotask.domain.repository

import dev.sagi.monotask.data.model.Achievement

interface StatsRepository {
    suspend fun addXp(userId: String, amount: Int, currentXp: Int, currentLevel: Int)
    suspend fun removeXp(userId: String, amount: Int)
    suspend fun updateUserStats(
        userId: String,
        xpGained: Int,
        wasAce: Boolean,
        newAchievements: List<Achievement>
    )
    suspend fun undoUserStats(userId: String, wasAce: Boolean)
    suspend fun patchStatsCount(userId: String, correctTotal: Int, correctAceCount: Int)
    suspend fun patchEarnedStats(
        userId: String,
        longestStreak: Int,
        earnedAchievements: Map<String, String>
    )
}
