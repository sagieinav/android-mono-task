package dev.sagi.monotask.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
                width = 2.dp,
                color = Color.White.copy(alpha = 0.5f), // glass edge highlight
                shape = shape
            ),
        shape = shape,
        color = GlassSurface,
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),  // bright top edge
                        Color.White.copy(alpha = 0.1f)   // fades out downward
                    )
                )
            )
        ) {
            content()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GlassCardPreview() {
    MonoTaskTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFB3C8F0), // soft blue
                            Color(0xFFE8D5F5), // soft purple
                            Color(0xFFF5E6D0)  // warm peach
                        )
                    )
                )
                .padding(24.dp)
        ) {
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
}