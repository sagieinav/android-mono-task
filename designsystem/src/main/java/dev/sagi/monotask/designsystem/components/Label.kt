package dev.sagi.monotask.designsystem.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
    color: Color,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    fontFamily: FontFamily = googleSans,
    fontWeight: FontWeight = FontWeight.Light,
    horizontalPadding: Dp = 6.dp,
    verticalPadding: Dp = 5.dp,
    accentColor: Color? = color,
    baseColor: Color = color.copy(alpha = 0.04f),
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null
) {
    val resolvedModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable(
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
    } else modifier

    GlassSurface(
        shape = shape,
        accentColor = accentColor,
        baseColor = baseColor,
        blurred = false,
        modifier = resolvedModifier
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
                color = color
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
            MonoLabel(label = "High", color = Color(0xFFE57373))
            MonoLabel(label = "Work", color = Color(0xFF64B5F6))
            MonoLabel(label = "Done", color = Color(0xFF81C784))
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
                color = color,
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
            MonoLabel(label = "Tappable", color = Color(0xFF64B5F6), onClick = {})
        }
    }
}

