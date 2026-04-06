package dev.sagi.monotask.designsystem.theme

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke

private const val OUTLINE_ALPHA = 0.14f


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


fun Modifier.monoShadow(
    shape: Shape,
    alpha: Float = 0.6f,
    elevation: Dp = 20.dp
): Modifier = this.shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = Color.Black.copy(alpha = alpha),
        spotColor = Color.Black.copy(alpha = 0.05f)
    )


// This is more expensive to use. Mainly for big, noticeable components
fun Modifier.glassBorderPremium(
    shape: Shape,
    width: Dp = 4.dp
): Modifier = this.drawWithCache {
        val innerWidth = width.toPx()
        val outerWidth = 0.5.dp.toPx()

        val outerColor = Color.Black.copy(alpha = OUTLINE_ALPHA)

        val cornerFraction = (atan2(
                size.height / 2.0,
                size.width / 2.0
            ) / (2 * PI)).toFloat()

        val bright = Color.White.copy(alpha = 0.8f)
        val dark = Color.White.copy(alpha = 0.05f)

        // Cached, only rebuilt when size changes
        val brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0f to bright,
                cornerFraction to bright,
                (0.5f - cornerFraction) to dark,
                (0.5f + cornerFraction) to bright,
                (1f - cornerFraction) to dark,
                1f to bright,
            ),
            center = Offset(size.width / 2f, size.height / 2f)
        )

        val outerBrush = SolidColor(outerColor)

        // Cached, only rebuilt when size changes
        val outline = shape.createOutline(size, layoutDirection, this)

        onDrawWithContent {
            drawContent()
            // Inner glass stroke
            drawOutline(
                outline = outline,
                brush = brush,
                style = Stroke(width = innerWidth)
            )
            // Outer definition stroke
            drawOutline(
                outline = outline,
                brush = outerBrush,
                style = Stroke(width = outerWidth)
            )
        }
    }


fun Modifier.glassBorder(
    shape: Shape,
    color: Color? = null,
    width: Dp = 1.5.dp
): Modifier {
    val innerWidth = width
    val outerWidth = 0.5.dp

    val outerColor = Color.Black.copy(alpha = OUTLINE_ALPHA)

    val innerBrush = if (color == null) {
        SolidColor(Color.White.copy(alpha = 0.5f))
    } else {
        Brush.linearGradient(
            colors = listOf(
                color.copy(alpha = 0.4f),
                color.copy(alpha = 0.1f),
                Color.Transparent,
                Color.White.copy(alpha = 0.5f),
            )
        )
    }

    val outerBrush = if (color == null) {
        SolidColor(outerColor)
    } else {
        Brush.linearGradient(
            colors = listOf(
                color.copy(alpha = 0.4f),
                color.copy(alpha = 0.1f),
                Color.Transparent,
                outerColor,
            )
        )
    }

    // Chain the borders
    return this
        .border(width = innerWidth, brush = innerBrush, shape = shape)
        .border(width = outerWidth, brush = outerBrush, shape = shape)
}


fun Modifier.glassBackground(
    baseColor: Color = Color.Unspecified,
    accentColor: Color? = null,
    shineAlpha: Float = 0.6f
): Modifier = this.drawWithCache {
        val shineBrush = if (accentColor == null) {
            Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = shineAlpha), Color.Transparent)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.3f),
                    accentColor.copy(alpha = 0.05f),
                    Color.Transparent,
                    Color.White.copy(alpha = shineAlpha)
                )
            )
        }

        onDrawBehind {
            // Draw the solid base (if it exists)
            if (baseColor != Color.Unspecified && baseColor != Color.Transparent) {
                drawRect(color = baseColor)
            }
            // Draw the glass shine directly on top
            drawRect(brush = shineBrush)
        }
    }


// circleRadius: the radius of the shadow circle drawn. Defaults to size.width/2 (fills the box)
fun Modifier.circleGlow(
    color: Color,
    radius: Dp = 10.dp,
    circleRadius: Dp? = null,
    offsetY: Dp = 0.dp
): Modifier = this.drawWithCache {

    val radiusPx = radius.toPx()
    val offsetYPx = offsetY.toPx()
    val shadowColorArgb = color.toArgb()

    val nativePaint = Paint().apply {
        isAntiAlias = true
        // Use Android's native transparent color integer
        this.color = android.graphics.Color.TRANSPARENT
        setShadowLayer(
            radiusPx,
            0f, // offsetX
            offsetYPx,
            shadowColorArgb
        )
    }

    onDrawBehind {
        if (color.alpha < 0.05f) return@onDrawBehind

        val shadowRadiusPx = circleRadius?.toPx() ?: (size.width / 2f)

        // Draw directly to the native canvas using the cached native paint
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawCircle(
                size.width / 2f,
                size.height / 2f,
                shadowRadiusPx,
                nativePaint
            )
        }
    }
}
