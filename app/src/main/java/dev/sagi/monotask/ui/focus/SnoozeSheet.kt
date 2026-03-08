package dev.sagi.monotask.ui.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.penaltyRed
import androidx.compose.ui.text.style.TextAlign
import dev.sagi.monotask.domain.util.XpEvents
import dev.sagi.monotask.ui.component.core.ActionButton
import dev.sagi.monotask.ui.component.core.BottomSheet
import dev.sagi.monotask.ui.theme.monoBorder
import dev.sagi.monotask.ui.theme.monoShadow

@Composable
fun SnoozeBottomSheet(
    onDismissRequest: () -> Unit,
    onSnooze: (penalty: Int) -> Unit
) {
    BottomSheet(
        title = "Snooze Task",
        onDismissRequest = onDismissRequest
    ) {
        Text(
//            "Choose your next task",
            "CHOOSE YOUR NEXT TASK",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),

            modifier = Modifier.fillMaxWidth()
        )

        ChooseSnoozeButton(
            icon = painterResource(R.drawable.ic_due_soon),
            label = "Due Soon",
            xpCost = XpEvents.SNOOZE_DUE_SOON,
            onClick = { onSnooze(XpEvents.SNOOZE_DUE_SOON) }
        )
        ChooseSnoozeButton(
            icon = painterResource(R.drawable.ic_skip),
            label = "Next in Queue",
            xpCost = XpEvents.SNOOZE_NEXT,
            onClick = { onSnooze(XpEvents.SNOOZE_NEXT) }
        )
        ChooseSnoozeButton(
            icon = painterResource(R.drawable.ic_view_kanban),
            label = "Choose Manually",
            xpCost = XpEvents.SNOOZE_MANUAL,
            onClick = { onSnooze(XpEvents.SNOOZE_MANUAL) }
        )
    }
}


@Composable
fun ChooseSnoozeButton(
    icon: Painter,
    label: String,
    xpCost: Int? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
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
            if (xpCost != null) {
                Surface(
                    shape = RoundedCornerShape(100),
                    color = penaltyRed.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$xpCost XP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = penaltyRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

