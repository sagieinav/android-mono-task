package dev.sagi.monotask.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.core.MonoBottomSheet
import dev.sagi.monotask.ui.component.core.GlassSurface


private data class FaqItem(val question: String, val answer: String)

private val FAQ_ITEMS = listOf(
    FaqItem(
        question = "What is MonoTask?",
        answer   = "MonoTask shows you one task at a time to eliminate choice fatigue." +
                " Instead of staring at an endless list, the app picks what you should work on next, so you can just start."
    ),
    FaqItem(
        question = "How does task priority work?",
        answer   = "Tasks are ranked and queued by importance & due date proximity." +
                " You can tune the weights in Settings → 'Task priority calculation' to match your style."
    ),
    FaqItem(
        question = "What is an ACE task?",
        answer   = "An ACE is a task that has never been snoozed." +
                " Complete it without snoozing to earn bonus XP and show off that gold border!"
    ),
    FaqItem(
        question = "What happens when I snooze a task?",
        answer   = "Snoozing costs XP and moves the task back in the queue." +
                " Once snoozed, a task permanently loses its ACE status, so think before you snooze!"
    ),
    FaqItem(
        question = "What is Hyperfocus?",
        answer   = "Hyperfocus hides the Kanban view entirely, forcing you to focus on one task." +
                " Enable it when you want zero distractions."
    ),
    FaqItem(
        question = "How do XP and levels work?",
        answer   = "You earn XP by completing tasks." +
                " Leveling up gets progressively harder, so every task counts."
    ),
)


@Composable
fun FaqBottomSheet(onDismiss: () -> Unit) {
    MonoBottomSheet(title = "FAQ", onDismissRequest = onDismiss) {
        FAQ_ITEMS.forEach { item ->
            FaqItemRow(item = item)
        }
        Spacer(Modifier.height(4.dp))
    }
}


@Composable
private fun FaqItemRow(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f, label = "chevron")

    GlassSurface(
        shape = MaterialTheme.shapes.medium,
        blurred = false,
//        accentColor = MaterialTheme.colorScheme.outlineVariant,
//        baseColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text      = item.question,
                    style     = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color     = MaterialTheme.colorScheme.onSurface,
                    modifier  = Modifier.weight(1f)
                )
                Icon(
                    painter            = painterResource(R.drawable.ic_chevron),
                    contentDescription = null,
                    modifier           = Modifier
                        .padding(start = 8.dp)
                        .size(18.dp)
                        .rotate(chevronRotation),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Text(
                    text  = item.answer,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
