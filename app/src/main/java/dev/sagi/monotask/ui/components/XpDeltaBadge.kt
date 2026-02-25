package dev.sagi.monotask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import dev.sagi.monotask.ui.theme.bonusGreen
import dev.sagi.monotask.ui.theme.penaltyRed
import kotlinx.coroutines.delay

@Composable
fun XpDeltaBadge(
    xpDelta: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val isPositive = xpDelta >= 0
    val label = if (isPositive) "+${xpDelta} XP" else "${xpDelta} XP"
    val color = if (isPositive) bonusGreen else penaltyRed

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(300, easing = EaseOut)) +
                slideInVertically(
                    animationSpec = tween(400, easing = EaseOutCubic),
                    initialOffsetY = { it / 2 }
                ),
        exit  = fadeOut(tween(400, easing = EaseIn)) +
                slideOutVertically(
                    animationSpec = tween(400, easing = EaseInCubic),
                    targetOffsetY = { -it / 2 }
                )

    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun XpDeltaBadgePreview() {
    MonoTaskTheme {
        // Static preview: visible = true to see both states
//        Column(
//            modifier = Modifier.padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            XpDeltaBadge(xpDelta = 100, visible = true)
//            XpDeltaBadge(xpDelta = -50, visible = true)
//        }

        // Dynamic preview
        var visible by remember { mutableStateOf(false) }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            XpDeltaBadge(xpDelta = -100, visible = visible)
//            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = { visible = true }) {
                Text("Complete Task")
            }
        }

        LaunchedEffect(visible) {
            if (visible) {
                delay(2000)
                visible = false
            }
        }

    }
}
