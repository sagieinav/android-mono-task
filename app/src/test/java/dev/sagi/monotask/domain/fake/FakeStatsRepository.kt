package dev.sagi.monotask.domain.fake

import dev.sagi.monotask.domain.repository.StatsRepository

class FakeStatsRepository : StatsRepository {

    var shouldThrowOnUpdateStats = false

    data class AddXpCall(val amount: Int, val currentXp: Int)
    data class UpdateStatsCall(val xpGained: Int, val wasAce: Boolean)

    val addXpCalls = mutableListOf<AddXpCall>()
    val removeXpCalls = mutableListOf<Int>()
    val updateStatsCalls = mutableListOf<UpdateStatsCall>()
    val undoStatsCalls = mutableListOf<Boolean>()

    override suspend fun addXp(userId: String, amount: Int, currentXp: Int) {
        addXpCalls.add(AddXpCall(amount, currentXp))
    }

    override suspend fun removeXp(userId: String, amount: Int) {
        removeXpCalls.add(amount)
    }

    override suspend fun updateUserStats(userId: String, xpGained: Int, wasAce: Boolean) {
        if (shouldThrowOnUpdateStats) throw RuntimeException("Simulated stats error")
        updateStatsCalls.add(UpdateStatsCall(xpGained, wasAce))
    }

    override suspend fun undoUserStats(userId: String, wasAce: Boolean) {
        undoStatsCalls.add(wasAce)
    }
}
