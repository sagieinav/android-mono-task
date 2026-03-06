@file:OptIn(ExperimentalLayoutApi::class)

package dev.sagi.monotask.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.component.core.CustomTag
import dev.sagi.monotask.ui.component.core.DueDateLabel
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.component.core.ImportanceTag
import dev.sagi.monotask.ui.theme.LocalHazeState
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.aceTaskBorder
import dev.sagi.monotask.ui.theme.defaultTaskBorder
import dev.sagi.monotask.util.ext.toRelativeDate

@Composable
fun FocusCard(
    task: Task,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.large

    GlassSurface(
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .then(
                if (task.isAce) Modifier.aceTaskBorder(shape)
                else Modifier.defaultTaskBorder(shape)
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            FocusCardHeader(task)
            FocusCardBody(task)
            FocusCardFooter(task)
        }
    }
}

// ========== Header: Due date (left) + XP (right) ==========

@Composable
private fun FocusCardHeader(task: Task) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        task.dueDate?.let { DueDateLabel(it) } ?: Spacer(Modifier) // keeps XP pinned right
        XpLabelCurrent(xp = task.currentXp)
    }
}

// ========== Body: Title + Description ==========

@Composable
private fun FocusCardBody(task: Task) {
    Column(verticalArrangement = Arrangement.Center) {
        val titleWrapped = task.title
            .replace("-", "-\u200B")
            .replace("—", "—\u200B")

        Text(
            text = titleWrapped,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        if (task.description.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ========== Footer: Tags ==========

@Composable
private fun FocusCardFooter(task: Task) {
    val maxVisible = 4
    val visibleTags = task.tags.take(maxVisible)
    val overflowCount = task.tags.size - maxVisible

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
        maxLines = 2,
    ) {
        ImportanceTag(importance = task.importance)
        visibleTags.forEach { CustomTag(label = it) }
        if (overflowCount > 0) CustomTag(label = "+$overflowCount")
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
