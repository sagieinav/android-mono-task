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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.util.ActivityStats
import dev.sagi.monotask.ui.component.statistics.AceCompletionCard
import dev.sagi.monotask.ui.component.statistics.ActivityHeatmap
import dev.sagi.monotask.ui.component.statistics.BarChart
import dev.sagi.monotask.ui.component.statistics.StreakCard
import dev.sagi.monotask.ui.component.statistics.LineChart
import dev.sagi.monotask.ui.component.statistics.TopPerformanceCard
import dev.sagi.monotask.ui.component.statistics.TotalTasksCard
import dev.sagi.monotask.ui.component.statistics.TotalXpCard
import dev.sagi.monotask.ui.theme.AceGold
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// Tab 2 — Statistics: XP stat card, heatmap, bar charts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatisticsTab(
    state: ProfileUiState.Ready,
    bottomPadding: Dp
) {
    val activity = state.activityData
    val tasks = state.completedTasks
    val monthActivity = state.monthActivityData
//    val workspaces = state.workspaces

    val aceCount    = tasks.count { it.isAce }
    val totalCompletedTasks  = tasks.size
    val totalXp = activity.sumOf { it.xpEarned }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(
            start           = 16.dp,
            end             = 16.dp,
            top             = 16.dp,
            bottom          = bottomPadding + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        item {
            StatSection("Weekly Activity") {
                // Xp this week (line chart)
                LineChart(
                    title        = "XP This Week",
                    headline     = "$totalXp XP",
                    points       = ActivityStats.buildXpPoints(activity),
                    trendPercent = ActivityStats.computeXpTrend(activity),
                    lineColor    = AceGold,
                )

                // Tasks this week (bar graph)
                BarChart(
                    title       = "Tasks This Week",
                    headline     = "${activity.sumOf { it.tasksCompleted }} completed",
                    points      = ActivityStats.buildTaskPoints(activity),
                    trendPercent = ActivityStats.computeTaskTrend(activity),
                    barColor    = MaterialTheme.colorScheme.primary,
                    animate     = true,
                )
            }
        }

        item {
            StatSection("Monthly Activity") {
                ActivityHeatmap(activityData = state.monthActivityData)
            }
        }


        item {
            StatSection("All Time") {
                // Top Performance Card
                TopPerformanceCard(activityData = state.activityData)

                // 4 half-width widget cards with simple data
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TotalTasksCard(
                        totalTasks = totalCompletedTasks,
                        modifier = Modifier.weight(1f)
                    )
                    AceCompletionCard(
                        aceCount = aceCount,
                        totalTasks = totalCompletedTasks,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TotalXpCard(
                        totalXp = totalXp,
                        modifier = Modifier.weight(1f)
                    )
                    StreakCard(
                        activityData = state.monthActivityData,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        // DonutChart Duo
//        item {
//            CompletionDonutCard(tasks, workspaces)
//        }
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