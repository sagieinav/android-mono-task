package dev.sagi.monotask.ui.kanban

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.component.core.CustomTag
import dev.sagi.monotask.ui.component.core.DueDateLabel
import dev.sagi.monotask.ui.component.core.TagSize
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.invincibleBorder
import dev.sagi.monotask.ui.theme.monoShadow


@Composable
fun KanbanCard(
    task: Task,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardShape = MaterialTheme.shapes.small
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
//            .monoBorder(cardShape)
            .monoShadow(cardShape)
            .invincibleBorder(cardShape),
        shape = cardShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ========== Title ==========
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ========== Tags ==========
            if (task.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    task.tags.forEach { tag ->
                        CustomTag(size = TagSize.SMALL, label = tag)
                    }
                }
            }

            // ========== Due date ==========
            task.dueDate?.let {
                DueDateLabel(timestamp = it, small = true)
            }
        }
    }
}

// ========== Preview ==========
@Preview(showBackground = true)
@Composable
fun KanbanCardPreview() {
    MonoTaskTheme {
        Column(
            modifier = Modifier
//                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KanbanCard(
                task = Task(
                    id = "1",
                    title = "Submit Final Report",
                    tags = listOf("Urgent"),
                    importance = Importance.HIGH,
                    dueDate = Timestamp.now()
                )
            )
            KanbanCard(
                task = Task(
                    id = "2",
                    title = "Client Meeting",
                    tags = listOf("internal"),
                    importance = Importance.MEDIUM,
                )
            )
            KanbanCard(
                task = Task(
                    id = "3",
                    title = "Read Industry News",
                    importance = Importance.LOW,
                    dueDate = Timestamp.now()
                )
            )
        }
    }
}
