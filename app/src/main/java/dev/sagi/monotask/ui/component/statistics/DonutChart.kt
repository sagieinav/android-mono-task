package dev.sagi.monotask.ui.component.statistics

import android.graphics.RectF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors
import kotlin.math.PI
import androidx.compose.foundation.Canvas as ComposeCanvas

// ========== Design constants ==========

private val DefaultRingSize = 120.dp
private val StrokeWidth     = 16.dp
private val SegmentGap      = 3.dp
private val GlowRadius      = 10.dp
private val glowAlpha       = 0.5f

// ========== Public model ==========

data class DonutSegment(
    val label: String,
    val value: Float,
    val color: Color? = null,
)

private val SegmentPalette = listOf(
    Color(0xFF3B82F6),  // blue
    Color(0xFF8B5CF6),  // purple
    Color(0xFF2DD4BF),  // teal
    Color(0xFFF59E0B),  // amber
    Color(0xFFEF4444),  // red
    Color(0xFF10B981),  // green
    Color(0xFFF97316),  // orange
    Color(0xFFEC4899),  // pink
)

// ========== DonutItem ==========
// Self-sized to ringSize. Legend width matches ring width automatically via fillMaxWidth()
// constrained by the parent Column's fixed width(ringSize).

@Composable
fun DonutItem(
    title: String,
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    ringSize: Dp = DefaultRingSize,
    animate: Boolean = true,
) {
    // handle segments with no provided colors
    val colors = MaterialTheme.customColors
    val resolvedColors: List<Color> = segments.mapIndexed { i, segment ->
        segment.color ?: colors.chartColors[i % colors.chartColors.size]
    }

    val total       = segments.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    val percentages = segments.map { it.value / total }


    val animProgress = remember(segments) { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(animate, segments) {
        if (animate) {
            animProgress.snapTo(0f)
            animProgress.animateTo(1f, tween(900, easing = EaseInCubic))
        } else {
            animProgress.snapTo(1f)
        }
    }

    // The donut itself
    val density    = LocalDensity.current
    val glowPaints = remember(segments, resolvedColors, density) {
        val strokePx = with(density) { StrokeWidth.toPx() }
        val glowPx   = with(density) { GlowRadius.toPx() }
        resolvedColors.map { color ->
            android.graphics.Paint().apply {
                isAntiAlias = true
                this.color  = android.graphics.Color.TRANSPARENT
                style       = android.graphics.Paint.Style.STROKE
                strokeWidth = strokePx
                strokeCap   = android.graphics.Paint.Cap.ROUND
                setShadowLayer(glowPx, 0f, 0f, color.copy(glowAlpha).toArgb())
            }
        }
    }

    val labelColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    val textColor  = MaterialTheme.colorScheme.onSurface

    Column(
        modifier            = modifier.width(ringSize),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ========== Ring ==========
        Box(
            modifier = Modifier
                .size(ringSize)
                .drawBehind {
                    val strokePx = StrokeWidth.toPx()
                    val radius   = (ringSize.toPx() - strokePx) / 2f
                    val cx       = size.width / 2f
                    val cy       = size.height / 2f
                    val rect     = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
                    val gapAngle = (SegmentGap.toPx() / (2.0 * PI * radius) * 360.0).toFloat()
                    var start    = -90f
                    val progress = animProgress.value

                    segments.forEachIndexed { i, _ ->
                        val pct   = percentages[i]
                        val sweep = ((pct * 360f) - gapAngle) * progress
                        if (sweep > 0f) {
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawArc(rect, start + gapAngle / 2f, sweep, false, glowPaints[i])
                            }
                        }
                        start += pct * 360f
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            ComposeCanvas(modifier = Modifier.requiredSize(ringSize)) {
                drawRingSegments(
                    percentages,
                    segments,
                    resolvedColors,
                    animProgress.value,
                    StrokeWidth.toPx())
            }

            Text(
                text  = title,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
        }

        // ========== Legend ==========
        Column(
            modifier            = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            segments.forEachIndexed { i, segment ->
                val color = resolvedColors[i]
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
                    Text(
                        text     = segment.label,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text       = "${(percentages[i] * 100).toInt()}%",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = color
                    )
                }
            }
        }
    }
}

// ========== DonutRowCard ==========

@Composable
fun DonutRowCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    StatCard(modifier = modifier, title = title) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top
        ) {
            content()
        }
    }
}

// ========== Drawing helper ==========

private fun DrawScope.drawRingSegments(
    percentages: List<Float>,
    segments: List<DonutSegment>,
    resolvedColors: List<Color>,
    progress: Float,
    strokeWidth: Float,
) {
    val radius     = (size.minDimension - strokeWidth) / 2f
    val center     = Offset(size.width / 2f, size.height / 2f)
    val gapAngle   = (SegmentGap.toPx() / (2.0 * PI * radius) * 360.0).toFloat()
    var startAngle = -90f

    segments.forEachIndexed { i, segment ->
        val color = resolvedColors[i]
        val pct        = percentages[i]
        val sweepAngle = ((pct * 360f) - gapAngle) * progress
        if (sweepAngle > 0f) {
            drawArc(
                color      = color,
                startAngle = startAngle + gapAngle / 2f,
                sweepAngle = sweepAngle,
                useCenter  = false,
                topLeft    = Offset(center.x - radius, center.y - radius),
                size       = Size(radius * 2f, radius * 2f),
                style      = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        startAngle += pct * 360f
    }
}

// ========== Preview ==========

@Preview(showBackground = true)
@Composable
private fun DonutRowCardPreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            DonutRowCard(
                title    = "Task Completion Distribution",
                modifier = Modifier.padding(16.dp)
            ) {
                DonutItem(
                    title    = "Importance",
                    segments = listOf(
                        DonutSegment("High",   12f, Color(0xFFEF4444)),
                        DonutSegment("Medium", 25f, Color(0xFFF59E0B)),
                        DonutSegment("Low",     8f, Color(0xFF6B7280)),
                    )
                )
                DonutItem(
                    title    = "Workspace",
                    segments = listOf(
                        DonutSegment("Work",     20f, Color(0xFF3B82F6)),
                        DonutSegment("Personal", 15f, Color(0xFF8B5CF6)),
                        DonutSegment("Health",   10f, Color(0xFF2DD4BF)),
                    )
                )
            }
        }
    }
}
