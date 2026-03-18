package dev.sagi.monotask.ui.component.statistics

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.domain.util.ActivityStats.computeRecordStreak
import dev.sagi.monotask.ui.theme.AceGoldDim
import dev.sagi.monotask.ui.theme.StreakFire
import dev.sagi.monotask.ui.theme.XpViolet
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.circleGlow
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.ui.theme.gloock
import dev.sagi.monotask.ui.theme.ibmPlexMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ========== Shared design constants ==========

private val CircleRadius     = 22.dp
private val CutoutPadding    = 6.dp
private val CornerRadius     = 28.dp
private val ContentTopPadding = 14.dp
private val ContentBottomPadding = 10.dp
private val ContentHorizPadding = 16.dp

// The cutout circle bottom edge relative to content top — used to clear date label
private val CutoutClearance  = CircleRadius * 2 - ContentTopPadding + 2.dp

private val SmallCardHeight  = 140.dp
private val MediumCardHeight = 160.dp

private val StreakFireColor  = StreakFire
private val XpVioletColor    = XpViolet

private val DateFormatter    = DateTimeFormatter.ofPattern("EEE, MMM d")

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
            val w  = size.width
            val h  = size.height
            val tl = with(density) { topLeft.toPx() }
            val bl = with(density) { bottomLeft.toPx() }
            val br = with(density) { bottomRight.toPx() }
            val cR = with(density) { circleRadius.toPx() }
            val p  = with(density) { padding.toPx() }
            val s  = with(density) { smoothing.toPx() }
            val cutoutSize = (cR * 2) + p

            moveTo(0f, tl)
            if (tl > 0) quadraticTo(0f, 0f, tl, 0f) else lineTo(0f, 0f)
            lineTo(w - cutoutSize - s, 0f)
            quadraticTo(w - cutoutSize, 0f,         w - cutoutSize, s)
            quadraticTo(w - cutoutSize, cutoutSize, w - s,          cutoutSize)
            quadraticTo(w,             cutoutSize, w,               cutoutSize + s)
            lineTo(w, h - br)
            if (br > 0) quadraticTo(w, h, w - br, h) else lineTo(w, h)
            lineTo(bl, h)
            if (bl > 0) quadraticTo(0f, h, 0f, h - bl) else lineTo(0f, h)
            close()
        })
}

// ========== Shared icon circle ==========

@Composable
private fun WidgetIconCircle(icon: Painter, accentColor: Color) {
    Box(
        modifier = Modifier
            .size(CircleRadius * 2)
            .circleGlow(color = accentColor.copy(alpha = 0.1f), radius = 10.dp)
            .clip(CircleShape)
            .glassBackground(
                accentColor = accentColor,
                baseColor   = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
            )
            .glassBorder(shape = CircleShape, color = accentColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter            = icon,
            contentDescription = null,
            tint               = accentColor.copy(alpha = 0.8f),
            modifier           = Modifier.size(24.dp)
        )
    }
}

// ========== Shared value row ==========

@Composable
private fun WidgetValueRow(
    value: String,
    unit: String = "",
    accentColor: Color,
) {
    val initialFontSize = MaterialTheme.typography.headlineMedium.fontSize
    var fontSize by remember(value) { mutableStateOf(initialFontSize) }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text     = value,
            style    = MaterialTheme.typography.headlineMedium.copy(fontSize = fontSize),
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alignByBaseline(),
            onTextLayout = { result ->
                if (result.hasVisualOverflow && fontSize > 18.sp) fontSize *= 0.9f
            }
        )
        if (unit.isNotEmpty()) {
            Text(
                text       = unit,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Thin,
                color      = MaterialTheme.colorScheme.outlineVariant,
                modifier   = Modifier.alignByBaseline()
            )
        }
    }
}

// ========== StatWidgetSmall ==========
// Half-width card. Placed in a 2-column Row by the caller

@Composable
fun StatWidgetSmall(
    title: String,
    value: String,
    unit: String = "",
    icon: Painter,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val titleEndPadding = CircleRadius * 2 - 6.dp
    val cutoutShape = InclusiveCutoutShape(
        circleRadius = CircleRadius, padding = CutoutPadding,
        topLeft = CornerRadius, bottomLeft = CornerRadius, bottomRight = CornerRadius,
    )

    Box(modifier = modifier.height(SmallCardHeight)) {

        // ========== Card body ==========
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(cutoutShape)
                .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainer)
                .glassBorder(cutoutShape, accentColor)
        )

        // ========== Content ==========
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
                    text     = title,
                    style    = MaterialTheme.typography.titleMedium,
                    lineHeight = 20.sp, // reduce vertical "padding" when wrapped
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?. let {
                    Text(
                        text  = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(alpha = 0.8f)
                    )
                }
            }

            WidgetValueRow(value = value, unit = unit, accentColor = accentColor)
        }

        // ========== Circle icon ==========
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            WidgetIconCircle(icon = icon, accentColor = accentColor)
        }
    }
}

