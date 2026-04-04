package dev.sagi.monotask.ui.focus.component

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.SweepGradient
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.component.GlassSurface
import dev.sagi.monotask.designsystem.theme.AceGold
import dev.sagi.monotask.designsystem.theme.AceGoldDim
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import kotlin.intArrayOf

// aceTaskBorder: gold rotating gradient for ACE-importance tasks
// defaultTaskBorder: subtle static outline for normal tasks


// ========== Shared Border Drawing Internals ==========

private const val FADE_STEPS = 10
private val BORDER_WIDTH = 5.dp

// One instance per modifier. All paths and measure reused across frames
private class BorderDrawCache {
    val fullPath  = Path()
    val bodyPath  = Path()
    val stepPaths = Array(FADE_STEPS) { Path() }
    val measure   = PathMeasure()
}

private fun DrawScope.drawOutlineBorder(
    outline: Outline,
    expand: Float,
    paint: Paint,
    cache: BorderDrawCache,
    fraction: Float = 1f,
    fadeTailFraction: Float = 0.1f
) {
    if (fraction <= 0f) return

    val tailFadeEnd = 1f + fadeTailFraction  // 1.1f

    cache.fullPath.rewind()
    when (outline) {
        is Outline.Rectangle -> {
            val r = outline.rect
            cache.fullPath.addRect(
                r.left - expand, r.top - expand,
                r.right + expand, r.bottom + expand,
                Path.Direction.CW
            )
        }
        is Outline.Rounded -> {
            val rr = outline.roundRect
            cache.fullPath.addRoundRect(
                RectF(rr.left - expand, rr.top - expand, rr.right + expand, rr.bottom + expand),
                floatArrayOf(
                    rr.topLeftCornerRadius.x + expand, rr.topLeftCornerRadius.y + expand,
                    rr.topRightCornerRadius.x + expand, rr.topRightCornerRadius.y + expand,
                    rr.bottomRightCornerRadius.x + expand, rr.bottomRightCornerRadius.y + expand,
                    rr.bottomLeftCornerRadius.x + expand, rr.bottomLeftCornerRadius.y + expand
                ),
                Path.Direction.CW
            )
        }
        is Outline.Generic -> cache.fullPath.set(outline.path.asAndroidPath())
    }

    if (fraction >= tailFadeEnd) {
        drawContext.canvas.nativeCanvas.drawPath(cache.fullPath, paint)
        return
    }

    cache.measure.setPath(cache.fullPath, false)   // reuse, no new PathMeasure
    val totalLength   = cache.measure.length
    val endLength     = totalLength * fraction.coerceAtMost(1f)
    val fadeTailLen   = (totalLength * fadeTailFraction).coerceAtMost(endLength)
    val originalAlpha = paint.alpha
    val tailAlpha     = if (fraction <= 1f) 1f else (tailFadeEnd - fraction) / fadeTailFraction

    if (endLength > fadeTailLen) {
        cache.bodyPath.rewind()
        cache.measure.getSegment(fadeTailLen, endLength, cache.bodyPath, true)
        paint.alpha = originalAlpha
        drawContext.canvas.nativeCanvas.drawPath(cache.bodyPath, paint)
    }

    for (i in 0 until FADE_STEPS) {
        cache.stepPaths[i].rewind()
        if (cache.measure.getSegment(
                (i.toFloat() / FADE_STEPS) * fadeTailLen,
                ((i + 1f)    / FADE_STEPS) * fadeTailLen,
                cache.stepPaths[i], true
            )) {
            paint.alpha = (if (fraction <= 1f) {
                originalAlpha * (i + 1f) / FADE_STEPS
            } else {
                originalAlpha * (1f - tailAlpha * (1f - (i + 1f) / FADE_STEPS))
            }).toInt().coerceIn(0, 255)
            drawContext.canvas.nativeCanvas.drawPath(cache.stepPaths[i], paint)
        }
    }

    paint.alpha = originalAlpha
}


// ========== ACE Task Border (Animated Gold Gradient) ==========

