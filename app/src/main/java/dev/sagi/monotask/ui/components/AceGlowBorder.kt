package dev.sagi.monotask.ui.components


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
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.AceGold
import dev.sagi.monotask.ui.theme.AceGoldDim
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import dev.sagi.monotask.ui.theme.AceGoldGlow


fun Modifier.aceGlowBorder(
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 3.dp
): Modifier = composed {
    val rotation by rememberInfiniteTransition(label = "aceGlow").animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing)
        ),
        label = "aceRotation"
    )

    this.drawBehind {
        // Build a sweep gradient shader and rotate ONLY its colors
        val shader = android.graphics.SweepGradient(
            size.width / 2f,
            size.height / 2f,
            intArrayOf(
                AceGoldGlow.toArgb(),
                AceGold.toArgb(),
                AceGoldDim.toArgb(),
                AceGold.toArgb(),
                AceGoldGlow.toArgb(),
            ),
            null
        )
        val matrix = android.graphics.Matrix()
        matrix.postRotate(rotation, size.width / 2f, size.height / 2f)
        shader.setLocalMatrix(matrix)

        val paint = android.graphics.Paint().apply {
            this.shader = shader
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = borderWidth.toPx()
            isAntiAlias = true
        }

        // halfStroke  maneuver for "forcing" external stroke
        val halfStroke = borderWidth.toPx() / 2f
        drawContext.canvas.nativeCanvas.drawRoundRect(
            -halfStroke, -halfStroke,
            size.width + halfStroke, size.height + halfStroke,
            cornerRadius.toPx() + halfStroke, cornerRadius.toPx() + halfStroke,
            paint
        )

    }
}


@Preview(showBackground = true)
@Composable
fun AceGlowBorderPreview() {
    MonoTaskTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            GlassCard(
                modifier = Modifier
                    .padding(12.dp)
                    .aceGlowBorder()
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
