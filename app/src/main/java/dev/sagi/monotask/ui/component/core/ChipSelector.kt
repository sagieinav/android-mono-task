package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme

@Composable
fun GlassChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    selectedColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = MaterialTheme.shapes.small,
    leadingIcon: @Composable (() -> Unit)? = null // for using it as "AssistChip"
) {
    val textColor = if (selected) selectedColor
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)

    GlassLabel(
        label             = label,
        color             = textColor,
        modifier          = modifier,
        shape             = shape,
        textStyle         = MaterialTheme.typography.titleSmall,
        fontWeight        = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        horizontalPadding = 10.dp,
        verticalPadding   = 6.dp,
        accentColor       = if (selected) selectedColor else null,
        baseColor         = Color.Transparent,
        onClick           = onClick,
        leadingContent    = leadingIcon
    )
}


// ========== Previews ==========

@Preview(showBackground = true)
@Composable
private fun GlassChipPreview() {
    MonoTaskTheme {
        Row(
            modifier              = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassChip(label = "All", selected = false, onClick = {})
            GlassChip(label = "High", selected = true, onClick = {})
        }
    }
}
