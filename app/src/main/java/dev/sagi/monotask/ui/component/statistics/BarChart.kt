package dev.sagi.monotask.ui.component.statistics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.domain.util.ActivityStats.ChartPoint
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ========== Design constants ==========

private val BarWidth              = 32.dp
private val BarCornerRadius       = 10.dp
private val ChartHeight           = 140.dp
private val TooltipClearance      = 30.dp

private const val MaxSelectedFraction = 0.82f
private const val BarActiveAlpha      = 1.0f
private const val BarTodayAlpha       = 0.5f
private const val StripeLineAlpha     = 0.22f
private val BarGlowBlur           = 12.dp
private const val BarGlowAlpha    = 0.35f

// ========== BarChart ==========

@Composable
fun BarChart(
    title: String,
    headline: String,
    points: List<ChartPoint>,
    trendPercent: Int,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    animate: Boolean = true,
) {
    val textColor  = MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)

    StatCard(
        modifier = modifier,
        title    = title,
        headline = headline,
        badge    = if (trendPercent != 0) {{ TrendBadge(trendPercent) }} else null
    ) {

        // ========== Bars ==========
        BarChartContent(
            points   = points,
            barColor = barColor,
            animate  = animate,
            modifier = Modifier.fillMaxWidth().height(ChartHeight)
        )

        // ========== X-axis labels ==========
        Row(modifier = Modifier.fillMaxWidth()) {
            points.forEach { point ->
                Text(
                    text      = point.label,
                    style     = MaterialTheme.typography.labelSmall,
                    color     = labelColor,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.weight(1f)
                )
            }
        }
    }
}


// ========== BarChartContent ==========

@Composable
private fun BarChartContent(
    points: List<ChartPoint>,
    barColor: Color,
    animate: Boolean,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return

    val todayIndex    = points.size - 1
    val maxV          = points.maxOf { it.value }.takeIf { it > 0f } ?: 1f
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val animProgress = remember(points) {
        points.map { Animatable(if (animate) 0f else 1f) }
    }

    LaunchedEffect(animate, points) {
        if (animate) {
            animProgress.forEach { it.snapTo(0f) }
            points.indices.forEach { i ->
                launch {
                    delay(i * 60L)
                    animProgress[i].animateTo(1f, tween(700, easing = FastOutSlowInEasing))
                }
            }
        } else {
            animProgress.forEach { it.snapTo(1f) }
        }
    }

    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Bottom
    ) {
        points.forEachIndexed { index, point ->
            val isSelected  = index == selectedIndex
            val isToday     = index == todayIndex
            val rawFraction = (point.value / maxV) * animProgress[index].value
            val barFraction = when {
                isSelected -> rawFraction.coerceAtMost(MaxSelectedFraction)
                else       -> rawFraction
            }.coerceAtLeast(if (point.value > 0f) 0.03f else 0f)
            val barHeight   = ChartHeight * barFraction

            val selectionAnim by animateFloatAsState(
                targetValue   = if (isSelected) 1f else 0f,
                animationSpec = tween(250, easing = FastOutSlowInEasing),
                label         = "barSelect_$index"
            )

            Box(
                modifier         = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
                SingleBar(
                    selectionAnim = selectionAnim,
                    isToday       = isToday,
                    barHeight     = barHeight,
                    barColor      = barColor,
                    onClick       = { selectedIndex = if (selectedIndex == index) -1 else index }
                )

                if (point.value > 0f && selectionAnim > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = -(barHeight + TooltipClearance - 20.dp))
                            .graphicsLayer(alpha = selectionAnim)
                    ) {
                        BarTooltip(point = point, barColor = barColor)
                    }
                }
            }
        }
    }
}

// ========== SingleBar ==========

@Composable
private fun SingleBar(
    selectionAnim: Float,
    isToday: Boolean,
    barHeight: Dp,
    barColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(BarWidth)
            .height(barHeight)
            .clip(RoundedCornerShape(BarCornerRadius))
            .drawBehind {
                when {
                    isToday -> {
                        drawRect(barColor.copy(alpha = BarTodayAlpha))
                        if (selectionAnim > 0f)
                            drawRect(barColor.copy(alpha = BarActiveAlpha * selectionAnim))
                    }
                    else -> {
                        clipRect(0f, 0f, size.width, size.height) {
                            rotate(degrees = 45f, pivot = Offset(size.width / 2f, size.height / 2f)) {
                                val gap  = 3.dp.toPx()
                                var x    = -(size.width + size.height)
                                val endX = size.width + size.height
                                while (x < endX) {
                                    drawLine(
                                        color       = barColor.copy(alpha = StripeLineAlpha),
                                        start       = Offset(x, -size.height),
                                        end         = Offset(x, size.height * 2),
                                        strokeWidth = gap
                                    )
                                    x += gap * 2
                                }
                            }
                        }
                        if (selectionAnim > 0f) {
                            if (selectionAnim > 0.5f) {
                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawRoundRect(
                                        0f, 0f, size.width, size.height,
                                        BarCornerRadius.toPx(), BarCornerRadius.toPx(),
                                        android.graphics.Paint().apply {
                                            color       = barColor.copy(alpha = BarGlowAlpha * selectionAnim).toArgb()
                                            maskFilter  = android.graphics.BlurMaskFilter(
                                                BarGlowBlur.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL
                                            )
                                            isAntiAlias = true
                                        }
                                    )
                                }
                            }
                            drawRect(barColor.copy(alpha = BarActiveAlpha * selectionAnim))
                        }
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
    )
}

// ========== BarTooltip ==========

@Composable
private fun BarTooltip(point: ChartPoint, barColor: Color) {
    Text(
        text       = "${point.value.toInt()}",
        style      = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color      = barColor
    )
}

// ========== Preview ==========

@Preview(showBackground = true)
@Composable
private fun BarChartPreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            BarChart(
                title        = "Tasks this week",
                headline     = "23 completed",
                trendPercent = 42,
                points       = listOf(
                    ChartPoint(3f, "Sat"), ChartPoint(0f, "Sun"),
                    ChartPoint(5f, "Mon"), ChartPoint(2f, "Tue"),
                    ChartPoint(7f, "Wed"), ChartPoint(4f, "Thu"),
                    ChartPoint(2f, "Fri"),
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
