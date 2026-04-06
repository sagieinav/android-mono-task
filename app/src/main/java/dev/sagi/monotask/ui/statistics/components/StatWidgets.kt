package dev.sagi.monotask.ui.statistics.components

import dev.sagi.monotask.designsystem.theme.IconPack
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.designsystem.components.GlassSurface
import dev.sagi.monotask.designsystem.components.ValueLabel
import dev.sagi.monotask.designsystem.theme.customColors
import dev.sagi.monotask.designsystem.theme.LocalScaffoldPadding
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.circleGlow
import dev.sagi.monotask.designsystem.theme.glassBackground
import dev.sagi.monotask.designsystem.theme.glassBorder
import dev.sagi.monotask.designsystem.theme.gloock
import dev.sagi.monotask.designsystem.theme.monoShadow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ========== Shared design constants ==========

private val CircleRadius = 22.dp
private val CutoutPadding = 6.dp
private val CornerRadius = 28.dp
private val ContentTopPadding = 14.dp
private val ContentBottomPadding = 10.dp
private val ContentHorizPadding = 16.dp


private val SmallCardHeight = 140.dp
private val MediumCardHeight = 160.dp

private val DateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

// ========== InclusiveCutoutShape ==========
// Rounded rect with a circular cutout in the top-right corner

class InclusiveCutoutShape(
    private val topLeft: Dp = 12.dp,
    private val bottomLeft: Dp = 12.dp,
    private val bottomRight: Dp = 12.dp,
    private val circleRadius: Dp,
    private val padding: Dp = 10.dp,
    private val smoothing: Dp = 20.dp,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(path = Path().apply {
            val width = size.width
            val height = size.height
            val topLeft = with(density) { this@InclusiveCutoutShape.topLeft.toPx() }
            val bottomLeft = with(density) { this@InclusiveCutoutShape.bottomLeft.toPx() }
            val bottomRight = with(density) { this@InclusiveCutoutShape.bottomRight.toPx() }
            val cornerRad = with(density) { circleRadius.toPx() }
            val padding = with(density) { this@InclusiveCutoutShape.padding.toPx() }
            val smoothing = with(density) { this@InclusiveCutoutShape.smoothing.toPx() }
            val cutoutSize = (cornerRad * 2) + padding

            moveTo(0f, topLeft)
            if (topLeft > 0) quadraticTo(0f, 0f, topLeft, 0f) else lineTo(0f, 0f)
            lineTo(width - cutoutSize - smoothing, 0f)
            quadraticTo(width - cutoutSize, 0f, width - cutoutSize, smoothing)
            quadraticTo(width - cutoutSize, cutoutSize, width - smoothing, cutoutSize)
            quadraticTo(width, cutoutSize, width, cutoutSize + smoothing)
            lineTo(width, height - bottomRight)
            if (bottomRight > 0) quadraticTo(width, height, width - bottomRight, height) else lineTo(width, height)
            lineTo(bottomLeft, height)
            if (bottomLeft > 0) quadraticTo(0f, height, 0f, height - bottomLeft) else lineTo(0f, height)
            close()
        })
}

// ========== Shared icon circle ==========

@Composable
private fun WidgetIconCircle(icon: Painter, accentColor: Color) {
    Box(
        modifier = Modifier
            .size(CircleRadius * 2)
            .circleGlow(color = accentColor.copy(alpha = 0.15f), radius = 10.dp)
            .clip(CircleShape)
            .glassBackground(
                accentColor = accentColor,
                baseColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
            )
            .glassBorder(shape = CircleShape, color = accentColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = accentColor.copy(alpha = 0.8f),
            modifier = Modifier.size(26.dp)
        )
    }
}

// ========== StatWidgetSmall ==========
// Half-width card. Placed in a 2-column Row by the caller

@Composable
fun StatWidgetSmall(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String = "",
    icon: Painter,
    accentColor: Color,
    subtitle: String? = null,
) {
    val titleEndPadding = CircleRadius * 2 - 6.dp
    val cutoutShape = InclusiveCutoutShape(
        circleRadius = CircleRadius, padding = CutoutPadding,
        topLeft = CornerRadius, bottomLeft = CornerRadius, bottomRight = CornerRadius,
    )

    Box(modifier = modifier.height(SmallCardHeight)) {

        // Main Content:
        Box(
            modifier = Modifier
                .monoShadow(cutoutShape)
                .clip(cutoutShape)
                .glassBackground()
                .glassBorder(cutoutShape, accentColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ContentHorizPadding, vertical = 0.dp)
                    .padding(top = ContentTopPadding, bottom = ContentBottomPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = titleEndPadding)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        lineHeight = 20.sp, // reduce vertical "padding" when wrapped
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    subtitle?. let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor.copy(alpha = 0.8f)
                        )
                    }
                }

                ValueLabel(value = value, unit = unit)
            }
        }


        // Circle Icon:
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            WidgetIconCircle(icon = icon, accentColor = accentColor)
        }
    }
}

