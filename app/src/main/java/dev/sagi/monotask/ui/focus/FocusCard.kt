@file:OptIn(ExperimentalLayoutApi::class)

package dev.sagi.monotask.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.component.task.CustomTag
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.component.task.DueDateLabel
import dev.sagi.monotask.ui.component.task.ImportanceLabel
import dev.sagi.monotask.ui.component.task.XpChip
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.aceTaskBorder
import dev.sagi.monotask.ui.theme.defaultTaskBorder

// ========== FocusCard ==========
@Composable
fun FocusCard(
    task: Task,
    borderFraction: Float,
    modifier: Modifier = Modifier,
    hideXpLabel: Boolean = false
) {
    val shape       = MaterialTheme.shapes.large
    val borderWidth = 5.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = borderWidth / 2)
            .heightIn(min = 200.dp)
            .then(
                if (task.isAce) Modifier.aceTaskBorder(
                    shape         = shape,
                    drawFraction  = borderFraction,
                    borderWidth   = borderWidth
                )
                else Modifier.defaultTaskBorder(
                    shape         = shape,
                    drawFraction  = 2f,
                    borderWidth   = borderWidth
                )
            )
    ) {
        GlassSurface(
            shape    = shape,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
        ) {}

        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            FocusCardHeader(task = task, hideXpLabel = hideXpLabel)
            FocusCardBody(task)
            FocusCardFooter(task)
        }
    }
}


// ========== Header: XpChip + ImportanceChip ==========

@Composable
private fun FocusCardHeader(task: Task, hideXpLabel: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        if (!hideXpLabel) XpChip(xp = task.currentXp)
        ImportanceLabel(importance = task.importance)
        Spacer(Modifier.weight(1f))
        task.dueDate?.let { DueDateLabel(it) } ?: Spacer(Modifier)
    }
}


// ========== Body: Title + Description ==========

@Composable
private fun FocusCardBody(task: Task) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        val titleWrapped = task.title
            .replace("-", "-\u200B")
            .replace("—", "—\u200B")

        Text(
            text     = titleWrapped,
            style    = MaterialTheme.typography.displaySmall,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        if (task.description.isNotBlank()) {
            Text(
                text      = task.description,
                style     = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                maxLines  = 8,
                overflow  = TextOverflow.Ellipsis
            )
        }
    }
}


// ========== Footer: DueDateTag + Custom Tags ==========

@Composable
private fun FocusCardFooter(task: Task) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(6.dp),
        maxLines              = 2,
    ) {
//        task.dueDate?.let { DueDateTag(it) }
        task.tags.forEach { CustomTag(label = it) }
    }
}


// ========== Preview ==========

@Preview(showBackground = true)
@Composable
fun FocusCardPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.size(360.dp).padding(18.dp)) {
            FocusCard(
                task = Task(
                    id          = "1",
                    title       = "Build Swipe-to-Complete",
                    description = "Complete sections 3 and 4, submit before midnight.",
                    importance  = Importance.HIGH,
                    tags        = listOf("CS101", "ds"),
                    snoozeCount = 0,
                    dueDate     = Timestamp.now()
                ),
                borderFraction = 0.5f
            )
        }
    }
}
