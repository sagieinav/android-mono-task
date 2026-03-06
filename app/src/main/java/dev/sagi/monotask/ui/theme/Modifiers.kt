package dev.sagi.monotask.ui.theme

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.graphics.SweepGradient
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.graphics.toArgb
import dev.sagi.monotask.ui.component.core.GlassSurface
import kotlin.intArrayOf


// ========== MonoTask's basic elevated, bordered design ==========
fun Modifier.basicMonoTask(shape: Shape): Modifier = composed {
    this
        .shadow(
            elevation = 4.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.25f)
        )
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            shape = shape
        )
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

fun Modifier.glassBorder(shape: Shape): Modifier = composed {
    this
        // Inner "glass highlight" border
        .border(
            width = 1.5.dp,
            color = Color.White.copy(alpha = 0.5f),
            shape = shape
        )
        // Outer border
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            shape = shape
        )
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




// Private helper for AceTaskBorder
private fun DrawScope.drawOutlineBorder(
    outline: Outline,
    expand: Float, // positive = outside composable bounds, negative = inside
    paint: android.graphics.Paint
) {
    when (outline) {
        is Outline.Rectangle -> {
            drawContext.canvas.nativeCanvas.drawRect(
                -expand, -expand,
                size.width + expand, size.height + expand,
                paint
            )
        }
        is Outline.Rounded -> {
            val rr = outline.roundRect
            val radii = floatArrayOf(
                (rr.topLeftCornerRadius.x     + expand).coerceAtLeast(0f), (rr.topLeftCornerRadius.y     + expand).coerceAtLeast(0f),
                (rr.topRightCornerRadius.x    + expand).coerceAtLeast(0f), (rr.topRightCornerRadius.y    + expand).coerceAtLeast(0f),
                (rr.bottomRightCornerRadius.x + expand).coerceAtLeast(0f), (rr.bottomRightCornerRadius.y + expand).coerceAtLeast(0f),
                (rr.bottomLeftCornerRadius.x  + expand).coerceAtLeast(0f), (rr.bottomLeftCornerRadius.y  + expand).coerceAtLeast(0f),
            )
            drawContext.canvas.nativeCanvas.drawPath(
                Path().apply {
                    addRoundRect(
                        RectF(-expand, -expand, size.width + expand, size.height + expand),
                        radii,
                        Path.Direction.CW
                    )
                },
                paint
            )
        }
        is Outline.Generic -> {
            val scaledPath = Path()
            Matrix().apply {
                setScale(
                    (size.width  + expand * 2) / size.width,
                    (size.height + expand * 2) / size.height,
                    size.width / 2f, size.height / 2f
                )
            }.let { outline.path.asAndroidPath().transform(it, scaledPath) }
            drawContext.canvas.nativeCanvas.drawPath(scaledPath, paint)
        }
    }
}


// ========== Focus Task Borders ==========
@Composable
fun Modifier.aceTaskBorder(
    shape: Shape = MaterialTheme.shapes.medium,
    borderWidth: Dp = 6.dp,
    glassInnerWidth: Dp = 1.5.dp,
    glassOuterWidth: Dp = 1.dp
): Modifier = composed {
    val rotation by rememberInfiniteTransition(label = "aceGlow").animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(animation = tween(5000, easing = LinearEasing)),
        label = "aceRotation"
    )

    val glassInnerColor = Color.White.copy(alpha = 0.5f)

    this.drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)
        val halfStroke = borderWidth.toPx() / 2f

    // Shader
        val shader = SweepGradient(
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
        Matrix().also {
            it.postRotate(rotation, size.width / 2f, size.height / 2f)
            shader.setLocalMatrix(it)
        }

        // Layer 1: outer glass
        drawOutlineBorder(
            outline,
            expand = borderWidth.toPx() + glassOuterWidth.toPx() / 2f,
            paint = android.graphics.Paint().apply {
                this.shader = shader
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = glassOuterWidth.toPx()
                isAntiAlias = true
                alpha = 80   // 0–255, ~30% — keeps it subtle
            }
        )

        // Layer 2: gold sweep border
        drawOutlineBorder(
            outline,
            expand = halfStroke,
            paint = android.graphics.Paint().apply {
                this.shader = shader
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = borderWidth.toPx()
                isAntiAlias = true
                // full alpha — no override
            }
        )

        // Layer 3: inner glass highlight
        drawOutlineBorder(
            outline,
            expand = -(glassInnerWidth.toPx() / 2f),
            paint = android.graphics.Paint().apply {
                color = glassInnerColor.toArgb()
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = glassInnerWidth.toPx()
                isAntiAlias = true
            }
        )
    }
}



@Composable
fun Modifier.defaultTaskBorder(
    shape: Shape = MaterialTheme.shapes.medium,
    borderWidth: Dp = 6.dp
): Modifier = composed {
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    this.drawBehind {
        val paint = android.graphics.Paint().apply {
            color = borderColor.toArgb()
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = borderWidth.toPx()
            isAntiAlias = true
        }

        val halfStroke = borderWidth.toPx() / 2f
        val expandedSize = Size(
            size.width + borderWidth.toPx(),
            size.height + borderWidth.toPx()
        )

        val outline = shape.createOutline(expandedSize, layoutDirection, this)

        drawContext.canvas.nativeCanvas.save()
        drawContext.canvas.nativeCanvas.translate(-halfStroke, -halfStroke)

        when (outline) {
            is Outline.Rectangle -> {
                drawContext.canvas.nativeCanvas.drawRect(
                    outline.rect.toAndroidRectF(),
                    paint
                )
            }
            is Outline.Rounded -> {
                val rr = outline.roundRect
                drawContext.canvas.nativeCanvas.drawRoundRect(
                    rr.left, rr.top, rr.right, rr.bottom,
                    rr.topLeftCornerRadius.x, rr.topLeftCornerRadius.y,
                    paint
                )
            }
            is Outline.Generic -> {
                drawContext.canvas.nativeCanvas.drawPath(
                    outline.path.asAndroidPath(),
                    paint
                )
            }
        }

        drawContext.canvas.nativeCanvas.restore()
    }
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


@Preview(showBackground = true)
@Composable
fun aceTaskBorder() {
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

//            CustomTag(
//                "ace",
//                modifier = Modifier
//                    .aceGlowBorder(50.dp))
        }
    }
}
