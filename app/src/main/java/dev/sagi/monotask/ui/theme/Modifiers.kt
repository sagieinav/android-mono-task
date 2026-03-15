package dev.sagi.monotask.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


// ========== WRAPPER: MonoTask's basic elevated, bordered design ==========
fun Modifier.basicMonoTask(shape: Shape): Modifier = composed {
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
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.TRANSPARENT
            setShadowLayer(blur.toPx(), offsetX.toPx(), offsetY.toPx(), color.toArgb())
        }
        val path = shape.createOutline(size, layoutDirection, this).toAndroidPath()

        canvas.nativeCanvas.save()
        canvas.nativeCanvas.clipOutPath(path) // block anything inside the shape
        canvas.nativeCanvas.drawPath(path, paint)
        canvas.nativeCanvas.restore()
    }
}

private fun Outline.toAndroidPath(): android.graphics.Path = when (this) {
    is Outline.Rectangle -> android.graphics.Path().apply {
        addRect(rect.left, rect.top, rect.right, rect.bottom, android.graphics.Path.Direction.CW)
    }
    is Outline.Rounded -> android.graphics.Path().apply {
        addRoundRect(
            android.graphics.RectF(roundRect.left, roundRect.top, roundRect.right, roundRect.bottom),
            floatArrayOf(
                roundRect.topLeftCornerRadius.x,     roundRect.topLeftCornerRadius.y,
                roundRect.topRightCornerRadius.x,    roundRect.topRightCornerRadius.y,
                roundRect.bottomRightCornerRadius.x, roundRect.bottomRightCornerRadius.y,
                roundRect.bottomLeftCornerRadius.x,  roundRect.bottomLeftCornerRadius.y,
            ),
            android.graphics.Path.Direction.CW
        )
    }
    is Outline.Generic -> path.asAndroidPath()
}


fun Modifier.monoShadow(shape: Shape): Modifier = composed {
    this
        .shadow(
            elevation = 4.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.3f)
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

fun Modifier.glassBorder(
    shape: Shape,
    color: Color? = null,
    borderWidth: Dp = 2.dp
): Modifier = composed {
    val innerWidth = borderWidth * 0.75f
    val outerWidth = borderWidth * 0.25f

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


fun Modifier.circleGlow(
    color: Color,
    radius: Dp = 10.dp,
    offsetY: Dp = 0.dp
) = this.drawBehind {
    if (color.alpha < 0.05f) return@drawBehind
    drawIntoCanvas { canvas ->
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
            size.width / 2f,
            paint
        )
    }
}