// ========== StatWidgetMedium ==========
// Full-width card. Has a date label top-right and an optional secondary stat bottom-right

@Composable
fun StatWidgetMedium(
    title: String,
    subtitle: String,
    value: String,
    unit: String = "",
    icon: Painter,
    accentColor: Color,
    modifier: Modifier = Modifier,
    dateLabel: String? = null,
    trailingValue: String? = null,
    trailingUnit: String? = null,
) {
    val cutoutShape = InclusiveCutoutShape(
        circleRadius = CircleRadius, padding = CutoutPadding,
        topLeft = CornerRadius, bottomLeft = CornerRadius, bottomRight = CornerRadius,
    )

    Box(modifier = modifier.fillMaxWidth().height(MediumCardHeight)) {

        // ========== Card body ==========
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(cutoutShape)
                .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainer)
                .glassBorder(cutoutShape, accentColor)
        )

        // ========== Content ==========
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ContentHorizPadding)
                .padding(top = ContentTopPadding, bottom = ContentBottomPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ========== Top section ==========
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Main title
                    Text(
                        text     = title,
                        style    = MaterialTheme.typography.titleLarge,
                        fontFamily = gloock,
                        color    = accentColor.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Date label
                        dateLabel?. let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = accentColor
                            )
                        }

                        // Subtitle
                        Text(
                            text  = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                }
            }

            // ========== Bottom section ==========
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                WidgetValueRow(value = value, unit = unit, accentColor = accentColor)
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.alignByBaseline()
                            )
                            trailingUnit?. let { unit ->
                                Text(
                                    text = unit,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Thin,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.alignByBaseline()
                                )
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
        title       = "Tasks Completed",
        value       = totalTasks.toString(),
        unit        = "total",
        icon        = painterResource(R.drawable.ic_task_alt),
        accentColor = MaterialTheme.colorScheme.primary,
        modifier    = modifier
    )
}

@Composable
fun AceCompletionCard(aceCount: Int, totalTasks: Int, modifier: Modifier = Modifier) {
    val pct = if (totalTasks > 0) (aceCount * 100) / totalTasks else 0
    StatWidgetSmall(
        title       = "Ace Completion",
        value       = "$pct%",
        unit        = "ratio",
        icon        = painterResource(R.drawable.ic_ace),
        accentColor = AceGoldDim,
        subtitle    = "$aceCount ace tasks",
        modifier    = modifier
    )
}

@Composable
fun TotalXpCard(totalXp: Int, modifier: Modifier = Modifier) {
    StatWidgetSmall(
        title       = "Total XP",
        value       = totalXp.toString(),
        unit        = "xp",
        icon        = painterResource(R.drawable.ic_xp),
        accentColor = XpVioletColor,
        modifier    = modifier
    )
}

@Composable
fun StreakCard(activityData: List<DailyActivity>, modifier: Modifier = Modifier) {
    val recordDays = computeRecordStreak(activityData)
    StatWidgetSmall(
        title       = "Streak Record",
        value       = recordDays.toString(),
        unit        = if (recordDays == 1) "day" else "days",
        icon        = painterResource(R.drawable.ic_fire),
        accentColor = StreakFireColor,
        modifier    = modifier
    )
}

@Composable
fun TopPerformanceCard(
    bestDay: DailyActivity?,
    modifier: Modifier = Modifier,
) {
//    val best  = topPerformanceDay.maxByOrNull { it.xpEarned }
    val xp    = bestDay?.xpEarned ?: 0
    val tasks = bestDay?.tasksCompleted ?: 0
    val date  = bestDay?.let {
        LocalDate.ofEpochDay(it.dateEpochDay).format(DateFormatter)
    }

    StatWidgetMedium(
        title         = "Top Performance",
        subtitle      = "your most productive day ever",
        value         = xp.toString(),
        unit          = "xp",
        icon          = painterResource(R.drawable.ic_top_performance),
        accentColor   = AceGoldDim,
        dateLabel     = date,
        trailingValue = tasks.toString(),
        trailingUnit  = if (tasks == 1) "task" else "tasks",
        modifier      = modifier
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
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TotalTasksCard(totalTasks = 142, modifier = Modifier.weight(1f))
                        AceCompletionCard(aceCount = 89, totalTasks = 142, modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TotalXpCard(totalXp = 14250, modifier = Modifier.weight(1f))
                        StreakCard(activityData = emptyList(), modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
