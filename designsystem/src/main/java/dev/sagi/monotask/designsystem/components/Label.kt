package dev.sagi.monotask.designsystem.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.R
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.googleSans


@Composable
fun MonoLabel(
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    fontFamily: FontFamily = googleSans,
    fontWeight: FontWeight = FontWeight.Light,
    horizontalPadding: Dp = 6.dp,
    verticalPadding: Dp = 5.dp,
    leadingContent: @Composable (() -> Unit)? = null
) {
    val resolvedAccentColor = accentColor ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    
    val baseColor = if (accentColor != null)
                        lerp(accentColor, MaterialTheme.colorScheme.surfaceContainerLowest, 0.96f)
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh

    GlassSurface(
        elevated = false,
        shape = shape,
        accentColor = accentColor,
        baseColor = baseColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            leadingContent?.invoke()
            Text(
                text = label,
                fontFamily = fontFamily,
                style = textStyle.copy(
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Proportional,
                        trim = LineHeightStyle.Trim.Both
                    )
                ),
                fontWeight = fontWeight,
                color = resolvedAccentColor
            )
        }
    }
}



// ========== Previews ==========

@Preview(showBackground = true)
@Composable
private fun MonoLabelDefaultPreview() {
    MonoTaskTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MonoLabel(label = "High", accentColor = Color(0xFFE57373))
            MonoLabel(label = "Work", accentColor = Color(0xFF64B5F6))
            MonoLabel(label = "Done", accentColor = Color(0xFF81C784))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonoLabelWithIconPreview() {
    MonoTaskTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val color = Color(0xFFE57373)
            MonoLabel(
                label = "Due soon",
                accentColor = color,
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_due_calendar),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(12.dp)
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonoLabelClickablePreview() {
    MonoTaskTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MonoLabel(label = "Tappable", accentColor = Color(0xFF64B5F6))
        }
    }
}

