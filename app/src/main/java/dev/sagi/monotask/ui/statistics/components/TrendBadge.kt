package dev.sagi.monotask.ui.statistics.components

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.components.MonoLabel
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.bonusGreen

@Composable
fun TrendBadge(
    trendPercent: Int,
    modifier: Modifier = Modifier
) {
    val trendUp = trendPercent > 0
    val tint = if (trendUp) bonusGreen else MaterialTheme.colorScheme.error
    val prefix = if (trendUp) "+" else ""

    MonoLabel(
        label = "$prefix$trendPercent%",
        accentColor = tint,
        modifier = modifier,
        leadingContent = {
            Icon(
                painter = painterResource(if (trendUp) IconPack.TrendingUp else IconPack.TrendingDown),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
        }
    )
}

@Preview(showBackground = true, name = "TrendBadge — positive")
@Composable
private fun TrendBadgePositivePreview() {
    MonoTaskTheme {
        TrendBadge(trendPercent = 42)
    }
}

@Preview(showBackground = true, name = "TrendBadge — negative")
@Composable
private fun TrendBadgeNegativePreview() {
    MonoTaskTheme {
        TrendBadge(trendPercent = -15)
    }
}
