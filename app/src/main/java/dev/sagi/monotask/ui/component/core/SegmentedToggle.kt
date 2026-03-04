package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme

@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(100),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
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
