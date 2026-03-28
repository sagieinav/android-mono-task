package dev.sagi.monotask.ui.component.display

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.core.GlassLabel
import dev.sagi.monotask.ui.theme.bonusGreen

@Composable
fun TrendBadge(trendPercent: Int) {
    val trendUp = trendPercent >= 0
    val tint = if (trendUp) bonusGreen else MaterialTheme.colorScheme.error
    GlassLabel(
        label = "${if (trendUp) "+" else ""}$trendPercent%",
        color = tint,
        leadingContent = {
            val iconRes = if (trendUp) R.drawable.ic_trending_up else R.drawable.ic_trending_down
            Icon(
                painter           = painterResource(iconRes),
                contentDescription = null,
                tint              = tint,
                modifier          = Modifier.size(14.dp)
            )
        }
    )
}
