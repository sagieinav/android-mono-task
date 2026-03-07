package dev.sagi.monotask.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutExpo
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.EaseInOutQuint
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import androidx.compose.ui.unit.IntOffset
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.bonusGreen
import dev.sagi.monotask.ui.theme.gloock
import dev.sagi.monotask.ui.theme.googleSans
import dev.sagi.monotask.ui.theme.harabara
import dev.sagi.monotask.ui.theme.libreCaslon
import dev.sagi.monotask.ui.theme.lora
import dev.sagi.monotask.ui.theme.playfairDisplay
import dev.sagi.monotask.ui.theme.plusJakartaSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun XpLabelCurrent(xp: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_xp),
            contentDescription = null,
            modifier = modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "$xp XP",
            style = MaterialTheme.typography.titleSmall
                .copy(
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Proportional,
                        trim = LineHeightStyle.Trim.Both
                    )),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}



// ========== XpLabelCompletion ==========

private const val XP_POP_SCALE        = 1.45f   // slightly less extreme → faster settle
private const val XP_INITIAL_OFFSET_Y = -30f
private const val XP_EXIT_OFFSET_Y    = -200f
private const val SHIMMER_HALF_WIDTH  = 0.2f
private const val SHIMMER_SWEEP_SCALE = 1.4f

@Composable
fun XpLabelCompletion(
    xpDelta: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val scale   = remember { Animatable(0f) }
    val offsetY = remember { Animatable(XP_INITIAL_OFFSET_Y) }
    val alpha   = remember { Animatable(0f) }
    val shimmer = remember { Animatable(0f) }
    val color   = MaterialTheme.colorScheme.primary

    LaunchedEffect(visible) {
        if (visible) {
            scale.snapTo(1f)
            offsetY.snapTo(XP_INITIAL_OFFSET_Y)
            alpha.snapTo(1f)
            shimmer.snapTo(0f)

            // 1. Pop animation
            scale.animateTo(
                XP_POP_SCALE,
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness    = Spring.StiffnessMedium // stiffer than MediumLow
                )
            )

            // 2. Shimmer (highlight) animation
            shimmer.animateTo(
                1f,
                tween(
                    durationMillis = 650,          // was 800
                    easing         = EaseInOutQuart
                )
            )

            // 3. Float up & fade out
            launch {
                offsetY.animateTo(
                    XP_EXIT_OFFSET_Y,
                    tween(400, easing = EaseInOutCubic)
                )
            }
            alpha.animateTo(0f, tween(300))
        } else {
            scale.snapTo(0f)
            alpha.snapTo(0f)
            shimmer.snapTo(0f)
            offsetY.snapTo(XP_INITIAL_OFFSET_Y)
        }
    }

    val shimmerCenter = shimmer.value * SHIMMER_SWEEP_SCALE - SHIMMER_HALF_WIDTH
    val textBrush = if (shimmer.value in 0.01f..0.99f) {
        Brush.horizontalGradient(
            0f                                              to color,
            (shimmerCenter - SHIMMER_HALF_WIDTH).coerceIn(0f, 1f) to color,
            shimmerCenter.coerceIn(0f, 1f)                        to Color.White,
            (shimmerCenter + SHIMMER_HALF_WIDTH).coerceIn(0f, 1f) to color,
            1f                                                    to color
        )
    } else null

    Row(
        modifier = modifier.graphicsLayer {
            scaleX          = scale.value
            scaleY          = scale.value
            translationY    = offsetY.value
            this.alpha      = alpha.value
            transformOrigin = TransformOrigin(1f, 1f)
            rotationZ       = 7f
        },
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text       = "+$xpDelta XP",
            style      = MaterialTheme.typography.titleSmall.copy(
                brush = textBrush,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Proportional,
                    trim      = LineHeightStyle.Trim.Both
                )
            ),
            color      = if (textBrush == null) color else Color.Unspecified,
            fontWeight = FontWeight.Bold
        )
    }
}








@Preview(showBackground = true)
@Composable
fun XpLabelCompletionPreview() {
    MonoTaskTheme {
        // Static preview
//        Column(
//            modifier = Modifier.padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            XpDeltaBadge(xpDelta = 100, visible = true)
//            XpDeltaBadge(xpDelta = -50, visible = true)
//        }

        // Dynamic preview
        var visible by remember { mutableStateOf(false) }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Button(onClick = { visible = true }) {
                Text("Complete Task")
            }

            XpLabelCompletion(
                xpDelta = 100,
                visible = visible,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-16).dp)
            )
        }

        LaunchedEffect(visible) {
            if (visible) {
                delay(2000)
                visible = false
            }
        }

    }
}
