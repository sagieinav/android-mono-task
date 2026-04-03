package dev.sagi.monotask.ui.statistics

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.domain.repository.UserRepository
import dev.sagi.monotask.domain.service.ActivityStats
import dev.sagi.monotask.ui.common.BaseViewModel
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel<StatisticsUiState, StatisticsEvent, StatisticsUiEffect>() {

    override val initialState: StatisticsUiState = StatisticsUiState.Loading

    private var userId: String = ""

    init {
        viewModelScope.launch {
            userId = AuthUtils.awaitUid()
            fetchStatistics()
        }
    }

    override fun onEvent(event: StatisticsEvent) {
        when (event) {
            is StatisticsEvent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        val current = _uiState.value as? StatisticsUiState.Ready ?: return
        _uiState.value = current.copy(isRefreshing = true)
        fetchStatistics()
    }

    private fun fetchStatistics() {
        viewModelScope.launch {
            try {
                coroutineScope {
                    val userDeferred = async { userRepository.getUserOnce(userId) }
                    val activityDeferred = async { userRepository.getActivityOnce(userId, widestRange) }
                    val topDayDeferred = async { userRepository.getTopPerformanceDay(userId) }

                    val user = userDeferred.await()
                    val allActivity = activityDeferred.await()
                    val topDay = topDayDeferred.await()

                    val monthActivity = allActivity.filter { it.dateEpochDay in UserRepository.thisMonthRange }
                    val weekActivity = ActivityStats.weekActivity(allActivity)

                    _uiState.value = StatisticsUiState.Ready(
                        weeklyXp = weekActivity.sumOf { it.xpEarned },
                        weeklyTasks = weekActivity.sumOf { it.tasksCompleted },
                        weekXpPoints = ActivityStats.buildXpPoints(weekActivity),
                        weekTaskPoints = ActivityStats.buildTaskPoints(weekActivity),
                        weekXpTrend = ActivityStats.computeXpTrend(weekActivity),
                        weekTaskTrend = ActivityStats.computeTaskTrend(weekActivity),
                        monthlyXp = monthActivity.sumOf { it.xpEarned },
                        monthXpPoints = ActivityStats.buildXpPointsMonthly(monthActivity),
                        monthXpTrend = ActivityStats.computeXpTrendMonthly(monthActivity),
                        monthActivityData = monthActivity,
                        totalXp = user?.xp ?: 0,
                        totalTasks = user?.stats?.totalTasksCompleted ?: 0,
                        aceCount = user?.stats?.aceCount ?: 0,
                        longestStreak = user?.stats?.longestStreak ?: 0,
                        topPerformanceDay = topDay,
                        isRefreshing = false,
                    )
                }
            } catch (e: Exception) {
                sendEffect(StatisticsUiEffect.ShowError("Failed to load statistics: ${e.message}"))
                val current = _uiState.value as? StatisticsUiState.Ready
                if (current != null) _uiState.value = current.copy(isRefreshing = false)
            }
        }
    }

    // Covers both the last 7 days (weekly charts) and the current calendar month (heatmap).
    // Early in the month these diverge — e.g. on April 2 we need data back to March 27.
    private val widestRange: ClosedRange<Long>
        get() {
            val today = LocalDate.now().toEpochDay()
            val sevenDaysAgo = LocalDate.now().minusDays(6).toEpochDay()
            val monthStart = LocalDate.now().withDayOfMonth(1).toEpochDay()
            return minOf(sevenDaysAgo, monthStart)..today
        }
}
