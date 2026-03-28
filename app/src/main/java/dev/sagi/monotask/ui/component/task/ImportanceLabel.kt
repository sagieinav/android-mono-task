package dev.sagi.monotask.ui.component.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.ui.component.core.GlassLabel
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors

@Composable
fun ImportanceLabel(
    importance: Importance,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.customColors
    val (label, contentColor) = when (importance) {
        Importance.HIGH   -> "High"   to colors.importanceHighContent
        Importance.MEDIUM -> "Medium" to colors.importanceMediumContent
        Importance.LOW    -> "Low"    to colors.importanceLowContent
    }
    val iconRes = when (importance) {
        Importance.HIGH   -> R.drawable.ic_importance_high_alt
        Importance.MEDIUM -> R.drawable.ic_importance_medium_alt
        Importance.LOW    -> R.drawable.ic_importance_low_alt
    }
    GlassLabel(
        label    = label,
        color    = contentColor,
        modifier = modifier,
        leadingContent = {
            Icon(
                painter            = painterResource(iconRes),
                contentDescription = "Importance Icon",
                tint               = contentColor,
                modifier           = Modifier.height(14.dp)
            )
        }
    )
}


// ========== Previews ==========

@Preview(showBackground = true)
@Composable
private fun ImportanceLabelPreview() {
    MonoTaskTheme {
        Row(
            modifier              = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImportanceLabel(importance = Importance.HIGH)
            ImportanceLabel(importance = Importance.MEDIUM)
            ImportanceLabel(importance = Importance.LOW)
        }
    }
}
