package dev.sagi.monotask.ui.common

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.penaltyRed
import androidx.compose.ui.text.style.TextAlign
import dev.sagi.monotask.domain.service.XpEngine
import dev.sagi.monotask.designsystem.components.ActionButton
import dev.sagi.monotask.designsystem.components.MonoBottomSheet
import dev.sagi.monotask.designsystem.components.MonoLabel
import dev.sagi.monotask.designsystem.components.InfoCallout
import dev.sagi.monotask.designsystem.theme.customColors
import dev.sagi.monotask.designsystem.theme.glassBorder

@Composable
fun SnoozeBottomSheet(
    onDismissRequest: () -> Unit,
    onSnooze: (XpEngine.SnoozeOption) -> Unit,
    isAce: Boolean = false
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

//        if (isAce) {
//            Text(
//                text = "\u2736 Ace task: includes -${XpEngine.BONUS_ACE} XP penalty",
//                textAlign = TextAlign.Center,
//                style = MaterialTheme.typography.bodyMedium,
////                fontWeight = FontWeight.SemiBold,
//                color = MaterialTheme.customColors.ace,
//                modifier = Modifier.fillMaxWidth()
//            )
//        }

        // Define the metadata of each snooze option button
        val snoozeOptions = remember {
            listOf(
                Triple(XpEngine.SnoozeOption.BY_DUE_DATE, IconPack.DueSoon, "Due soon"),
                Triple(XpEngine.SnoozeOption.NEXT_IN_QUEUE, IconPack.Skip, "Next in queue"),
            )
        }

        // Show the snooze button for each snooze option
        val acePenalty = if (isAce) XpEngine.BONUS_ACE else 0
        snoozeOptions.forEach { (option, iconRes, label) ->
            ChooseSnoozeButton(
                icon = painterResource(iconRes),
                label = label,
                xpPenalty = option.penalty - acePenalty,
                onClick = { onSnooze(option) }
            )
        }

        // Info for manual snooze
        InfoCallout(
            title = "Manual Snooze",
            body = "Want to pick a specific next task? Tap it on the Kanban board and hit 'Focus now'!",
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
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
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
                    tint = LocalContentColor.current  // inherits color
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
                    shape = CircleShape,
                    accentColor = penaltyRed,
                    fontWeight = FontWeight.Light,
                    horizontalPadding = 8.dp
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
                icon      = painterResource(IconPack.DueSoon),
                label     = "Due soon",
                xpPenalty = XpEngine.SnoozeOption.BY_DUE_DATE.penalty
            )
            ChooseSnoozeButton(
                icon      = painterResource(IconPack.Skip),
                label     = "Next in queue",
                xpPenalty = XpEngine.SnoozeOption.NEXT_IN_QUEUE.penalty
            )
        }
    }
}
