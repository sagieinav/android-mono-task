package dev.sagi.monotask.ui.component.task

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors
import dev.sagi.monotask.ui.theme.harabara

enum class TagSize { Default, Small }

private val TagIconStartInset = 2.dp
private val TagIconEndInset   = 3.dp
private val TagPaddingMinimum = 2.dp

@Composable
fun TaskTag(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: TagSize = TagSize.Default,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val borderColor       = lerp(Color.Black, contentColor, 0.85f).copy(alpha = 0.2f)
    val textStyle         = MaterialTheme.typography.labelLarge.copy(
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim      = LineHeightStyle.Trim.Both
        )
    )
    val fontSize          = if (size == TagSize.Small) 9.sp else 14.sp
    val horizontalPadding = if (size == TagSize.Small) 5.dp else 8.dp
    val verticalPadding   = if (size == TagSize.Small) 1.dp else 2.dp
    val borderWidth       = if (size == TagSize.Small) 1.dp else 1.5.dp

    Surface(
        modifier = modifier.border(borderWidth, borderColor, CircleShape),
        shape    = CircleShape,
        color    = containerColor,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(
                start = if (leadingContent  != null) (horizontalPadding - TagIconStartInset).coerceAtLeast(TagPaddingMinimum)
                else horizontalPadding,
                end   = if (trailingContent != null) (horizontalPadding - TagIconEndInset).coerceAtLeast(TagPaddingMinimum)
                else horizontalPadding,
            )
        ) {
            leadingContent?.invoke()
            Text(
                text       = label.lowercase(),
                style      = textStyle,
                fontWeight = FontWeight.Bold,
                fontSize   = fontSize,
                fontFamily = harabara,
                color      = contentColor.copy(alpha = 0.85f),
                modifier   = Modifier.padding(vertical = verticalPadding),
                maxLines   = 1
            )
            trailingContent?.invoke()
        }
    }
}

@Composable
fun CustomTag(
    label: String,
    modifier: Modifier = Modifier,
    size: TagSize = TagSize.Default,
    onRemove: (() -> Unit)? = null
) {
    val colors = MaterialTheme.customColors
    val (containerColor, contentColor) = colors.tagColorFor(label)

    TaskTag(
        label           = label,
        containerColor  = containerColor,
        contentColor    = contentColor,
        size            = size,
        modifier        = modifier,
        trailingContent = onRemove?.let { removeAction ->
            {
                IconButton(
                    onClick  = removeAction,
                    modifier = Modifier.size(12.dp),
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_close),
                        contentDescription = "Remove $label tag",
                        tint               = MaterialTheme.colorScheme.outline,
                        modifier           = Modifier.size(10.dp)
                    )
                }
            }
        }
    )
}


// ========== Previews ==========

private val previewTags = listOf("io", "backend", "research", "android")

@Preview(showBackground = true, name = "CustomTag — default")
@Composable
private fun CustomTagDefaultPreview() {
    MonoTaskTheme {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(4.dp),
            modifier              = Modifier.padding(16.dp)
        ) {
            previewTags.forEach { CustomTag(label = it) }
        }
    }
}

@Preview(showBackground = true, name = "CustomTag — small")
@Composable
private fun CustomTagSmallPreview() {
    MonoTaskTheme {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(4.dp),
            modifier              = Modifier.padding(16.dp)
        ) {
            previewTags.forEach { CustomTag(label = it, size = TagSize.Small) }
        }
    }
}

@Preview(showBackground = true, name = "CustomTag — removable")
@Composable
private fun CustomTagRemovablePreview() {
    MonoTaskTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(4.dp),
            ) {
                previewTags.forEach { CustomTag(label = it, onRemove = {}) }
            }
        }
    }
}
