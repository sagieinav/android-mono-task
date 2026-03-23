package dev.sagi.monotask.ui.component.display

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.theme.bonusGreen

@Composable
fun TrendBadge(trendPercent: Int) {
    val trendUp = trendPercent >= 0
    val tint = if (trendUp) bonusGreen else MaterialTheme.colorScheme.error
    GlassSurface(
        shape       = MaterialTheme.shapes.small,
        accentColor = tint,
        baseColor   = tint.copy(alpha = 0.08f),
        blurred     = false
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val iconRes = if (trendUp) R.drawable.ic_trending_up else R.drawable.ic_trending_down
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint               = tint,
                modifier           = Modifier.size(14.dp)
            )
            Text(
                text  = "${if (trendUp) "+" else ""}$trendPercent%",
                style = MaterialTheme.typography.labelMedium,
                color = tint
            )
        }
    }
}