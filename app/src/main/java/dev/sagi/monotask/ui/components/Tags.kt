package dev.sagi.monotask.ui.components

import androidx.compose.animation.expandVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors


@Composable
fun TaskTag(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null // Potentially "close" icon
) {
    val tagShape = RoundedCornerShape(100)
    val borderColor = lerp(Color.Black, contentColor, 0.85f).copy(alpha = 0.2f)

    Surface(
        modifier = modifier.border(1.5.dp, borderColor, tagShape),
        shape = tagShape,
        color = containerColor,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = 9.dp,
                end = if (trailingContent != null) 6.dp else 9.dp, // tighter end padding when "close" is present
            )
        ) {
            Text(
                text = label.lowercase(),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            trailingContent?.invoke()
        }
    }
}


@Composable
fun ImportanceTag(
    importance: Importance,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.customColors
    val (label, containerColor, contentColor) = when (importance) {
        Importance.HIGH   -> Triple("High",   colors.importanceHighBackground,   colors.importanceHighContent)
        Importance.MEDIUM -> Triple("Medium", colors.importanceMediumBackground, colors.importanceMediumContent)
        Importance.LOW    -> Triple("Low",    colors.importanceLowBackground,    colors.importanceLowContent)
    }
    TaskTag(label, containerColor, contentColor, modifier)
}


@Composable
fun CustomTag(
    label: String,
    onRemove: (() -> Unit)? = null,  // null = read-only, non-null = dismissible
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.customColors
    val (containerColor, contentColor) = colors.tagColorFor(label)

    TaskTag(
        label = label,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
        trailingContent = onRemove?.let { removeAction ->
            {
                IconButton(
                    onClick = removeAction,
                    modifier = Modifier.size(12.dp), // "external" size dictates icon "padding" here
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Remove $label tag",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun CustomTagPreview() {
    MonoTaskTheme {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp)
        ) {
        // Importance Tags
            ImportanceTag(Importance.LOW)
            ImportanceTag(Importance.MEDIUM)
            ImportanceTag(Importance.HIGH)

        // Read-only custom tag (for views like focus, kanban...)
        CustomTag("leetcode")
        CustomTag("ds")

        // Dismissible custom tag with onRemove = { } (for create/edit task)
        CustomTag("leetcode", onRemove = {})
        CustomTag("ds", onRemove = {})
    }
    }
}



