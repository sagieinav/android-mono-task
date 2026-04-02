package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme

@Composable
fun MonoChipSelector(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    selectedColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = MaterialTheme.shapes.small,
    iconRes: Int? = null // for using it as "AssistChip"
) {
    val color = if (selected) selectedColor
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)

    MonoLabel(
        label = label,
        color = color,
        modifier = modifier,
        shape = shape,
        textStyle = MaterialTheme.typography.labelLarge,
        fontWeight = if (selected) FontWeight.Medium else FontWeight.ExtraLight,
        horizontalPadding = 10.dp,
        verticalPadding = 6.dp,
        accentColor = if (selected) selectedColor else null,
        baseColor = Color.Transparent,
        onClick = onClick,
        leadingContent = {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier  = Modifier.height(16.dp),
                    tint = color
                )
            }
        }
    )
}


// ========== Previews ==========

@Preview(showBackground = true)
@Composable
private fun MonoChipSelectorPreview() {
    MonoTaskTheme {
        Row(
            modifier              = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MonoChipSelector(label = "All", selected = false, onClick = {})
            MonoChipSelector(label = "High", selected = true, onClick = {})
        }
    }
}
