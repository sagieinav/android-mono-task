package dev.sagi.monotask.ui.statistics

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.display.DonutItem
import dev.sagi.monotask.ui.component.display.DonutRowCard
import dev.sagi.monotask.ui.component.display.DonutSegment
import dev.sagi.monotask.ui.theme.customColors

// ========== CompletionDonutCard ==========
// Domain wrapper that builds importance + workspace segments from task data.
// Call site: CompletionDonutCard(tasks = state.completedTasks, workspaces = state.workspaces)

@Composable
fun CompletionDonutCard(
    tasks: List<Task>,
    workspaces: List<Workspace>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.customColors

    val importanceSegments = remember(tasks, colors) {
        listOf(
            DonutSegment(
                "High",
                tasks.count { it.importance == Importance.HIGH }.toFloat(),
                colors.importanceHighContent
            ),
            DonutSegment(
                "Medium",
                tasks.count { it.importance == Importance.MEDIUM }.toFloat(),
                colors.importanceMediumContent
            ),
            DonutSegment(
                "Low",
                tasks.count { it.importance == Importance.LOW }.toFloat(),
                colors.importanceLowContent
            ),
        ).filter { it.value > 0f }
    }

    val workspaceSegments = remember(tasks, workspaces) {
        val wsMap = workspaces.associateBy { it.id }
        tasks
            .groupBy { it.workspaceId }
            .map { (wsId, wsTasks) ->
                DonutSegment(
                    label = wsMap[wsId]?.name ?: "Other",
                    value = wsTasks.size.toFloat(),
                )
            }
            .sortedByDescending { it.value }
    }

    DonutRowCard(title = "Task Completion Distribution", modifier = modifier) {
        DonutItem(title = "Importance", segments = importanceSegments)
        DonutItem(title = "Workspace", segments = workspaceSegments)
    }
}
