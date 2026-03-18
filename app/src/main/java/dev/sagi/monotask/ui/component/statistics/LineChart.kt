package dev.sagi.monotask.ui.component.statistics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.domain.util.ActivityStats.ChartPoint
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.theme.AceGold
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import kotlin.math.roundToInt

// ========== Design constants ==========

private val LineWidth             = 3.dp
private val GuidLineWidth         = 2.dp
private val GlowStrokeWidth       = 10.dp
private val GlowBlurRadius        = 8.dp
private const val GlowAlpha       = 0.25f
private const val GradientTopAlpha    = 0.4f
private const val GradientBottomAlpha = 0.07f
private val DotHaloRadius         = 12.dp
private val DotFillRadius         = 7.dp
private val DotCenterRadius       = 3.5.dp
private const val DotHaloAlpha    = 0.35f
private val TouchBottomBuffer     = 28.dp

// ========== Models ==========

private data class CachedChartData(
    val width: Float,
    val offsets: List<Offset>,
    val fullPath: Path,
    val pm: PathMeasure,
)

private data class SelectedPoint(
    val label: String,
    val value: Float,
    val screenPosition: Offset
)

// ========== LineChart ==========

@Composable
fun LineChart(
    title: String,
    headline: String,
    points: List<ChartPoint>,
    trendPercent: Int,
    modifier: Modifier = Modifier,
    lineColor: Color = AceGold,
    animate: Boolean = true,
) {
    val gridColor  = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val textColor  = MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)

    StatCard(
        modifier = modifier,
        title    = title,
        headline = headline,
        badge    = if (trendPercent != 0) {{ TrendBadge(trendPercent) }} else null
    ) {

        // ========== Chart ==========
        val chartHeight = 140.dp

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (points.isNotEmpty()) {
                val minV = points.minOf { it.value }
                val maxV = points.maxOf { it.value }
                Column(
                    modifier            = Modifier.height(chartHeight),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(maxV, (minV + maxV) / 2f, minV).forEach { v ->
                        Text(
                            text  = "${v.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                ChartCanvas(
                    points     = points,
                    lineColor  = lineColor,
                    gridColor  = gridColor,
                    animate    = animate,
                    drawHeight = chartHeight,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .height(chartHeight + TouchBottomBuffer)
                )

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
    }
}
// ========== ChartCanvas ==========

@Composable
private fun ChartCanvas(
    points: List<ChartPoint>,
    lineColor: Color,
    gridColor: Color,
    animate: Boolean,
    drawHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val animProgress = remember { Animatable(if (animate) 0f else 1f) }
    var userProgress by remember { mutableFloatStateOf(-1f) }
    var selectedPoint by remember { mutableStateOf<SelectedPoint?>(null) }
    val density = LocalDensity.current
    val drawHeightPx = with(density) { drawHeight.toPx() }

    // Cached path data. Rebuilt only when points or canvas size change, not every animation frame
    val cachedRef = remember { mutableStateOf<CachedChartData?>(null) }

    LaunchedEffect(animate, points) {
        if (animate) {
            animProgress.snapTo(0f)
            animProgress.animateTo(1f, animationSpec = tween(1500, easing = FastOutSlowInEasing))
        } else {
            animProgress.snapTo(1f)
        }
    }

    val currentProgress = if (userProgress >= 0f) userProgress else animProgress.value

    fun resolveCache(w: Float): CachedChartData? {
        cachedRef.value?.let { if (it.width == w) return it }
        if (points.size < 2) return null
        val minV    = points.minOf { it.value }
        val maxV    = points.maxOf { it.value }
        val range   = maxV - minV
        val offsets = points.mapIndexed { i, p ->
            Offset(
                x = (i.toFloat() / (points.size - 1)) * w,
                y = drawHeightPx * (1f - if (range > 0f) (p.value - minV) / range else 0.5f)
            )
        }
        val path = buildSmoothPath(offsets)
        val pm   = PathMeasure().also { it.setPath(path, false) }
        return CachedChartData(w, offsets, path, pm).also { cachedRef.value = it }
    }

    fun updateSelection(tapX: Float, width: Float) {
        val cache    = resolveCache(width) ?: return
        val progress = (tapX / width).coerceIn(0f, 1f)
        val idx      = (progress * (points.size - 1)).roundToInt().coerceIn(0, points.size - 1)
        userProgress  = progress
        selectedPoint = SelectedPoint(
            label          = points[idx].label,
            value          = interpolateAt(points, progress),
            screenPosition = cache.pm.let { pm ->
                pm.getPosition(pm.length * progress.coerceIn(0f, 1f))
            }
        )
    }

    // Invalidate cache when points change
    LaunchedEffect(points) { cachedRef.value = null }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        updateSelection(offset.x, size.width.toFloat())
                    }
                }
                .pointerInput(points) {
                    detectDragGestures(
                        onDragStart  = { offset -> updateSelection(offset.x, size.width.toFloat()) },
                        onDrag       = { change, _ ->
                            change.consume()
                            updateSelection(change.position.x, size.width.toFloat())
                        },
                        onDragEnd    = {},
                        onDragCancel = {}
                    )
                }
                .drawWithContent {
                    val w = size.width
                    val h = drawHeightPx

                    repeat(3) { i ->
                        val y = h * ((i + 1f) / 4f)
                        drawLine(
                            color       = gridColor,
                            start       = Offset(0f, y),
                            end         = Offset(w, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect  = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                    }

                    val cache = resolveCache(w)
                    if (cache == null) { drawContent(); return@drawWithContent }

                    drawPath(cache.fullPath, gridColor, style = Stroke(GuidLineWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                    if (currentProgress <= 0f) { drawContent(); return@drawWithContent }

                    val animPath = getAnimatedPath(cache.fullPath, cache.pm, currentProgress)
                    drawFillGradient(cache.fullPath, cache.pm, currentProgress, h, cache.offsets.first().x, lineColor)
                    drawLineGlow(animPath, lineColor)
                    drawPath(animPath, lineColor, style = Stroke(LineWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    val endPoint = cache.pm.let { it.getPosition(it.length * currentProgress.coerceIn(0f, 1f)) }
                    drawEndpointIndicator(endPoint, lineColor)

                    drawContent()
                }
        )

        // ========== Tooltip ==========
        selectedPoint?.let { point ->
            val offsetX = with(density) { point.screenPosition.x.toDp() - 20.dp }
            val offsetY = with(density) { point.screenPosition.y.toDp() - 60.dp }

            GlassSurface(
                modifier    = Modifier.offset(x = offsetX, y = offsetY),
                shape       = MaterialTheme.shapes.small,
                accentColor = lineColor.copy(alpha = 0.5f),
                baseColor   = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
                blurred     = false
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = point.label, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${point.value.toInt()}", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = lineColor)
                }
            }
        }
    }
}

// ========== Drawing helpers ==========
private fun interpolateAt(data: List<ChartPoint>, progress: Float): Float {
    if (data.size <= 1) return data.firstOrNull()?.value ?: 0f
    val exact = progress * (data.size - 1)
    val lo    = exact.toInt().coerceIn(0, data.size - 2)
    return data[lo].value + (data[lo + 1].value - data[lo].value) * (exact - lo)
}
private fun buildSmoothPath(pts: List<Offset>): Path = Path().apply {
    if (pts.isEmpty()) return@apply
    moveTo(pts[0].x, pts[0].y)
    for (i in 1 until pts.size) {
        val prev = pts[i - 1]; val curr = pts[i]
        val cx = (prev.x + curr.x) / 2f
        cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
    }
}

private fun getAnimatedPath(full: Path, pm: PathMeasure, progress: Float): Path {
    if (progress >= 1f) return full
    if (progress <= 0f) return Path()
    return Path().also { pm.getSegment(0f, pm.length * progress, it, true) }
}

private fun DrawScope.drawFillGradient(
    full: Path, pm: PathMeasure, progress: Float, height: Float, firstX: Float, color: Color
) {
    val target = pm.length * progress
    val path   = Path().apply {
        var started = false
        for (i in 0..50) {
            val pos = pm.getPosition((target * i / 50f).coerceAtMost(target))
            if (!started) { moveTo(pos.x, pos.y); started = true } else lineTo(pos.x, pos.y)
        }
        lineTo(pm.getPosition(target).x, height)
        lineTo(firstX, height)
        close()
    }
    drawPath(
        path  = path,
        brush = Brush.verticalGradient(
            listOf(color.copy(alpha = GradientTopAlpha), color.copy(alpha = GradientBottomAlpha), Color.Transparent)
        ),
        style = Fill
    )
}

private fun DrawScope.drawLineGlow(path: Path, color: Color) {
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawPath(
            path.asAndroidPath(),
            android.graphics.Paint().apply {
                this.color  = color.copy(alpha = GlowAlpha).toArgb()
                strokeWidth = GlowStrokeWidth.toPx()
                style       = android.graphics.Paint.Style.STROKE
                strokeCap   = android.graphics.Paint.Cap.ROUND
                maskFilter  = android.graphics.BlurMaskFilter(GlowBlurRadius.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                isAntiAlias = true
            }
        )
    }
}

private fun DrawScope.drawEndpointIndicator(center: Offset, color: Color) {
    drawCircle(color.copy(alpha = DotHaloAlpha), DotHaloRadius.toPx(),   center)
    drawCircle(color,                            DotFillRadius.toPx(),   center)
    drawCircle(Color.White,                      DotCenterRadius.toPx(), center)
}

// ========== Preview ==========

@Preview(showBackground = true)
@Composable
private fun LineChartPreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            LineChart(
                title        = "XP Activity this week",
                headline     = "72340 XP",
                trendPercent = 42,
                points       = listOf(
                    ChartPoint(120f, "Sat"), ChartPoint(0f,   "Sun"),
                    ChartPoint(310f, "Mon"), ChartPoint(85f,  "Tue"),
                    ChartPoint(460f, "Wed"), ChartPoint(230f, "Thu"),
                    ChartPoint(150f, "Fri"),
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