@Composable
fun Modifier.aceTaskBorder(
    shape: Shape = MaterialTheme.shapes.medium,
    borderWidth: Dp = BORDER_WIDTH,
    glassInnerWidth: Dp = 1.5.dp,
    glassOuterWidth: Dp = 1.dp,
    drawFraction: Float = 1f
): Modifier = composed {
    val rotation by rememberInfiniteTransition(label = "aceGlow").animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(animation = tween(5000, easing = LinearEasing)),
        label         = "aceRotation"
    )

    val density           = LocalDensity.current
    val borderWidthPx     = remember(borderWidth, density)     { with(density) { borderWidth.toPx() } }
    val glassInnerWidthPx = remember(glassInnerWidth, density) { with(density) { glassInnerWidth.toPx() } }
    val glassOuterWidthPx = remember(glassOuterWidth, density) { with(density) { glassOuterWidth.toPx() } }

    // One cache shared across all 3 drawOutlineBorder calls
    val cache        = remember { BorderDrawCache() }
    val shaderMatrix = remember { Matrix() }

    // Shader rebuilt only on size change, not every frame
    val shaderHolder     = remember { arrayOfNulls<SweepGradient>(1) }
    val cachedSize       = remember { floatArrayOf(0f, 0f) }

    // Paints allocated once
    val outerGlassPaint = remember(glassOuterWidthPx) {
        Paint().apply {
            style       = Paint.Style.STROKE
            strokeWidth = glassOuterWidthPx
            isAntiAlias = true
            alpha       = 80
        }
    }
    val goldPaint = remember(borderWidthPx) {
        Paint().apply {
            style       = Paint.Style.STROKE
            strokeWidth = borderWidthPx
            isAntiAlias = true
        }
    }
    val innerGlassPaint = remember(glassInnerWidthPx) {
        Paint().apply {
            color       = Color.White.copy(alpha = 0.5f).toArgb()
            style       = Paint.Style.STROKE
            strokeWidth = glassInnerWidthPx
            isAntiAlias = true
        }
    }

    this.drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)

        // Rebuild shader only when component size changes (typically once)
        if (shaderHolder[0] == null
            || cachedSize[0] != size.width
            || cachedSize[1] != size.height
        ) {
            val s = SweepGradient(
                size.width / 2f, size.height / 2f,
                intArrayOf(
                    Color.White.copy(alpha = 0.9f).toArgb(),
                    AceGold.toArgb(),
                    AceGoldDim.toArgb(),
                    AceGold.toArgb(),
                    Color.White.copy(alpha = 0.9f).toArgb(),
                ),
                null
            )
            shaderHolder[0]     = s
            cachedSize[0]       = size.width
            cachedSize[1]       = size.height
            outerGlassPaint.shader = s
            goldPaint.shader       = s
            // innerGlassPaint is solid color, no shader
        }

        shaderMatrix.reset()
        shaderMatrix.postRotate(rotation, size.width / 2f, size.height / 2f)
        shaderHolder[0]!!.setLocalMatrix(shaderMatrix)

        drawOutlineBorder(outline, borderWidthPx + glassOuterWidthPx / 2f, outerGlassPaint, cache, drawFraction)
        drawOutlineBorder(outline, borderWidthPx / 2f,                      goldPaint,       cache, drawFraction)
        drawOutlineBorder(outline, -(glassInnerWidthPx / 2f),               innerGlassPaint, cache, drawFraction)
    }
}


// ========== Default Task Border (Static Outline) ==========

@Composable
fun Modifier.defaultTaskBorder(
    shape: Shape = MaterialTheme.shapes.medium,
    borderWidth: Dp = BORDER_WIDTH,
    drawFraction: Float = 1f
): Modifier = composed {
    val borderColor   = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val density       = LocalDensity.current
    val borderWidthPx = remember(borderWidth, density) { with(density) { borderWidth.toPx() } }
    val cache         = remember { BorderDrawCache() }
    val paint         = remember(borderColor, borderWidthPx) {
        Paint().apply {
            color       = borderColor.toArgb()
            style       = Paint.Style.STROKE
            strokeWidth = borderWidthPx
            isAntiAlias = true
        }
    }

    this.drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutlineBorder(outline, borderWidthPx / 2f, paint, cache, drawFraction)
    }
}


// ========== Preview ==========

@Preview(showBackground = true)
@Composable
private fun AceTaskBorderPreview() {
    MonoTaskTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            GlassSurface(
                blurred = false,
                modifier = Modifier
                    .padding(12.dp)
                    .aceTaskBorder()
            ) {
                Text(
                    text = "ACE Task",
                    modifier = Modifier.padding(46.dp)
                )
            }
        }
    }
}
