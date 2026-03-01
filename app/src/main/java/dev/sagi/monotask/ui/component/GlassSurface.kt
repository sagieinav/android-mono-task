@file:OptIn(ExperimentalHazeMaterialsApi::class)

package dev.sagi.monotask.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.ui.theme.LocalHazeState
import dev.sagi.monotask.ui.theme.MonoTaskTheme

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = LocalHazeState.current

    Box(
        modifier = modifier
            // Clip FIRST so the blur cannot bleed outside the shape
            .clip(shape)
            // Apply the blur effect inside the clipped bounds
            .hazeEffect(hazeState, HazeMaterials.ultraThin())
            // Inner "glass highlight" border
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = shape
            )
            // Outer border
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = shape
            )
            // Glass gradient
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun GlassSurfaceElevated(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    GlassSurface(
        modifier
            // Shadow for elevation
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            ),
        shape
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun GlassSurfacePreview() {
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
                GlassSurface(content = { CustomTagPreview() })
                // Not Elevated
                GlassSurface(content = { CustomTagPreview() })

                // One more without a lambda (manual):
                GlassSurface() {
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