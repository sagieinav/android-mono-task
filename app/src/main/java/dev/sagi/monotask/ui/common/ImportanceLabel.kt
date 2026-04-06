package dev.sagi.monotask.ui.common

import dev.sagi.monotask.designsystem.theme.IconPack
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
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.designsystem.components.MonoLabel
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.customColors

@Composable
fun ImportanceLabel(
    importance: Importance,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.customColors
    val (label, color, iconRes) = when (importance) {
        Importance.HIGH   -> Triple(
            "High",
            colors.importanceHighContent,
            IconPack.ImportanceHighAlt
        )
        Importance.MEDIUM -> Triple(
            "Medium",
            colors.importanceMediumContent,
            IconPack.ImportanceMediumAlt
        )
        Importance.LOW    -> Triple(
            "Low",
            colors.importanceLowContent,
            IconPack.ImportanceLowAlt
        )
    }

    MonoLabel(
        label = label,
        accentColor = color,
        modifier = modifier,
        leadingContent = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = color,
                modifier = Modifier.height(14.dp)
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
