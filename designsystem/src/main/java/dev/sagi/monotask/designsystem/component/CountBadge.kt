package dev.sagi.monotask.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.customColors

@Composable
fun CountBadge(
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val labelColor = remember(color) { lerp(Color.Black, color, 0.9f).copy(alpha = 0.7f) }

    GlassSurface(
        shape = CircleShape,
        baseColor = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = count,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "count-badge"
        ) { count ->
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = labelColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CountBadgePreview() {
    MonoTaskTheme {
        GlassSurface(baseColor = MaterialTheme.colorScheme.surfaceContainer) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val colors = MaterialTheme.customColors
                CountBadge(2, colors.importanceLowBackground)
                CountBadge(2, colors.importanceMediumBackground)
                CountBadge(2, colors.importanceHighContent)
            }
        }
    }
}
