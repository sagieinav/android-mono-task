package dev.sagi.monotask.ui.component.core

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder

@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Each tab's measured width in Dp (updated via onSizeChanged)
    val tabWidths = remember { mutableStateListOf(*Array(options.size) { 0.dp }) }

    // Offset of each tab = sum of all preceding tab widths
    val selectedOffset: Dp = tabWidths.take(selectedIndex).fold(0.dp) { acc, w -> acc + w }
    val selectedWidth: Dp  = tabWidths.getOrElse(selectedIndex) { 0.dp }

    val pillOffset by animateDpAsState(
        targetValue   = selectedOffset,
        animationSpec = spring(
            stiffness    = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        ),
        label = "segmented_toggle_pill_offset"
    )
    val pillWidth by animateDpAsState(
        targetValue   = selectedWidth,
        animationSpec = spring(
            stiffness    = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        ),
        label = "segmented_toggle_pill_width"
    )

    GlassSurface(
        blurred  = false,
        shape    = CircleShape,
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(4.dp)) {

            // Pill layer. `matchParentSize` gets its height from the Row below
            Box(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .offset(x = pillOffset)
                        .width(pillWidth)
                        .fillMaxHeight()
                        .shadow(
                            elevation    = 8.dp,
                            shape        = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.3f),
                            spotColor    = Color.Black.copy(alpha = 0.8f)
                        )
                        .clip(CircleShape)
                        .glassBorder(CircleShape)
                        .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainerLow)
                )
            }

            // Label Row: drives the height and measures each tab's real width
            Row {
                options.forEachIndexed { index, label ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .onSizeChanged { size ->
                                // Full node width (text + horizontal padding)
                                tabWidths[index] = with(density) { size.width.toDp() }
                            }
                            .clip(CircleShape)
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (!isSelected) onOptionSelected(index)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = label,
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color      = if (isSelected)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────
// Previews
// ─────────────────────────────────────────

@Preview(showBackground = true, name = "First selected")
@Composable
private fun SegmentedToggleFirstPreview() {
    MonoTaskTheme {
        SegmentedToggle(
            options          = listOf("Active", "Archive"),
            selectedIndex    = 0,
            onOptionSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Second selected")
@Composable
private fun SegmentedToggleSecondPreview() {
    MonoTaskTheme {
        SegmentedToggle(
            options          = listOf("Active", "Archive"),
            selectedIndex    = 1,
            onOptionSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Three options")
@Composable
private fun SegmentedToggleThreePreview() {
    MonoTaskTheme {
        SegmentedToggle(
            options          = listOf("Day", "Week", "Month"),
            selectedIndex    = 1,
            onOptionSelected = {}
        )
    }
}
