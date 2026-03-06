package dev.sagi.monotask.ui.focus

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
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
import kotlin.math.roundToInt

@Composable
fun XpLabelCurrent(xp: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_xp),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
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



@Composable
fun XpLabelCompletion(
    xpDelta: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val label = "+${xpDelta} XP"
    val color = bonusGreen

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
