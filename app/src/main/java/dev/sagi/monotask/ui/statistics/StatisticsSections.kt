package dev.sagi.monotask.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.domain.service.ActivityStats
import dev.sagi.monotask.ui.component.display.ActivityHeatmap
import dev.sagi.monotask.ui.component.display.BarChart
import dev.sagi.monotask.ui.component.display.LineChart
import dev.sagi.monotask.ui.profile.ProfileUiState
import dev.sagi.monotask.ui.theme.customColors

// =========================
// Section composables (used by StatisticsScreen)
// =========================

fun LazyListScope.weeklyItems(
    state: ProfileUiState.Ready,
    animationKey: Int
) {
    val weekActivity = ActivityStats.weekActivity(state.activityData)
    val weeklyXP     = weekActivity.sumOf { it.xpEarned }
    val weeklyTasks  = weekActivity.sumOf { it.tasksCompleted }

    item {
        key(animationKey) {
            LineChart(
                title         = "XP Earned",
                headlineValue = "$weeklyXP",
                headlineUnit  = "xp",
                points        = ActivityStats.buildXpPoints(weekActivity),
                trendPercent  = ActivityStats.computeXpTrend(weekActivity),
                lineColor     = MaterialTheme.customColors.xp,
            )
        }
    }
    item {
        key(animationKey) {
            BarChart(
                title         = "Tasks Completed",
                headlineValue = "$weeklyTasks",
                headlineUnit  = "tasks",
                points        = ActivityStats.buildTaskPoints(weekActivity),
                trendPercent  = ActivityStats.computeTaskTrend(weekActivity),
                animate       = true
            )
        }
    }
}

fun LazyListScope.monthlyItems(state: ProfileUiState.Ready, animationKey: Int) {
    val monthlyXP = state.activityData.sumOf { it.xpEarned }
    item {
        key(animationKey) {
            LineChart(
                title         = "XP Earned",
                headlineValue = "$monthlyXP",
                headlineUnit  = "xp",
                points        = ActivityStats.buildXpPointsMonthly(state.activityData),
                trendPercent  = ActivityStats.computeXpTrendMonthly(state.activityData),
                lineColor     = MaterialTheme.customColors.xp,
            )
        }
    }
    item {
        key(animationKey) {
            ActivityHeatmap(
                activityData = state.activityData
            )
        }
    }
}

fun LazyListScope.allTimeItems(state: ProfileUiState.Ready, animationKey: Int) {
    val tasks               = state.completedTasks
    val aceCount            = tasks.count { it.isAce }
    val totalCompletedTasks = tasks.size
    val totalXP             = state.user.xp

    item {
        key(animationKey) {
            TopPerformanceCard(bestDay = state.topPerformanceDay)
        }
    }
    item {
        key(animationKey) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TotalTasksCard(totalTasks = totalCompletedTasks, modifier = Modifier.weight(1f))
                TotalXpCard(totalXp = totalXP, modifier = Modifier.weight(1f))
            }
        }
    }
    item {
        key(animationKey) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AceCompletionCard(
                    aceCount   = aceCount,
                    totalTasks = totalCompletedTasks,
                    modifier   = Modifier.weight(1f)
                )
                StreakCard(
                    activityData = state.activityData,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}