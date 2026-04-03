package dev.sagi.monotask.ui.statistics

import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.domain.service.ActivityStats

// ========== UI States ==========

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Ready(
        // Weekly
        val weeklyXp: Int = 0,
        val weeklyTasks: Int = 0,
        val weekXpPoints: List<ActivityStats.ChartPoint> = emptyList(),
        val weekTaskPoints: List<ActivityStats.ChartPoint> = emptyList(),
        val weekXpTrend: Int = 0,
        val weekTaskTrend: Int = 0,
        // Monthly
        val monthlyXp: Int = 0,
        val monthXpPoints: List<ActivityStats.ChartPoint> = emptyList(),
        val monthXpTrend: Int = 0,
        val monthActivityData: List<DailyActivity> = emptyList(),
        // All-Time
        val totalXp: Int = 0,
        val totalTasks: Int = 0,
        val aceCount: Int = 0,
        val longestStreak: Int = 0,
        val topPerformanceDay: DailyActivity? = null,
        // Meta
        val isRefreshing: Boolean = false,
    ) : StatisticsUiState()
}

// ========== Event Callbacks ==========

sealed interface StatisticsEvent {
    object Refresh : StatisticsEvent
}

// ========== One-Shot UI Effects ==========

sealed interface StatisticsUiEffect {
    data class ShowError(val message: String) : StatisticsUiEffect
}
