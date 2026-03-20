package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun GlassChip(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    selectedColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    leadingIcon: @Composable (() -> Unit)? = null // for using it as "AssistChip"
) {
    GlassSurface(
        shape = shape,
        blurred = false,
        accentColor = if (selected) selectedColor else null,
        modifier = modifier
            .clip(shape)
            .clickable(
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            leadingIcon?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}