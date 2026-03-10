package dev.sagi.monotask.ui.kanban

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.component.core.SegmentedToggle
import dev.sagi.monotask.ui.component.task.EditTaskSheet
import dev.sagi.monotask.ui.shared.WorkspaceViewModel
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import java.util.Date

private const val COLUMN_STAGGER_MS = 300

@Composable
fun KanbanScreen(
    navController: NavHostController,
    workspaceVM: WorkspaceViewModel,
    kanbanVM: KanbanViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        kanbanVM.startObservingTasks(workspaceVM.selectedWorkspace)
    }

    val uiState    by kanbanVM.uiState.collectAsState()
    val editingTask by kanbanVM.editingTask.collectAsState()

    KanbanScreenContent(
        uiState        = uiState,
        onToggleArchive = { kanbanVM.toggleArchive() },
        onTaskClick     = { kanbanVM.openEditSheet(it) }
    )

    editingTask?.let { task ->
        EditTaskSheet(
            task      = task,
            onDismiss = { kanbanVM.dismissEditSheet() },
            onSave    = { title, desc, importance, tags, dueDate ->
                kanbanVM.updateTask(task.copy(
                    title       = title,
                    description = desc,
                    importance  = importance,
                    tags        = tags,
                    dueDate     = dueDate?.let { Timestamp(Date(it)) }
                ))
                kanbanVM.dismissEditSheet()
            },
            onDelete = { kanbanVM.deleteTask(task.id) }
        )
    }
}

@Composable
fun KanbanScreenContent(
    uiState: KanbanUiState,
    onToggleArchive: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val innerPadding = LocalScaffoldPadding.current

    // Hold last Ready so columns keep their content visible during Loading
    var lastReady by remember { mutableStateOf<KanbanUiState.Ready?>(null) }
    val displayState = (uiState as? KanbanUiState.Ready)?.also { lastReady = it } ?: lastReady

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                top    = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ========== Active / Archive toggle ==========
        SegmentedToggle(
            options          = listOf("Active", "Archive"),
            selectedIndex    = if (uiState is KanbanUiState.Ready && uiState.isArchive) 1 else 0,
            onOptionSelected = { onToggleArchive() },
            modifier         = Modifier.align(Alignment.CenterHorizontally)
        )

        // ========== Kanban columns ==========
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KanbanColumn(
                title             = "High",
                importance        = Importance.HIGH,
                tasks             = displayState?.highTasks   ?: emptyList(),
                isArchive         = displayState?.isArchive   ?: false,
                onTaskClick       = onTaskClick,
                animationDelayMs  = 0 * COLUMN_STAGGER_MS
            )
            KanbanColumn(
                title             = "Medium",
                importance        = Importance.MEDIUM,
                tasks             = displayState?.mediumTasks ?: emptyList(),
                isArchive         = displayState?.isArchive   ?: false,
                onTaskClick       = onTaskClick,
                animationDelayMs  = 1 * COLUMN_STAGGER_MS
            )
            KanbanColumn(
                title             = "Low",
                importance        = Importance.LOW,
                tasks             = displayState?.lowTasks    ?: emptyList(),
                isArchive         = displayState?.isArchive   ?: false,
                onTaskClick       = onTaskClick,
                animationDelayMs  = 2 * COLUMN_STAGGER_MS
            )
        }
    }
}
