package dev.sagi.monotask.ui.component.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.nationalPark

@Composable
fun InfoCallout(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: Painter? = painterResource(R.drawable.ic_info_circle),
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    bodyColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    iconSize: Dp = 16.dp,
    horizontalPadding: Dp = 20.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon?.let {
                Icon(
                    painter           = it,
                    contentDescription = null,
                    tint              = titleColor,
                    modifier          = Modifier
                        .size(iconSize)
                )
            }
            Text(
                text       = title,
                style      = MaterialTheme.typography.bodySmall,
                fontFamily = nationalPark,
                fontWeight = FontWeight.Bold,
                color      = titleColor,
                modifier   = Modifier
            )
        }
        Text(
            text  = body,
            style = MaterialTheme.typography.bodySmall,
            color = bodyColor
        )
    }
}


// ========== Previews ==========

@Preview(showBackground = true, name = "InfoCallout, Default")
@Composable
private fun InfoCalloutPreview() {
    MonoTaskTheme {
        InfoCallout(
            title = "MANUAL SNOOZE",
            body  = "Want to pick a specific next task? Tap it on the Kanban board and hit 'Focus Now'!",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "InfoCallout, No Icon")
@Composable
private fun InfoCalloutNoIconPreview() {
    MonoTaskTheme {
        InfoCallout(
            title = "MANUAL SNOOZE",
            body  = "Want to pick a specific next task? Tap it on the Kanban board and hit 'Focus Now'!",
            icon  = null,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "InfoCallout, Colored")
@Composable
private fun InfoCalloutColoredPreview() {
    MonoTaskTheme {
        InfoCallout(
            title      = "TIP",
            body       = "Complete tasks before their due date to earn bonus XP.",
            titleColor = MaterialTheme.colorScheme.primary,
            modifier   = Modifier.padding(16.dp)
        )
    }
}
