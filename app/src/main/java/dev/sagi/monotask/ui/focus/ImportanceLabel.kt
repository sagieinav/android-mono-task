package dev.sagi.monotask.ui.focus

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
import dev.sagi.monotask.ui.component.core.MonoLabel
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors

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
            R.drawable.ic_importance_high_alt
        )
        Importance.MEDIUM -> Triple(
            "Medium",
            colors.importanceMediumContent,
            R.drawable.ic_importance_medium_alt
        )
        Importance.LOW    -> Triple(
            "Low",
            colors.importanceLowContent,
            R.drawable.ic_importance_low_alt
        )
    }

    MonoLabel(
        label = label,
        color = color,
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
