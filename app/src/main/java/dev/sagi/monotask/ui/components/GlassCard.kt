package dev.sagi.monotask.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.ui.theme.AeroShadow
import dev.sagi.monotask.ui.theme.AeroShadowDeep
import dev.sagi.monotask.ui.theme.GlassSurface
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.components.CustomTagPreview

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    elevated: Boolean = false, // false = ambient shadow, true = deeper shadow
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val shadowColor = if (elevated) AeroShadowDeep else AeroShadow
    val shadowElevation = if (elevated) 8.dp else 2.dp

    Surface(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.5f), // glass edge highlight
                shape = shape
            ),
        shape = shape,
        color = GlassSurface,
    ) {
        content()
    }
}


@Preview
@Composable
fun GlassCardPreview() {
    MonoTaskTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Elevated
            GlassCard(elevated = true, content = { CustomTagPreview() })
            // Not Elevated
            GlassCard(content = { CustomTagPreview() })

            // One more without a lambda (manual):
            GlassCard(elevated = true) {
                FlowRow(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ImportanceTag(Importance.HIGH)
                    CustomTag("leetcode")
                    CustomTag("ds", onRemove = {})
                }
            }
        }
    }
}
