package dev.sagi.monotask.data.demo

import dev.sagi.monotask.domain.repository.StatsRepository
import javax.inject.Inject

class DemoStatsRepository @Inject constructor(
    private val userRepository: DemoUserRepository
) : StatsRepository {

    override suspend fun addXp(userId: String, amount: Int, currentXp: Int) {
        userRepository.applyXpDelta(amount)
    }

    override suspend fun removeXp(userId: String, amount: Int) {
        userRepository.applyXpDelta(-amount)
    }

    override suspend fun updateUserStats(userId: String, xpGained: Int, wasAce: Boolean) {
        userRepository.applyStatsDelta(xpGained, wasAce)
    }

    override suspend fun undoUserStats(userId: String, wasAce: Boolean) {
        userRepository.undoStats(wasAce)
    }
}
