package dev.sagi.monotask.designsystem.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.glassBackground
import dev.sagi.monotask.designsystem.theme.glassBorder
import dev.sagi.monotask.designsystem.theme.monoShadow

private val TogglePillSpring = spring<Dp>(
    stiffness = Spring.StiffnessMediumLow,
    dampingRatio = Spring.DampingRatioLowBouncy
)


@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false,
    blurred: Boolean = false,
    baseColor: Color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
    textStyle: TextStyle = MaterialTheme.typography.titleSmall
) {
    // Capture constraints BEFORE GlassSurface, since Box always passes min=0 to its content,
    // losing the tight height constraint that external .height() modifiers create.
    BoxWithConstraints(
        modifier = modifier.monoShadow(CircleShape)
    ) {
        val fillHeight = constraints.hasFixedHeight

        GlassSurface(
            blurred = blurred,
            shape = CircleShape,
            baseColor = baseColor,
            modifier = if (fillHeight) Modifier.fillMaxHeight() else Modifier
        ) {
            Box(
                modifier = Modifier.padding(4.dp)
            ) {
                if (fullWidth) {
                    FullWidthToggleContent(options, selectedIndex, onOptionSelected, textStyle)
                } else {
                    ContentSizedToggleContent(options, selectedIndex, onOptionSelected, textStyle, fillHeight)
                }
            }
        }
    }
}

// Equal-width tabs via BoxWithConstraints
@Composable
private fun BoxScope.FullWidthToggleContent(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    textStyle: TextStyle
) {
    // Pill layer
    BoxWithConstraints(modifier = Modifier.matchParentSize()) {
        val tabWidth = maxWidth / options.size
        val pillOffset by animateDpAsState(
            targetValue   = tabWidth * selectedIndex,
            animationSpec = TogglePillSpring,
            label = "segmented_toggle_pill_offset"
        )
        TogglePill(
            modifier = Modifier
                .offset(x = pillOffset)
                .width(tabWidth)
        )
    }

    // Label row: drives the height, distributes width equally
    Row(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (!isSelected) onOptionSelected(index)
                    },
                contentAlignment = Alignment.Center
            ) {
                ToggleLabel(label, isSelected, textStyle)
            }
        }
    }
}

// Content-sized tabs. Pill animates both offset and width to match each option's natural size
@Composable
private fun BoxScope.ContentSizedToggleContent(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    textStyle: TextStyle,
    fillHeight: Boolean = false
) {
    val density = LocalDensity.current
    val tabWidths = remember { mutableStateListOf(*Array(options.size) { 0.dp }) }

    val selectedOffset: Dp = tabWidths.take(selectedIndex).fold(0.dp) { acc, w -> acc + w }
    val selectedWidth: Dp  = tabWidths.getOrElse(selectedIndex) { 0.dp }

    val pillOffset by animateDpAsState(
        targetValue   = selectedOffset,
        animationSpec = TogglePillSpring,
        label = "segmented_toggle_pill_offset"
    )
    val pillWidth by animateDpAsState(
        targetValue   = selectedWidth,
        animationSpec = TogglePillSpring,
        label = "segmented_toggle_pill_width"
    )

    // Pill layer
    Box(
        modifier = Modifier
            .matchParentSize()
    ) {
        TogglePill(
            modifier = Modifier
                .offset(x = pillOffset)
                .width(pillWidth)
        )
    }

    // Label row: drives height, each item sizes to its content
    Row {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .onSizeChanged { size ->
                        tabWidths[index] = with(density) { size.width.toDp() }
                    }
                    .clip(CircleShape)
                    .clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (!isSelected) onOptionSelected(index)
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .then(
                        if (fillHeight) Modifier.fillMaxHeight()
                                else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                ToggleLabel(label, isSelected, textStyle)
            }
        }
    }
}


@Composable
private fun TogglePill(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(CircleShape)
            .glassBorder(CircleShape)
            .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainerLow)
    )
}

@Composable
private fun ToggleLabel(label: String, isSelected: Boolean, textStyle: TextStyle) {
    Text(
        text = label,
        style = textStyle,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (isSelected)
            MaterialTheme.colorScheme.onSurfaceVariant
        else
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}


// =========================================
// Previews
// =========================================

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
