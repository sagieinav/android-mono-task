package dev.sagi.monotask.domain.fake

import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class FakeActivityRepository : ActivityRepository {

    private val _activities = MutableStateFlow<List<DailyActivity>>(emptyList())

    var activities: List<DailyActivity>
        get() = _activities.value
        set(value) { _activities.value = value }

    data class LogCall(val xp: Int, val tasks: Int)
    data class RemoveCall(val xp: Int, val tasks: Int, val epochDay: Long)

    val logCalls = mutableListOf<LogCall>()
    val removeCalls = mutableListOf<RemoveCall>()

    override suspend fun logDailyActivity(userId: String, xpEarned: Int, tasksCompleted: Int) {
        logCalls.add(LogCall(xpEarned, tasksCompleted))
        val today = LocalDate.now().toEpochDay()
        _activities.update { list ->
            val existing = list.find { it.dateEpochDay == today }
            if (existing != null) {
                list.map {
                    if (it.dateEpochDay == today)
                        it.copy(xpEarned = it.xpEarned + xpEarned, tasksCompleted = it.tasksCompleted + tasksCompleted)
                    else it
                }
            } else {
                list + DailyActivity(dateEpochDay = today, xpEarned = xpEarned, tasksCompleted = tasksCompleted)
            }
        }
    }

    override suspend fun removeDailyActivity(
        userId: String,
        xpToSubtract: Int,
        tasksToSubtract: Int,
        dateEpochDay: Long
    ) {
        removeCalls.add(RemoveCall(xpToSubtract, tasksToSubtract, dateEpochDay))
    }

    override fun getActivity(userId: String, range: ClosedRange<Long>?): Flow<List<DailyActivity>> =
        if (range == null) _activities
        else _activities.map { list -> list.filter { it.dateEpochDay in range } }

    override suspend fun getActivityOnce(userId: String, range: ClosedRange<Long>?): List<DailyActivity> {
        val list = _activities.value
        return if (range == null) list else list.filter { it.dateEpochDay in range }
    }

    override suspend fun getTopPerformanceDay(userId: String): DailyActivity? =
        _activities.value.maxByOrNull { it.xpEarned }
}
