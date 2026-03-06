package dev.sagi.monotask.ui.kanban

import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.basicMonoTask
import dev.sagi.monotask.ui.theme.customColors
import dev.sagi.monotask.ui.theme.harabara
import dev.sagi.monotask.ui.theme.monoBorder
import dev.sagi.monotask.ui.theme.monoShadow


@Composable
fun KanbanColumn(
    title: String,
    importance: Importance,
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.customColors
    val (containerColor, contentColor) = when (importance) {
        Importance.HIGH   -> colors.importanceHighBackground   to colors.importanceHighContent
        Importance.MEDIUM -> colors.importanceMediumBackground to colors.importanceMediumContent
        Importance.LOW    -> colors.importanceLowBackground    to colors.importanceLowContent
    }

    Column(
        modifier = modifier
            .width(180.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(6.dp) // gap between header and body
    ) {
        val cardShape = MaterialTheme.shapes.medium
        // ========== Header Surface ==========
        GlassSurface(
            blurred = false,
            shape = cardShape,
            modifier = modifier
    //            .fillMaxWidth()
    ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Proportional,
                            trim = LineHeightStyle.Trim.Both
                        )
                    ),
                    fontFamily = harabara,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(100),
                    color = containerColor.copy(alpha = 0.35f),
                ) {
                    Text(
                        text = tasks.size.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // ========== Body Surface ==========
        GlassSurface(
            blurred = false,
            shape = cardShape,
            modifier = modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)  // Room for last card's shadow
            ) {
                items(tasks, key = { it.id }) { task ->
                    KanbanCard(task = task, onClick = { onTaskClick(task) })
                }
            }
        }
    }
}


@Preview(heightDp = 600)
@Composable
fun KanbanColumnPreview() {
    MonoTaskTheme {
        val fakeTasks = listOf(
            Task(id = "1", title = "Fix login crash", importance = Importance.HIGH,   tags = listOf("auth", "bug")),
            Task(id = "2", title = "Write unit tests", importance = Importance.MEDIUM, tags = listOf("testing")),
            Task(id = "3", title = "Update README",   importance = Importance.LOW,    tags = listOf("docs")),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            KanbanColumn(
                title = "High",
                tasks = fakeTasks.filter { it.importance == Importance.HIGH },
                onTaskClick = {},
                importance = Importance.HIGH
            )
            KanbanColumn(
                title = "Medium",
                tasks = fakeTasks.filter { it.importance == Importance.MEDIUM },
                onTaskClick = {},
                importance = Importance.MEDIUM
            )
            KanbanColumn(
                title = "Low",
                tasks = fakeTasks.filter { it.importance == Importance.LOW },
                onTaskClick = {},
                importance = Importance.LOW
            )
        }
    }
}
