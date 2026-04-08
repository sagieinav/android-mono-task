package dev.sagi.monotask.designsystem.theme

import android.graphics.Color.HSVToColor
import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.compose.animation.core.EaseInBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.components.GlassSurface

private val DefaultBorderWidth = 4.dp


// ========== Premium Border (Animated Metallic Sweep Gradient) ==========
fun Modifier.premiumBorder(
    shape: Shape,
    color: Color,
    borderWidth: Dp = DefaultBorderWidth,
    entryProgressProvider: () -> Float = { 1f }
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "premiumGlow")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(5000, easing = LinearEasing)),
        label = "premiumRotation"
    )

    this.drawWithCache {
        val maxBorderWidthPx = borderWidth.toPx()
        val shaderMatrix = Matrix()

        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        hsv[1] = (hsv[1] * 1.5f).coerceAtMost(1f)
        hsv[2] = hsv[2] * 0.9f

        val darkShadow = Color(HSVToColor(hsv))
        val brightHighlight = lerp(Color.White, color, 0.15f)

        val sweepGradient = SweepGradient(
            size.width / 2f, size.height / 2f,
            intArrayOf(
                brightHighlight.toArgb(), color.toArgb(), darkShadow.toArgb(),
                color.toArgb(), brightHighlight.toArgb(), color.toArgb(),
                darkShadow.toArgb(), color.toArgb(), brightHighlight.toArgb()
            ),
            null
        )
        val premiumBrush = ShaderBrush(sweepGradient)

        onDrawBehind {
            val progress = entryProgressProvider().coerceIn(0f, 1f)
            if (progress <= 0f) return@onDrawBehind

            val currentBorderWidthPx = maxBorderWidthPx * progress
            val outline = shape.createOutline(size, layoutDirection, this)

            val outerExpandAmount = currentBorderWidthPx / 2f
            val expandedOuterPath = outline.toExpandedPath(outerExpandAmount)

            shaderMatrix.reset()
            shaderMatrix.postRotate(rotation, size.width / 2f, size.height / 2f)
            sweepGradient.setLocalMatrix(shaderMatrix)

            drawPath(
                path = expandedOuterPath,
                brush = premiumBrush,
                style = Stroke(width = currentBorderWidthPx),
                alpha = progress
            )
        }
    }
}


// ========== Outline Border (Static) ==========
fun Modifier.outlineBorder(
    shape: Shape,
    borderColor: Color,
    borderWidth: Dp = DefaultBorderWidth,
    entryProgressProvider: () -> Float = { 1f }
): Modifier = this.drawWithCache {
    val maxBorderWidthPx = borderWidth.toPx()
    val baseColor = borderColor.copy(alpha = 0.3f)

    onDrawBehind {
        val progress = entryProgressProvider().coerceIn(0f, 1f)
        if (progress <= 0f) return@onDrawBehind

        val currentBorderWidthPx = maxBorderWidthPx * progress
        val outline = shape.createOutline(size, layoutDirection, this)

        val outerExpandAmount = currentBorderWidthPx / 2f
        val expandedOuterPath = outline.toExpandedPath(outerExpandAmount)

        drawPath(
            path = expandedOuterPath,
            color = baseColor,
            style = Stroke(width = currentBorderWidthPx),
            alpha = progress
        )
    }
}


// ========== Path Expansion Helper ==========
private fun Outline.toExpandedPath(expandBy: Float): Path {
    val path = Path()
    when (this) {
        is Outline.Rectangle -> {
            path.addRect(
                Rect(
                    left = rect.left - expandBy,
                    top = rect.top - expandBy,
                    right = rect.right + expandBy,
                    bottom = rect.bottom + expandBy
                )
            )
        }
        is Outline.Rounded -> {
            val rr = roundRect
            path.addRoundRect(
                RoundRect(
                    left = rr.left - expandBy,
                    top = rr.top - expandBy,
                    right = rr.right + expandBy,
                    bottom = rr.bottom + expandBy,
                    topLeftCornerRadius = CornerRadius(
                        (rr.topLeftCornerRadius.x + expandBy).coerceAtLeast(0f),
                        (rr.topLeftCornerRadius.y + expandBy).coerceAtLeast(0f)
                    ),
                    topRightCornerRadius = CornerRadius(
                        (rr.topRightCornerRadius.x + expandBy).coerceAtLeast(0f),
                        (rr.topRightCornerRadius.y + expandBy).coerceAtLeast(0f)
                    ),
                    bottomRightCornerRadius = CornerRadius(
                        (rr.bottomRightCornerRadius.x + expandBy).coerceAtLeast(0f),
                        (rr.bottomRightCornerRadius.y + expandBy).coerceAtLeast(0f)
                    ),
                    bottomLeftCornerRadius = CornerRadius(
                        (rr.bottomLeftCornerRadius.x + expandBy).coerceAtLeast(0f),
                        (rr.bottomLeftCornerRadius.y + expandBy).coerceAtLeast(0f)
                    )
                )
            )
        }
        is Outline.Generic -> path.addPath(this.path)
    }
    return path
}


// ========== Preview ==========
@Preview(showBackground = true)
@Composable
private fun PremiumBorderPreview() {
    val shape = MaterialTheme.shapes.medium
    val entryAnim = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            entryAnim.snapTo(0f)
            entryAnim.animateTo(1f, animationSpec = tween(1500, easing = EaseInBack))
            kotlinx.coroutines.delay(1000)
        }
    }

    MonoTaskTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            GlassSurface(
                shape = shape,
                modifier = Modifier
                    .padding(12.dp)
                    .premiumBorder(
                        shape = shape,
                        color = MaterialTheme.customColors.ace,
                        entryProgressProvider = { entryAnim.value }
                    )
            ) {
                Text(
                    text = "Premium Border",
                    modifier = Modifier.padding(46.dp)
                )
            }
        }
    }
}
