package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.basicMonoTask


@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
//    Surface(
//        shape = CircleShape,
//        color = MaterialTheme.colorScheme.surfaceVariant,
//        modifier = modifier
////            .basicMonoTask(CircleShape)
//    ) {
    GlassSurface(
        blurred = false,
        shape = CircleShape,
        modifier = modifier
//            .basicMonoTask(CircleShape)
    ) {
        Row(Modifier
            .padding(4.dp)
        ) {
            options.forEachIndexed { index, label ->
                ToggleChip(
                    label = label,
                    selected = index == selectedIndex,
                    onClick = { if (index != selectedIndex) onOptionSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun ToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = if (selected) 8.dp else 0.dp,
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.8f),
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(
                if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
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
            options = listOf("Active", "Archive"),
            selectedIndex = 0,
            onOptionSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Second selected")
@Composable
private fun SegmentedToggleSecondPreview() {
    MonoTaskTheme {
        SegmentedToggle(
            options = listOf("Active", "Archive"),
            selectedIndex = 1,
            onOptionSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Three options")
@Composable
private fun SegmentedToggleThreePreview() {
    MonoTaskTheme {
        SegmentedToggle(
            options = listOf("Day", "Week", "Month"),
            selectedIndex = 1,
            onOptionSelected = {}
        )
    }
}
