package dev.sagi.monotask.ui.component.core

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.ui.theme.monoShadowWorkaround
import dev.sagi.monotask.util.Constants

private val TAB_TRACK_SHAPE = CircleShape
private val TAB_PILL_SHAPE  = CircleShape

@Composable
fun GlassTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        shape    = TAB_TRACK_SHAPE,
        modifier = modifier
            .fillMaxWidth()
            .monoShadowWorkaround(CircleShape),
        blurred  = true,
        baseColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            // Animated pill: subtle highlight inside the frosted container
            BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                val tabWidth = maxWidth / tabs.size

                val pillOffset by animateDpAsState(
                    targetValue   = tabWidth * selectedIndex,
                    animationSpec = spring(
                        stiffness    = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioLowBouncy
                    ),
                    label = "glass_tab_pill"
                )

                Box(
                    modifier = Modifier
                        .offset(x = pillOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .shadow(
                            elevation    = 8.dp,
                            shape        = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.3f),
                            spotColor    = Color.Black.copy(alpha = 0.6f)
                        )
                        .clip(TAB_PILL_SHAPE)
                        .glassBorder(TAB_PILL_SHAPE)
                        .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainerLow)
//                        .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                )
            }

            // Labels: equal-width slots. Drives the height of the whole component
            Row(modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { index, label ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(TAB_PILL_SHAPE)
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (!isSelected) onTabSelected(index)
                            }
//                            .padding(vertical = 5.5.dp)
                            .height(Constants.Theme.TOP_BAR_ITEM_HEIGHT)

                        ,
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = label,
                            style      = MaterialTheme.typography.titleMedium,
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

@Preview(showBackground = true, name = "Profile tabs — first")
@Composable
private fun GlassTabRowFirstPreview() {
    MonoTaskTheme {
        GlassTabRow(
            tabs          = listOf("Profile", "Statistics", "Social"),
            selectedIndex = 0,
            onTabSelected = {},
            modifier      = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, name = "Profile tabs — middle")
@Composable
private fun GlassTabRowMiddlePreview() {
    MonoTaskTheme {
        GlassTabRow(
            tabs          = listOf("Profile", "Statistics", "Social"),
            selectedIndex = 1,
            onTabSelected = {},
            modifier      = Modifier.fillMaxWidth()
        )
    }
}
