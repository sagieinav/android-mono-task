package dev.sagi.monotask.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R

@Composable
fun SnoozeBottomSheet(
    onDismissRequest: () -> Unit,
    onSnooze: (penalty: Int) -> Unit
) {
    BottomSheet(
        title = "Snooze Task",
        onDismissRequest = onDismissRequest
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Choose your next task",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            SnoozePenaltyRow(
                icon = painterResource(R.drawable.ic_due_soon),
                label = "Due Soon",
                xpCost = XpEvents.SNOOZE_DUE_SOON,
                onClick = { onSnooze(XpEvents.SNOOZE_DUE_SOON) }
            )
            SnoozePenaltyRow(
                icon = painterResource(R.drawable.ic_skip),
                label = "Next in Queue",
                xpCost = XpEvents.SNOOZE_NEXT,
                onClick = { onSnooze(XpEvents.SNOOZE_NEXT) }
            )
            SnoozePenaltyRow(
                icon = painterResource(R.drawable.ic_view_kanban),
                label = "Choose Manually",
                xpCost = XpEvents.SNOOZE_MANUAL,
                onClick = { onSnooze(XpEvents.SNOOZE_MANUAL) }
            )
        }
    }
}
