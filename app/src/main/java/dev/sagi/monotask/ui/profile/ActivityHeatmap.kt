package dev.sagi.monotask.ui.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

/**
 * Monthly activity heatmap — inspired by AwesomeUI's ConsistencyHeatmapCardGlow,
 * written from scratch so we have full control over styling and no extra dependency.
 */
@Composable
fun ActivityHeatmap(
    activityData: List<DailyActivity>,
    currentStreak: Int,
    recordStreak: Int,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    var year  by remember { mutableIntStateOf(today.year) }
    var month by remember { mutableIntStateOf(today.monthValue) }

    val intensityMap = remember(activityData) {
        if (activityData.isEmpty()) return@remember emptyMap<Long, Float>()
        val maxTasks = activityData.maxOf { it.tasksCompleted }.coerceAtLeast(1)
        activityData.associate { it.dateEpochDay to (it.tasksCompleted.toFloat() / maxTasks) }
    }

    val accentColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {

        // ── Month navigation ──────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                val prev = YearMonth.of(year, month).minusMonths(1)
                year = prev.year; month = prev.monthValue
            }) {
                Icon(
                    painter           = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = "Previous month"
                )
            }

            Text(
                text       = "${YearMonth.of(year, month).month.getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)} $year",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            val canGoForward = YearMonth.of(year, month).isBefore(YearMonth.now())
            IconButton(
                onClick  = {
                    val next = YearMonth.of(year, month).plusMonths(1)
                    year = next.year; month = next.monthValue
                },
                enabled = canGoForward
            ) {
                Icon(
                    painter           = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = "Next month",
                    tint              = if (canGoForward)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // ── Day-of-week labels ────────────────────────────────────────────
        Row(Modifier.fillMaxWidth()) {
            listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { label ->
                Text(
                    text      = label,
                    modifier  = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // ── Calendar grid ─────────────────────────────────────────────────
        val yearMonth   = YearMonth.of(year, month)
        val daysInMonth = yearMonth.lengthOfMonth()
        // Sunday = col 0. DayOfWeek: Mon=1…Sun=7, so (value % 7) maps Sun→0
        val startOffset = yearMonth.atDay(1).dayOfWeek.value % 7
        val rows        = (startOffset + daysInMonth + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in 0 until rows) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until 7) {
                        val dayNumber = row * 7 + col - startOffset + 1
                        if (dayNumber < 1 || dayNumber > daysInMonth) {
                            Box(Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val date      = yearMonth.atDay(dayNumber)
                            val intensity = intensityMap[date.toEpochDay()] ?: 0f
                            HeatmapCell(
                                intensity   = intensity,
                                isToday     = date == today,
                                isFuture    = date.isAfter(today),
                                accentColor = accentColor,
                                modifier    = Modifier.weight(1f).aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Streak counters ───────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StreakChip("Current streak", currentStreak, "🔥", Modifier.weight(1f))
            StreakChip("Record streak",  recordStreak,  "🏆", Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))

        // ── Intensity legend ──────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text("Less", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { intensity ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(cellColor(intensity, accentColor, isFuture = false))
                )
            }
            Spacer(Modifier.width(4.dp))
            Text("More", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Individual cell
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeatmapCell(
    intensity: Float,
    isToday: Boolean,
    isFuture: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(intensity, tween(400), label = "cell")
    val bgColor  = cellColor(animated, accentColor, isFuture)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .then(
                if (isToday) Modifier.drawBehind {
                    drawRoundRect(
                        color        = accentColor,
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style        = Stroke(1.5.dp.toPx())
                    )
                } else Modifier
            )
    )
}

private fun cellColor(intensity: Float, accentColor: Color, isFuture: Boolean): Color = when {
    isFuture      -> Color(0xFFEEEEEE)
    intensity <= 0f -> Color(0xFFE8E8E8)
    else          -> accentColor.copy(alpha = 0.2f + intensity * 0.8f)
}

// ─────────────────────────────────────────────────────────────────────────────
// Streak chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StreakChip(
    label: String,
    value: Int,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$emoji $value", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ActivityHeatmapPreview() {
    MonoTaskTheme {
        val today    = LocalDate.now()
        val fakeData = (0..20).map { daysBack ->
            DailyActivity(
                dateEpochDay   = today.minusDays(daysBack.toLong()).toEpochDay(),
                xpEarned       = (20..120).random(),
                tasksCompleted = (1..5).random()
            )
        }
        ActivityHeatmap(
            activityData  = fakeData,
            currentStreak = 7,
            recordStreak  = 14,
            modifier      = Modifier.padding(16.dp)
        )
    }
}
