package dev.sagi.monotask.ui.component.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.penaltyRed
import androidx.compose.ui.text.style.TextAlign
import dev.sagi.monotask.domain.service.XpEngine
import dev.sagi.monotask.ui.component.core.ActionButton
import dev.sagi.monotask.ui.component.core.MonoBottomSheet
import dev.sagi.monotask.ui.component.core.MonoLabel
import dev.sagi.monotask.ui.component.display.InfoCallout

@Composable
fun SnoozeBottomSheet(
    onDismissRequest: () -> Unit,
    onSnooze: (XpEngine.SnoozeOption) -> Unit
) {
    MonoBottomSheet(
        title = "Snooze Task",
        onDismissRequest = onDismissRequest
    ) {
        Text(
//            "Choose your next task",
            "CHOOSE YOUR NEXT TASK",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
//            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),

            modifier = Modifier.fillMaxWidth()
        )

        // Define the metadata of each snooze option button
        val snoozeOptions = remember {
            listOf(
                Triple(XpEngine.SnoozeOption.BY_DUE_DATE,       R.drawable.ic_due_soon,         "Due soon"),
                Triple(XpEngine.SnoozeOption.NEXT_IN_QUEUE,     R.drawable.ic_skip,             "Next in queue"),
            )
        }

        // Show the snooze button for each snooze option
        snoozeOptions.forEach { (option, iconRes, label) ->
            ChooseSnoozeButton(
                icon       = painterResource(iconRes),
                label      = label,
                xpPenalty  = option.penalty,
                onClick    = { onSnooze(option) }
            )
        }

        // Info for manual snooze
        InfoCallout(
            title    = "Manual Snooze",
            body     = "Want to pick a specific next task? Tap it on the Kanban board and hit 'Focus now'!",
            modifier = Modifier
                .padding(top = 8.dp)
        )
    }
}


@Composable
fun ChooseSnoozeButton(
    icon: Painter,
    label: String,
    modifier: Modifier = Modifier,
    xpPenalty: Int? = null,
    onClick: () -> Unit = {}
) {
    ActionButton(
        onClick = onClick,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface  // neutral, not a primary action
    ) {
        // Override arrangement to SpaceBetween for this specific layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = LocalContentColor.current.copy(alpha = 0.7f)  // inherits color
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            xpPenalty?. let {
                MonoLabel(
                    label = "$it XP",
                    color = penaltyRed,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}


// ========== Preview ==========

@Preview(showBackground = true)
@Composable
private fun ChooseSnoozeButtonPreview() {
    MonoTaskTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChooseSnoozeButton(
                icon      = painterResource(R.drawable.ic_due_soon),
                label     = "Due soon",
                xpPenalty = XpEngine.SnoozeOption.BY_DUE_DATE.penalty
            )
            ChooseSnoozeButton(
                icon      = painterResource(R.drawable.ic_skip),
                label     = "Next in queue",
                xpPenalty = XpEngine.SnoozeOption.NEXT_IN_QUEUE.penalty
            )
        }
    }
}
