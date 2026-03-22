package dev.sagi.monotask.ui.profile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.lerp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors


@Composable
fun XpBar(
    level: Int,
    currentXp: Int,
    xpForNextLevel: Int,
    modifier: Modifier = Modifier
) {
    val progress = currentXp.toFloat() / xpForNextLevel.toFloat()

    val animatable = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatable.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.alignByBaseline()
            ) {
                Text(
                    text       = "Lv.",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Thin,
                    color      = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                    modifier   = Modifier.alignByBaseline()
                )
                Text(
                    text     = "$level",
                    style    = MaterialTheme.typography.headlineMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Text(
                text     = "$currentXp / $xpForNextLevel XP",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Thin,
                color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                modifier = Modifier.alignByBaseline()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatable.value)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(run {
                        val xpColor = MaterialTheme.customColors.xp
                        Brush.horizontalGradient(
                            colors = listOf(lerp(xpColor, Color.White, 0.3f), xpColor)
                        )
                    })
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun XpBarPreview() {
    MonoTaskTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            XpBar(
                level = 12,
                currentXp = 300,
                xpForNextLevel = 500,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
