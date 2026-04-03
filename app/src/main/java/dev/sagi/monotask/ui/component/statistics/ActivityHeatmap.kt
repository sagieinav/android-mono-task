package dev.sagi.monotask.ui.component.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.domain.service.ActivityStats
import dev.sagi.monotask.ui.theme.customColors
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.bonusGreen
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// ========== Design constants ==========

private val CellCornerRadius = 6.dp
private val CellSpacing      = 5.dp
private val DividerWidth     = 1.dp

private val DayLabels = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

// ========== Cell state ==========

private enum class CellState {
    ACTIVE, INACTIVE_PAST, FUTURE, EMPTY
}

// ========== ActivityHeatmap ==========

@Composable
fun ActivityHeatmap(
    activityData: List<DailyActivity>,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large
) {
    val today = remember { LocalDate.now() }
    val monthStart = today.withDayOfMonth(1)
    val monthEnd = today
    val monthName = remember { today.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) }

    val activeDays = remember(activityData) {
        activityData.filter { it.tasksCompleted > 0 }.map { it.dateEpochDay }.toSet()
    }

    val firstDayOffset = remember { monthStart.dayOfWeek.value % 7 }
    val daysInMonth    = remember { today.month.length(today.isLeapYear) }

    val cells: List<LocalDate?> = remember(today) {
        val list = MutableList<LocalDate?>(firstDayOffset) { null }
        for (day in 1..daysInMonth) list.add(monthStart.withDayOfMonth(day))
        while (list.size % 7 != 0) list.add(null)
        list
    }

    val weeks = remember(cells) { cells.chunked(7) }

    // Side panel stats
    val totalTasksThisMonth = remember(activityData) { activityData.sumOf { it.tasksCompleted } }
    val bestStreak = remember(activityData) {
        ActivityStats.computeRecordStreak(
            activityData, monthStart.toEpochDay()..monthEnd.toEpochDay()
        )
    }

    val labelColor = MaterialTheme.colorScheme.outlineVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val inactiveColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val activeColor = remember { lerp(bonusGreen, Color.Black, 0.15f) }

    StatCard(
        modifier = modifier,
        title = "Activity Heatmap",
        headlineValue = monthName,
        shape = shape
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {

            // ========== Grid ==========
            Column(
                modifier = Modifier.weight(0.65f),
                verticalArrangement = Arrangement.spacedBy(CellSpacing)
            ) {
                // Day-of-week labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CellSpacing)
                ) {
                    DayLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Week rows
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CellSpacing)
                    ) {
                        week.forEach { date ->
                            val state = when {
                                date == null -> CellState.EMPTY
                                date.isAfter(today) -> CellState.FUTURE
                                activeDays.contains(date.toEpochDay()) -> CellState.ACTIVE
                                else -> CellState.INACTIVE_PAST
                            }
                            HeatmapCell(
                                state = state,
                                isToday = date == today,
                                activeColor = activeColor,
                                inactiveColor = inactiveColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ========== Divider ==========
            Box(
                modifier = Modifier
                    .width(DividerWidth)
                    .fillMaxHeight()
                    .background(dividerColor)
                    .align(Alignment.CenterVertically)
            )

            // ========== Side panel ==========
            Column(
                modifier = Modifier.weight(0.35f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SideStat(
                    label = "Completed",
                    value = totalTasksThisMonth.toString(),
                    titleColor = MaterialTheme.colorScheme.primary,
                    unit = if (totalTasksThisMonth == 1) "task" else "tasks"
                )
                SideStat(
                    label = "Best Streak",
                    value = bestStreak.toString(),
                    titleColor = MaterialTheme.customColors.aceDim,
                    unit = if (bestStreak == 1) "day" else "days"
                )
            }
        }
    }
}

// ========== Side panel stat ==========

@Composable
private fun SideStat(
    label: String,
    value: String,
    titleColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
    unit: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = titleColor
        )
        Row(
            verticalAlignment     = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineMedium,
                color      = MaterialTheme.colorScheme.onSurface,
                modifier   = Modifier.alignByBaseline()
            )
            unit?.let {
                Text(
                    text       = it,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = MaterialTheme.colorScheme.outlineVariant,
                    modifier   = Modifier.alignByBaseline()
                )
            }
        }
    }
}

// ========== Single cell ==========

@Composable
private fun HeatmapCell(
    state: CellState,
    isToday: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
) {
    val shape       = RoundedCornerShape(CellCornerRadius)
    val stripeColor = lerp(inactiveColor, Color.Black, 0.08f)
    val borderWidth = 1.dp

    val todayBorderColor = when (state) {
        CellState.ACTIVE -> lerp(activeColor, Color.Black, 0.2f)
        else             -> lerp(inactiveColor, Color.Black, 0.6f)
    }

    val baseModifier = modifier.aspectRatio(1f).clip(shape)

    val cellModifier = when (state) {
        CellState.EMPTY -> baseModifier

        CellState.FUTURE -> baseModifier
            .glassBackground(baseColor = inactiveColor)
            .glassBorder(shape = shape, width = borderWidth)

        CellState.INACTIVE_PAST -> baseModifier
            .drawBehind {
                drawRect(inactiveColor)
                val gap = 2.dp.toPx()
                var x   = -(size.width + size.height)
                while (x < size.width + size.height) {
                    drawLine(
                        color       = stripeColor,
                        start       = Offset(x, 0f),
                        end         = Offset(x + size.height, size.height),
                        strokeWidth = gap / 2
                    )
                    x += gap * 2
                }
            }
            .glassBackground()
            .glassBorder(shape = shape, width = borderWidth)

        CellState.ACTIVE -> baseModifier
            .background(activeColor)
            .glassBackground(baseColor = activeColor)
            .glassBorder(shape = shape, color = activeColor, width = borderWidth)
    }

    val finalModifier = if (isToday && state != CellState.EMPTY) {
        cellModifier.border(borderWidth, todayBorderColor, shape)
    } else {
        cellModifier
    }

    Box(modifier = finalModifier)
}

// ========== Preview ==========

@Preview(showBackground = true)
@Composable
private fun ActivityHeatmapPreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            val today = LocalDate.now()
            val fakeData = listOf(
                DailyActivity(today.withDayOfMonth(1).toEpochDay(),  3, 120),
                DailyActivity(today.withDayOfMonth(2).toEpochDay(),  1, 40),
                DailyActivity(today.withDayOfMonth(5).toEpochDay(),  5, 210),
                DailyActivity(today.withDayOfMonth(6).toEpochDay(),  2, 80),
                DailyActivity(today.withDayOfMonth(7).toEpochDay(),  4, 160),
                DailyActivity(today.withDayOfMonth(10).toEpochDay(), 1, 30),
                DailyActivity(today.withDayOfMonth(12).toEpochDay(), 6, 280),
                DailyActivity(today.toEpochDay(),                    2, 90),
            ).filter { it.dateEpochDay <= today.toEpochDay() }

            ActivityHeatmap(
                activityData = fakeData,
                modifier     = Modifier.padding(16.dp)
            )
        }
    }
}
