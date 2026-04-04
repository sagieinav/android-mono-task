package dev.sagi.monotask.domain.repository

import dev.sagi.monotask.data.model.DailyActivity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ActivityRepository {

    companion object {
        val thisMonthRange: ClosedRange<Long>
            get() {
                val today = LocalDate.now()
                return today.withDayOfMonth(1).toEpochDay()..today.toEpochDay()
            }
    }

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
}
