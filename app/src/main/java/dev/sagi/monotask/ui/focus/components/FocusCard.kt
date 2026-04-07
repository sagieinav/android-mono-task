@file:OptIn(ExperimentalLayoutApi::class)

package dev.sagi.monotask.ui.focus.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.common.CustomTag
import dev.sagi.monotask.designsystem.components.GlassSurface
import dev.sagi.monotask.ui.common.DueDateLabel
import dev.sagi.monotask.ui.common.ImportanceLabel
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.customColors
import dev.sagi.monotask.designsystem.theme.outlineBorder
import dev.sagi.monotask.designsystem.theme.premiumBorder

@Composable
fun FocusCard(
    task: Task,
    entryProgressProvider: () -> Float,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.large
    val borderWidth = 5.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = borderWidth / 2) // compensate for external border
            .then(
                if (task.isAce) Modifier.premiumBorder(
                    shape = shape,
                    color = MaterialTheme.customColors.ace,
                    borderWidth = borderWidth,
                    entryProgressProvider = entryProgressProvider
                )
                else Modifier.outlineBorder(
                    shape = shape,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    borderWidth = borderWidth,
                    entryProgressProvider = entryProgressProvider
                )
            )
    ) {
        GlassSurface(
            shape = shape,
            shineAlpha = 0.9f,
            baseColor = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.heightIn(min = 220.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                FocusCardHeader(task = task)
                FocusCardBody(task)
                if (task.tags.isNotEmpty()) FocusCardFooter(task)
            }
        }
    }
}


// ========== Header: XpChip + ImportanceChip ==========
@Composable
private fun FocusCardHeader(task: Task) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XpLabel(xp = task.currentXp)
        ImportanceLabel(importance = task.importance)
        Spacer(Modifier.weight(1f))
        task.dueDate?.let { DueDateLabel(it) } ?: Spacer(Modifier)
    }
}


// ========== Body: Title + Description ==========
@Composable
private fun FocusCardBody(task: Task) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        if (task.description.isNotBlank()) {
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


// ========== Footer: DueDateTag + Custom Tags ==========
@Composable
private fun FocusCardFooter(task: Task) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        maxLines = 2,
    ) {
        task.tags.forEach { CustomTag(label = it) }
    }
}


// ========== Preview ==========
@Preview(showBackground = true)
@Composable
private fun FocusCardPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.size(360.dp).padding(18.dp)) {
            FocusCard(
                entryProgressProvider = { 1f },
                task = Task(
                    id = "1",
                    title = "Build Swipe-to-Complete",
                    description = "Complete sections 3 and 4, submit before midnight.",
                    importance = Importance.HIGH,
                    tags = listOf("CS101", "ds"),
                    snoozeCount = 0,
                    dueDate = Timestamp.now()
                )
            )
        }
    }
}