// ========== StatWidgetMedium ==========

@Composable
fun StatWidgetMedium(
    title: String,
    subtitle: String,
    value: String,
    icon: Painter,
    accentColor: Color,
    modifier: Modifier = Modifier,
    unit: String = "",
    dateLabel: String? = null,
    trailingValue: String? = null,
    trailingUnit: String? = null,
) {
    val cutoutShape = InclusiveCutoutShape(
        circleRadius = CircleRadius, padding = CutoutPadding,
        topLeft = CornerRadius, bottomLeft = CornerRadius, bottomRight = CornerRadius,
    )

    Box(modifier = modifier.fillMaxWidth().height(MediumCardHeight)) {

        // Main Content:
        Box(
            modifier = Modifier
                .monoShadow(cutoutShape)
                .clip(cutoutShape)
                .glassBackground()
                .glassBorder(cutoutShape, accentColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ContentHorizPadding)
                    .padding(top = ContentTopPadding, bottom = ContentBottomPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Section:
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Main Title:
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = accentColor.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Date Label:
                            dateLabel?. let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = accentColor
                                )
                            }
                            // Subtitle:
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
//                            fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }

                    }
                }

                // Bottom Section:
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    ValueLabel(value = value, unit = unit)
                    // Trailing value and unit (if not null)
                    trailingValue?. let { value ->
                        Column(horizontalAlignment = Alignment.End) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                    modifier = Modifier.alignByBaseline()
                                )
                                trailingUnit?. let { unit ->
                                    Text(
                                        text = unit,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.alignByBaseline()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ========== Circle icon ==========
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            WidgetIconCircle(icon = icon, accentColor = accentColor)
        }
    }
}



// ================================ Named card instances =================================

@Composable
fun TotalTasksCard(totalTasks: Int, modifier: Modifier = Modifier) {
    StatWidgetSmall(
        title = "Tasks Completed",
        value = totalTasks.toString(),
        unit = "total",
        icon = painterResource(IconPack.TaskAlt),
        accentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
fun AceCompletionCard(aceCount: Int, aceCompletionPct: Int, modifier: Modifier = Modifier) {
    StatWidgetSmall(
        title = "Ace Completion",
        value = "$aceCompletionPct%",
        unit = "ratio",
        icon = painterResource(IconPack.Ace),
        accentColor = MaterialTheme.customColors.aceDim,
        subtitle = "$aceCount ace tasks",
        modifier = modifier
    )
}

@Composable
fun TotalXpCard(totalXp: Int, modifier: Modifier = Modifier) {
    StatWidgetSmall(
        title = "Total XP",
        value = totalXp.toString(),
        unit = "xp",
        icon = painterResource(IconPack.Xp),
        accentColor = MaterialTheme.customColors.xp,
        modifier = modifier
    )
}

@Composable
fun StreakCard(
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    StatWidgetSmall(
        title = "Streak Record",
        value = longestStreak.toString(),
        unit = if (longestStreak == 1) "day" else "days",
        icon = painterResource(IconPack.Fire),
        accentColor = MaterialTheme.customColors.streak,
        modifier = modifier
    )
}

@Composable
fun TopPerformanceCard(
    bestDay: DailyActivity?,
    modifier: Modifier = Modifier,
) {
//    val best  = topPerformanceDay.maxByOrNull { it.xpEarned }
    val xp = bestDay?.xpEarned ?: 0
    val tasks = bestDay?.tasksCompleted ?: 0
    val date = remember(bestDay) {
        bestDay?.let { LocalDate.ofEpochDay(it.dateEpochDay).format(DateFormatter) }
    }

    StatWidgetMedium(
        title = "Top Performance",
        subtitle = "your most productive day ever",
        value = xp.toString(),
        unit = "xp",
        icon = painterResource(IconPack.TopPerformance),
        accentColor = MaterialTheme.customColors.aceDim,
        dateLabel = date,
        trailingValue = tasks.toString(),
        trailingUnit = if (tasks == 1) "task" else "tasks",
        modifier = modifier
    )
}

// ========== Preview ==========

@Preview
@Composable
private fun StatWidgetsPreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TopPerformanceCard(
                        bestDay = DailyActivity(LocalDate.now().toEpochDay(),               2, 150)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TotalTasksCard(totalTasks = 142, modifier = Modifier.weight(1f))
                        AceCompletionCard(
                            aceCount = 89,
                            aceCompletionPct = 77,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TotalXpCard(totalXp = 14250, modifier = Modifier.weight(1f))
                        StreakCard(longestStreak = 14, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
