package dev.sagi.monotask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.SuccessGreen
import dev.sagi.monotask.ui.theme.PenaltyRedGlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import dev.sagi.monotask.ui.theme.bonusGreen
import dev.sagi.monotask.ui.theme.penaltyRed
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun XpDeltaBadge(
    xpDelta: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val isPositive = xpDelta >= 0
    val label = if (isPositive) "+${xpDelta} XP" else "${xpDelta} XP"
    val color = if (isPositive) bonusGreen else penaltyRed

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "xp_alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 12f,
        animationSpec = tween(400, easing = EaseOutCubic),
        label = "xp_offset"
    )

    Text(
        text = label,
        color = color.copy(alpha = alpha),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier.offset { IntOffset(0, offsetY.roundToInt()) }
    )
}



@Preview(showBackground = true)
@Composable
fun XpDeltaBadgePreview() {
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

            XpDeltaBadge(
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
