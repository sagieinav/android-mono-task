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
import com.google.firebase.Timestamp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors
import dev.sagi.monotask.ui.theme.harabara
import dev.sagi.monotask.util.ext.toRelativeDate

enum class TagSize { DEFAULT, SMALL }

@Composable
fun TaskTag(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: TagSize = TagSize.DEFAULT,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val tagShape      = CircleShape
    val borderColor   = lerp(Color.Black, contentColor, 0.85f).copy(alpha = 0.2f)
    val textStyle     = MaterialTheme.typography.labelLarge.copy(
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim      = LineHeightStyle.Trim.Both
        )
    )
    val fontSize          = if (size == TagSize.DEFAULT) 14.sp else 10.sp
    val horizontalPadding = if (size == TagSize.SMALL) 5.dp else 8.dp
    val verticalPadding   = if (size == TagSize.SMALL) 0.7.dp else 2.dp
    val borderWidth       = if (size == TagSize.SMALL) 1.dp else 1.5.dp

    Surface(
        modifier = modifier.border(borderWidth, borderColor, tagShape),
        shape    = tagShape,
        color    = containerColor,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(
                start = if (leadingContent != null) (horizontalPadding - 2.dp).coerceAtLeast(2.dp)
                        else horizontalPadding,
                end   = if (trailingContent != null) horizontalPadding - 3.dp
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
    size: TagSize = TagSize.DEFAULT,
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


@Composable
fun ImportanceTag(
    importance: Importance,
    modifier: Modifier = Modifier,
    size: TagSize = TagSize.DEFAULT
) {
    val colors = MaterialTheme.customColors
    val (label, containerColor, contentColor) = when (importance) {
        Importance.HIGH   -> Triple("High",   colors.importanceHighBackground,   colors.importanceHighContent)
        Importance.MEDIUM -> Triple("Medium", colors.importanceMediumBackground, colors.importanceMediumContent)
        Importance.LOW    -> Triple("Low",    colors.importanceLowBackground,    colors.importanceLowContent)
    }
    TaskTag(
        label          = label,
        containerColor = containerColor,
        contentColor   = contentColor,
        size           = size,
        modifier       = modifier
    )
}


@Composable
fun DueDateTag(
    timestamp: Timestamp,
    modifier: Modifier = Modifier,
    size: TagSize = TagSize.DEFAULT
) {
    val relativeDate   = timestamp.toRelativeDate()
    val contentColor   = if (relativeDate.isOverdue) MaterialTheme.colorScheme.error
                         else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val containerColor = contentColor.copy(alpha = 0.08f)
    val iconSize       = if (size == TagSize.SMALL) 8.dp else 14.dp

    TaskTag(
        label          = relativeDate.text,
        containerColor = containerColor,
        contentColor   = contentColor,
        size           = size,
        modifier       = modifier,
        leadingContent = {
            Icon(
                painter            = painterResource(R.drawable.ic_due_soon),
                contentDescription = null,
                tint               = contentColor.copy(alpha = 0.85f),
                modifier           = Modifier.size(iconSize)
            )
        }
    )
}



// ========== Previews ==========

@Preview(showBackground = true)
@Composable
fun CustomTagPreview() {
    MonoTaskTheme {
        Column {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(4.dp),
                modifier              = Modifier.padding(16.dp)
            ) {
                ImportanceTag(importance = Importance.LOW)
                ImportanceTag(importance = Importance.MEDIUM)
                ImportanceTag(importance = Importance.HIGH)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(4.dp),
                modifier              = Modifier.padding(16.dp)
            ) {
                ImportanceTag(size = TagSize.SMALL, importance = Importance.LOW)
                ImportanceTag(size = TagSize.SMALL, importance = Importance.MEDIUM)
                ImportanceTag(size = TagSize.SMALL, importance = Importance.HIGH)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(4.dp),
                modifier              = Modifier.padding(16.dp)
            ) {
                DueDateTag(timestamp = Timestamp(System.currentTimeMillis() / 1000 + 86400 * 2, 0))
                DueDateTag(timestamp = Timestamp(System.currentTimeMillis() / 1000 - 86400, 0))
            }
        }
    }
}
