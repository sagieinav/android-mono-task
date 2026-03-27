package dev.sagi.monotask.ui.theme

import android.annotation.SuppressLint
import android.graphics.Path
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.time.delay
import kotlin.math.atan2



@SuppressLint("SuspiciousModifierThen")
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    clickable(
        interactionSource = MutableInteractionSource(),
        indication = null,
        onClick = onClick
    )
)




// Removes min size constraints from any composable
fun Modifier.noMinSize() = layout { measurable, constraints ->
    val placeable = measurable.measure(
        constraints.copy(minWidth = 0, minHeight = 0)
    )
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(0, 0)
    }
}


// ========== WRAPPER: MonoTask's basic elevated, bordered design ==========
fun Modifier.basicMonoTask(
    shape: Shape,
    elevation: Dp = 4.dp
): Modifier = composed {
    this
        .monoShadow(shape)
        .monoBorder(shape)
}


// Custom (workaround) drop shadow for use in semi-transparent components
fun Modifier.monoShadowWorkaround(
    shape: Shape,
    color: Color = Color.Black.copy(alpha = 0.04f),
    blur: Dp = 6.dp,
    offsetY: Dp = 2.dp,
    offsetX: Dp = 0.dp,
): Modifier = this.drawWithCache {
    // Calculate and cache objects ONLY when the layout size changes
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        this.color = android.graphics.Color.TRANSPARENT
        setShadowLayer(
            blur.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            color.toArgb()
        )
    }

    val path = shape.createOutline(size, layoutDirection, this).toAndroidPath()

    // The actual draw phase (runs every frame, but does no allocations)
    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.save()
            canvas.nativeCanvas.clipOutPath(path) // Block inside the shape
            canvas.nativeCanvas.drawPath(path, paint)
            canvas.nativeCanvas.restore()
        }
    }
}.clip(shape)

fun Modifier.monoShadow(
    shape: Shape,
    elevation: Dp = 4.dp,
    strength: Float = 1f
): Modifier = composed {
    this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.05f * strength),
            spotColor = Color.Black.copy(alpha = 0.35f * strength)
        )
}

fun Modifier.monoBorder(shape: Shape): Modifier = composed {
    this
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            shape = shape
        )
}

// This is more expensive to use. Mainly for big, noticeable components
fun Modifier.glassBorderPremium(
    shape: Shape,
    width: Dp = 2.dp
): Modifier = composed {
    val innerWidth = width * 1.5f
    val outerWidth = width * 0.5f

    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    drawWithContent {
        drawContent()

        val cornerFraction = (atan2(
            size.height / 2.0,
            size.width / 2.0
        ) / (2 * Math.PI)).toFloat()

        val bright = Color.White.copy(alpha = 0.6f)
        val dark   = Color.White.copy(alpha = 0.05f)

        val brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0f                      to bright,
                cornerFraction          to bright,
                (0.5f - cornerFraction) to dark,
                (0.5f + cornerFraction) to bright,
                (1f - cornerFraction)   to dark,
                1f                      to bright,
            ),
            center = Offset(size.width / 2f, size.height / 2f)
        )

        val outline = shape.createOutline(size, layoutDirection, this)

        // Inner glass stroke
        drawOutline(
            outline = outline,
            brush   = brush,
            style   = Stroke(width = innerWidth.toPx())
        )
        // Outer definition stroke
        drawOutline(
            outline = outline,
            brush   = SolidColor(outlineVariant.copy(alpha = 0.4f)),
            style   = Stroke(width = outerWidth.toPx())
        )
    }
}

fun Modifier.glassBorder(
    shape: Shape,
    color: Color? = null,
    width: Dp = 2.dp
): Modifier = composed {
    val innerWidth = width * 0.75f
    val outerWidth = width * 0.25f

    val innerModifier =
        if (color == null) {
            // Normal, non-colored:
            Modifier.border(innerWidth, Color.White.copy(alpha = 0.5f), shape)
        } else {
            // Colored:
            val brush = Brush.linearGradient(
                colors = listOf(
                    color.copy(alpha = 0.4f),
                    color.copy(alpha = 0.1f),
                    Color.Transparent,
                    Color.White.copy(alpha = 0.5f),
                )
            )
            Modifier.border(innerWidth, brush, shape)
        }

    val outerModifier =
        if (color == null) {
            // Normal, non-colored:
            Modifier.border(outerWidth, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), shape)
        } else {
            // Colored:
            val brush = Brush.linearGradient(
                colors = listOf(
                    color.copy(alpha = 0.4f),
                    color.copy(alpha = 0.1f),
                    Color.Transparent,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                )
            )
            Modifier.border(outerWidth, brush, shape)
        }

    // Apply the 2 borders
    this
        .then(innerModifier)
        .then(outerModifier)
}

fun Modifier.glassBackground(
    accentColor: Color? = null,
    baseColor: Color = Color.Transparent // solid base bg layer
): Modifier {
    val shineBrush = if (accentColor == null) {
        Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.3f),
                accentColor.copy(alpha = 0.05f),
                Color.Transparent,
                Color.White.copy(alpha = 0.4f)
            )
        )
    }
    return this
        .background(baseColor)      // solid base
        .background(shineBrush)     // glass shine on top
}


// "Invincible" border. used for readability mainly
fun Modifier.invincibleBorder(shape: Shape): Modifier = composed {
    this
        .border(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            shape = shape
        )
}


// circleRadius: the radius of the shadow circle drawn. Defaults to size.width/2 (fills the box).
// Pass a smaller value when the composable is intentionally larger than the content circle
// (e.g. to give the shadow room to render without being clipped by the RenderNode boundary).
fun Modifier.circleGlow(
    color: Color,
    radius: Dp = 10.dp,
    circleRadius: Dp? = null,
    offsetY: Dp = 0.dp
) = this.drawBehind {
    if (color.alpha < 0.05f) return@drawBehind
    drawIntoCanvas { canvas ->
        val shadowRadius = circleRadius?.toPx() ?: (size.width / 2f)
        val paint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.TRANSPARENT
            setShadowLayer(
                radius.toPx(),
                0f,
                offsetY.toPx(),
                color.toArgb()
            )
        }
        canvas.nativeCanvas.drawCircle(
            size.width / 2f,
            size.height / 2f,
            shadowRadius,
            paint
        )
    }
}

private fun Outline.toAndroidPath(): Path = when (this) {
    is Outline.Rectangle -> Path().apply {
        addRect(rect.left, rect.top, rect.right, rect.bottom, Path.Direction.CW)
    }
    is Outline.Rounded -> Path().apply {
        addRoundRect(
            android.graphics.RectF(roundRect.left, roundRect.top, roundRect.right, roundRect.bottom),
            floatArrayOf(
                roundRect.topLeftCornerRadius.x,     roundRect.topLeftCornerRadius.y,
                roundRect.topRightCornerRadius.x,    roundRect.topRightCornerRadius.y,
                roundRect.bottomRightCornerRadius.x, roundRect.bottomRightCornerRadius.y,
                roundRect.bottomLeftCornerRadius.x,  roundRect.bottomLeftCornerRadius.y,
            ),
            Path.Direction.CW
        )
    }
    is Outline.Generic -> path.asAndroidPath()
}
