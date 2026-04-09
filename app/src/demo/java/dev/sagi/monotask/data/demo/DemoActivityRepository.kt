package dev.sagi.monotask.data.demo

import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

class DemoActivityRepository @Inject constructor() : ActivityRepository {
    private val _activity = MutableStateFlow(DemoSeedData.DEMO_ACTIVITY.toMutableList())

    private val friendActivity = mapOf(
        DemoSeedData.DEMO_FRIEND_ROEI_ID to DemoSeedData.DEMO_FRIEND_ROEI_ACTIVITY,
        DemoSeedData.DEMO_FRIEND_OFEK_ID to DemoSeedData.DEMO_FRIEND_OFEK_ACTIVITY,
    )

    override suspend fun logDailyActivity(userId: String, xpEarned: Int, tasksCompleted: Int) {
        val today = LocalDate.now().toEpochDay()
        _activity.update { list ->
            val existing = list.indexOfFirst { it.dateEpochDay == today }
            if (existing >= 0) {
                val updated = list[existing].let {
                    it.copy(xpEarned = it.xpEarned + xpEarned, tasksCompleted = it.tasksCompleted + tasksCompleted)
                }
                list.toMutableList().also { it[existing] = updated }
            } else {
                (list + DailyActivity(today, tasksCompleted, xpEarned)).toMutableList()
            }
        }
    }

    override suspend fun removeDailyActivity(
        userId: String,
        xpToSubtract: Int,
        tasksToSubtract: Int,
        dateEpochDay: Long
    ) {
        _activity.update { list ->
            list.map { entry ->
                if (entry.dateEpochDay == dateEpochDay) {
                    entry.copy(
                        xpEarned = maxOf(0, entry.xpEarned - xpToSubtract),
                        tasksCompleted = maxOf(0, entry.tasksCompleted - tasksToSubtract)
                    )
                } else entry
            }.toMutableList()
        }
    }

    override fun getActivity(userId: String, range: ClosedRange<Long>?): Flow<List<DailyActivity>> =
        _activity.map { list ->
            if (range == null) list.toList() else list.filter { it.dateEpochDay in range }
        }

    override suspend fun getActivityOnce(userId: String, range: ClosedRange<Long>?): List<DailyActivity> {
        val list = friendActivity[userId] ?: _activity.value
        return if (range == null) list.toList() else list.filter { it.dateEpochDay in range }
    }

    override suspend fun getTopPerformanceDay(userId: String): DailyActivity? =
        _activity.value.maxByOrNull { it.xpEarned }
}
