package dev.sagi.monotask.ui.component.core

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    iconRes: Int? = null
) {
    val color = if (selected) selectedColor
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)

    val iconSize by animateDpAsState(
        targetValue = if (selected) 16.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipIconSize"
    )


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
        leadingContent =
            iconRes?.let {
            {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier.height(iconSize),
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
