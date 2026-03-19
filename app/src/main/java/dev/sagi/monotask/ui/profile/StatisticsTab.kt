package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.util.ActivityStats
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.component.statistics.AceCompletionCard
import dev.sagi.monotask.ui.component.statistics.ActivityHeatmap
import dev.sagi.monotask.ui.component.statistics.BarChart
import dev.sagi.monotask.ui.component.statistics.CompletionDonutCard
import dev.sagi.monotask.ui.component.statistics.StreakCard
import dev.sagi.monotask.ui.component.statistics.LineChart
import dev.sagi.monotask.ui.component.statistics.TopPerformanceCard
import dev.sagi.monotask.ui.component.statistics.TotalTasksCard
import dev.sagi.monotask.ui.component.statistics.TotalXpCard
import dev.sagi.monotask.ui.theme.AceGold
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.util.Constants
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// Tab 2 — Statistics: XP stat card, heatmap, bar charts
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatisticsTab(
    state: ProfileUiState.Ready,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    topPadding: Dp,
    bottomPadding: Dp
) {
    val tasks = state.completedTasks
    val monthActivity = state.activityData
    val weekActivity = remember(monthActivity) { ActivityStats.weekActivity(monthActivity) }

    val aceCount           = remember(tasks) { tasks.count { it.isAce } }
    val totalCompletedTasks = tasks.size
    val weeklyXP           = remember(weekActivity) { weekActivity.sumOf { it.xpEarned } }
    val weeklyTasks        = remember(weekActivity) { weekActivity.sumOf { it.tasksCompleted } }
    val totalXP            = state.user.xp

    val refreshState = rememberPullToRefreshState()
    val refreshThreshold = 40.dp

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh    = onRefresh,
        modifier     = Modifier.fillMaxSize(),
        state        = refreshState,
        threshold    = refreshThreshold,
        indicator    = {
            PullToRefreshDefaults.LoadingIndicator(
                state = refreshState,
                isRefreshing = isRefreshing,
                color = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                maxDistance = refreshThreshold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = topPadding)  // position below floating tab row
            )
        }
    ) {
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                // For pulling content down together with refresh indicator:
                .graphicsLayer {
                    translationY = refreshState.distanceFraction * refreshThreshold.toPx()
                },
            contentPadding      = PaddingValues(
                top    = topPadding,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            item {
                StatSection("Weekly Activity") {
                    // Xp this week (line chart)
                    LineChart(
                        title        = "XP This Week",
                        headline     = "$weeklyXP XP",
                        points       = ActivityStats.buildXpPoints(weekActivity),
                        trendPercent = ActivityStats.computeXpTrend(weekActivity),
                        lineColor    = AceGold,
                    )

                    // Tasks this week (bar graph)
                    BarChart(
                        title       = "Tasks This Week",
                        headline     = "$weeklyTasks completed",
                        points      = ActivityStats.buildTaskPoints(weekActivity),
                        trendPercent = ActivityStats.computeTaskTrend(weekActivity),
                        barColor    = MaterialTheme.colorScheme.primary,
                        animate     = true,
                    )
                }
            }

            item {
                StatSection("Monthly Activity") {
                    ActivityHeatmap(activityData = state.activityData)
                }
            }

            item {
                StatSection("All Time") {
                    // Top Performance Card
                    TopPerformanceCard(bestDay = state.topPerformanceDay)

                    // 4 half-width widget cards with simple data
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TotalTasksCard(
                            totalTasks = totalCompletedTasks,
                            modifier = Modifier.weight(1f)
                        )
                        TotalXpCard(
                            totalXp = totalXP,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AceCompletionCard(
                            aceCount = aceCount,
                            totalTasks = totalCompletedTasks,
                            modifier = Modifier.weight(1f)
                        )
                        StreakCard(
                            activityData = state.activityData,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StatSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    SectionTitle(title)
    Column(
        modifier            = modifier.padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
    }
}
@Composable
private fun SectionTitle(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.headlineSmall,
//        fontFamily = gloock,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        textAlign  = TextAlign.Center,
        modifier   = Modifier.fillMaxWidth()
    )
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StatisticsTabPreview() {
    val today = LocalDate.now()
    MonoTaskTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            StatisticsTab(
                isRefreshing  = false,
                onRefresh     = {},
                topPadding    = 0.dp,
                state = ProfileUiState.Ready(
                    user           = User(id = "1", displayName = "Sagi Einav", level = 25, xp = 12450),
                    level          = 25,
                    levelProgress  = 0.73f,
                    xpIntoLevel    = 2115,
                    xpForNextLevel = 2326,
                    badges         = emptyList(),
                    activityData   = listOf(
                        DailyActivity(dateEpochDay = today.minusDays(6).toEpochDay(), xpEarned = 120, tasksCompleted = 4),
                        DailyActivity(dateEpochDay = today.minusDays(5).toEpochDay(), xpEarned = 0,   tasksCompleted = 0),
                        DailyActivity(dateEpochDay = today.minusDays(4).toEpochDay(), xpEarned = 310, tasksCompleted = 8),
                        DailyActivity(dateEpochDay = today.minusDays(3).toEpochDay(), xpEarned = 85,  tasksCompleted = 3),
                        DailyActivity(dateEpochDay = today.minusDays(2).toEpochDay(), xpEarned = 200, tasksCompleted = 6),
                        DailyActivity(dateEpochDay = today.minusDays(1).toEpochDay(), xpEarned = 450, tasksCompleted = 11),
                        DailyActivity(dateEpochDay = today.toEpochDay(),              xpEarned = 90,  tasksCompleted = 2),
                    )
                ),
                bottomPadding = 0.dp,
//            modifier = Modifier.background(MaterialTheme.colorScheme.background)
            )
        }
        }
}