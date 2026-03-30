@file:OptIn(ExperimentalHazeMaterialsApi::class)

package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.sagi.monotask.ui.component.task.CustomTag
import dev.sagi.monotask.ui.theme.LocalHazeState
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    blurred: Boolean = true,
    baseColor: Color = Color.Transparent,
    accentColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = LocalHazeState.current

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (!blurred) Modifier
                else Modifier.hazeEffect(hazeState, HazeMaterials.ultraThin())
            )
            .glassBorder(shape, accentColor)
            .glassBackground(accentColor = accentColor, baseColor = baseColor)
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun GlassSurfacePreview() {
    MonoTaskTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFB3C8F0),
                            Color(0xFFE8D5F5),
                            Color(0xFFF5E6D0)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassSurface(blurred = true) {
                    FlowRow(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CustomTag("leetcode")
                        CustomTag("work")
                        CustomTag("android")
                    }
                }
                GlassSurface(blurred = false) {
                    FlowRow(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CustomTag("leetcode")
                        CustomTag("work")
                        CustomTag("android")
                    }
                }
                GlassSurface(
                    blurred = false,
                    accentColor = MaterialTheme.colorScheme.primary
                ) {
                    FlowRow(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CustomTag("leetcode")
                        CustomTag("work")
                        CustomTag("android")
                        CustomTag("with accent")
                    }
                }
            }
        }
    }
}
