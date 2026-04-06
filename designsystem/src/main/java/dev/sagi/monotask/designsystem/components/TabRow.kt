package dev.sagi.monotask.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme


// Wrap SegmentedToggle with full width, add blur, increase font size
@Composable
fun MonoTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) = SegmentedToggle(
    options = tabs,
    selectedIndex = selectedIndex,
    onOptionSelected = onTabSelected,
    modifier = modifier.fillMaxWidth(),
    fullWidth = true,
    blurred = true,
    textStyle = MaterialTheme.typography.titleMedium
)


// Previews
@Preview(showBackground = true, name = "Profile tabs — first")
@Composable
private fun MonoTabRowFirstPreview() {
    MonoTaskTheme {
        MonoTabRow(
            tabs          = listOf("Profile", "Statistics", "Social"),
            selectedIndex = 0,
            onTabSelected = {},
        )
    }
}

@Preview(showBackground = true, name = "Profile tabs — middle")
@Composable
private fun MonoTabRowMiddlePreview() {
    MonoTaskTheme {
        MonoTabRow(
            tabs          = listOf("Profile", "Statistics", "Social"),
            selectedIndex = 1,
            onTabSelected = {},
        )
    }
}
