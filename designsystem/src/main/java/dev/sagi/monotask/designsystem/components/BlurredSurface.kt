package dev.sagi.monotask.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.sagi.monotask.designsystem.theme.LocalHazeState
import dev.sagi.monotask.designsystem.theme.glassBorder
import dev.sagi.monotask.designsystem.theme.monoShadow

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun BlurredSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    elevated: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = LocalHazeState.current

    // Blurred components get a transparent base color by default
//    val resolvedBaseColor = baseColor ?: Color.Transparent

    Box(
        modifier = modifier
            .then(
                if (elevated) Modifier.monoShadow(shape)
                else Modifier
            )
            .clip(shape)
            .glassBorder(shape)
            .hazeEffect(hazeState, HazeMaterials.ultraThin())
    ) {
        content()
    }
}