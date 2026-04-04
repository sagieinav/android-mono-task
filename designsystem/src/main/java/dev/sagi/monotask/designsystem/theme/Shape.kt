package dev.sagi.monotask.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val HexagonShape: Shape = GenericShape { size, _ ->
    val parsed = PathParser()
        .parsePathString(
            "M10.425,1.414l-6.775,3.996a3.21,3.21 0,0 0,-1.65 2.807v7.285" +
            "a3.226,3.226 0,0 0,1.678 2.826l6.695,4.237c1.034,0.57 2.22,0.57 3.2,0.032" +
            "l6.804,-4.302c0.98,-0.537 1.623,-1.618 1.623,-2.793v-7.284l-0.005,-0.204" +
            "a3.223,3.223 0,0 0,-1.284 -2.39l-0.107,-0.075l-0.007,-0.007" +
            "a1.074,1.074 0,0 0,-0.181 -0.133l-6.776,-3.995a3.33,3.33 0,0 0,-3.216 0z"
        )
        .toPath()
    // Path actual bounds within the 24×24 SVG grid
    val scale = size.width / 20f
    parsed.transform(Matrix().apply {
        scale(scale, scale)
        translate(-2f, -1.414f)
    })
    addPath(parsed)
}

// Map/override the 5 default shapes of M3
val MonoTaskShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),   // Chips, small badges
    small      = RoundedCornerShape(12.dp),  // Input fields, snackbars
    medium     = RoundedCornerShape(16.dp),  // Kanban task cards
    large      = RoundedCornerShape(24.dp),  // FocusCard, bottom sheets
    extraLarge = RoundedCornerShape(32.dp)   // Dialogs, full overlays
)
