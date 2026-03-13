package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Tab 2 — Statistics: XP stat card, heatmap, bar charts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatisticsTab(
    state: ProfileUiState.Ready,
    bottomPadding: Dp
) {
    val streak = remember(state.activityData) { computeCurrentStreak(state.activityData) }
    val record = remember(state.activityData) { computeRecordStreak(state.activityData) }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top        = 20.dp,
            bottom     = bottomPadding + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            StatCard(
                label = "Total XP",
                value = state.user.xp.toString(),
                emoji = "✨"
            )
        }

        item {
            SectionTitle("Activity")
            ActivityHeatmap(
                activityData  = state.activityData,
                currentStreak = streak,
                recordStreak  = record,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        item {
            SectionTitle("XP: last 7 days")
            SimpleBarChart(
                bars   = last7Days(state.activityData).map { it?.xpEarned?.toFloat() ?: 0f },
                labels = last7DayLabels(),
                color  = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            SectionTitle("Tasks completed: last 7 days")
            SimpleBarChart(
                bars   = last7Days(state.activityData).map { it?.tasksCompleted?.toFloat() ?: 0f },
                labels = last7DayLabels(),
                color  = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun StatCard(label: String, value: String, emoji: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(text = emoji, fontSize = 36.sp)
    }
}

/**
 * Minimal bar chart — will be upgraded to Vico once the dependency lands.
 * Already accepts the same data shape so the swap will be a drop-in.
 */
@Composable
fun SimpleBarChart(
    bars: List<Float>,
    labels: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val max = bars.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .height(120.dp),
            verticalAlignment     = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            bars.forEach { value ->
                val fraction = value / max
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height((120 * fraction).dp.coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(color.copy(alpha = 0.8f))
                )
            }
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            labels.forEach { label ->
                Text(
                    text      = label,
                    modifier  = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Streak + date helpers (private to this tab)
// ─────────────────────────────────────────────────────────────────────────────

fun computeCurrentStreak(data: List<DailyActivity>): Int {
    if (data.isEmpty()) return 0
    val days = data
        .filter { it.tasksCompleted > 0 }
        .map { it.dateEpochDay }
        .toSortedSet()
        .toList()
        .sortedDescending()

    var streak   = 0
    var expected = LocalDate.now().toEpochDay()
    for (day in days) {
        if (day == expected || day == expected - 1) {
            streak++
            expected = day - 1
        } else break
    }
    return streak
}

fun computeRecordStreak(data: List<DailyActivity>): Int {
    if (data.isEmpty()) return 0
    val days = data
        .filter { it.tasksCompleted > 0 }
        .map { it.dateEpochDay }
        .toSortedSet()
        .toList()
        .sorted()

    var best    = 0
    var current = 0
    var prev    = Long.MIN_VALUE
    for (day in days) {
        current = if (day == prev + 1) current + 1 else 1
        if (current > best) best = current
        prev = day
    }
    return best
}

fun last7Days(data: List<DailyActivity>): List<DailyActivity?> {
    val today = LocalDate.now()
    val map   = data.associateBy { it.dateEpochDay }
    return (6 downTo 0).map { daysBack ->
        map[today.minusDays(daysBack.toLong()).toEpochDay()]
    }
}

fun last7DayLabels(): List<String> {
    val today = LocalDate.now()
    return (6 downTo 0).map { daysBack ->
        today.minusDays(daysBack.toLong())
            .dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.getDefault())
            .take(2)
    }
}



@Preview(showBackground = true)
@Composable
private fun StatisticsTabPreview() {
    val today = LocalDate.now()
    MonoTaskTheme {
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
            bottomPadding = 0.dp
        )
    }
}