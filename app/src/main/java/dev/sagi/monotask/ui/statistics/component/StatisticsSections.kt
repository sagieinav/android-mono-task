package dev.sagi.monotask.ui.statistics.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.theme.customColors
import dev.sagi.monotask.ui.statistics.StatisticsUiState

// =========================
// Section composables (used by StatisticsScreen)
// =========================

fun LazyListScope.weeklyItems(state: StatisticsUiState.Ready, animationKey: Int) {
    item {
        key(animationKey) {
            LineChart(
                title = "XP Earned",
                headlineValue = "${state.weeklyXp}",
                headlineUnit = "xp",
                points = state.weekXpPoints,
                trendPercent = state.weekXpTrend,
                lineColor = MaterialTheme.customColors.xp,
            )
        }
    }
    item {
        key(animationKey) {
            BarChart(
                title = "Tasks Completed",
                headlineValue = "${state.weeklyTasks}",
                headlineUnit = "tasks",
                points = state.weekTaskPoints,
                trendPercent = state.weekTaskTrend,
                animate = true
            )
        }
    }
}

fun LazyListScope.monthlyItems(state: StatisticsUiState.Ready, animationKey: Int) {
    item {
        key(animationKey) {
            LineChart(
                title = "XP Earned",
                headlineValue = "${state.monthlyXp}",
                headlineUnit = "xp",
                points = state.monthXpPoints,
                trendPercent = state.monthXpTrend,
                lineColor = MaterialTheme.customColors.xp,
            )
        }
    }
    item {
        key(animationKey) {
            ActivityHeatmap(
                activeDays = state.monthActiveDays,
                totalTasksThisMonth = state.monthTotalTasks,
                bestStreak = state.monthBestStreak
            )
        }
    }
}

fun LazyListScope.allTimeItems(state: StatisticsUiState.Ready, animationKey: Int) {
    item {
        key(animationKey) {
            TopPerformanceCard(bestDay = state.topPerformanceDay)
        }
    }
    item {
        key(animationKey) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TotalTasksCard(totalTasks = state.totalTasks, modifier = Modifier.weight(1f))
                TotalXpCard(totalXp = state.totalXp, modifier = Modifier.weight(1f))
            }
        }
    }
    item {
        key(animationKey) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AceCompletionCard(
                    aceCount = state.aceCount,
                    aceCompletionPct = state.aceCompletionPct,
                    modifier = Modifier.weight(1f)
                )
                StreakCard(
                    longestStreak = state.longestStreak,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
