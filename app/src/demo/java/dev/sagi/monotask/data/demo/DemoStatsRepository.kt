package dev.sagi.monotask.data.demo

import dev.sagi.monotask.domain.repository.StatsRepository
import javax.inject.Inject

// Demo: stats are read-only — all write operations are intentional no-ops so the demo user's
// seed stats always look good regardless of what actions the presenter takes during the demo.
class DemoStatsRepository @Inject constructor() : StatsRepository {
    override suspend fun addXp(userId: String, amount: Int, currentXp: Int) = Unit
    override suspend fun removeXp(userId: String, amount: Int) = Unit
    override suspend fun updateUserStats(userId: String, xpGained: Int, wasAce: Boolean) = Unit
    override suspend fun undoUserStats(userId: String, wasAce: Boolean) = Unit
}
