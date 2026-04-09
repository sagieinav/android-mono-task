package dev.sagi.monotask.domain.repository

interface StatsRepository {
    suspend fun addXp(userId: String, amount: Int, currentXp: Int)
    suspend fun removeXp(userId: String, amount: Int)
    suspend fun updateUserStats(userId: String, xpGained: Int, wasAce: Boolean)
    suspend fun undoUserStats(userId: String, wasAce: Boolean)
}
