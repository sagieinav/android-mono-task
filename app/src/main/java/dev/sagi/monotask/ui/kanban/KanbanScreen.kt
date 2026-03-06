package dev.sagi.monotask.ui.kanban

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.component.core.SegmentedToggle
import dev.sagi.monotask.ui.component.task.EditTaskSheet
import dev.sagi.monotask.ui.shared.WorkspaceViewModel
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import java.util.Date

@Composable
fun KanbanScreen(
    navController: NavHostController,
    workspaceVM: WorkspaceViewModel,
    viewModel: KanbanViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.startObservingTasks(workspaceVM.selectedWorkspace)
    }

    val uiState by viewModel.uiState.collectAsState()
    val showCompleted by viewModel.showCompleted.collectAsState()
    val editingTask by viewModel.editingTask.collectAsState()

    KanbanScreenContent(
        uiState = uiState,
        showCompleted = showCompleted,
        onToggleArchive = { viewModel.toggleArchive() },
        onTaskClick = { viewModel.openEditSheet(it) }
    )

    editingTask?.let { task ->
        EditTaskSheet(
            task = task,
            onDismiss = { viewModel.dismissEditSheet() },
            onSave = { title, desc, importance, tags, dueDate ->
                viewModel.updateTask(
                    task.copy(
                    title = title, description = desc, importance = importance, tags = tags,
                    dueDate = dueDate?.let { Timestamp(Date(it)) }
                ))
                viewModel.dismissEditSheet()
            },
            onDelete = { viewModel.deleteTask(task.id) }
        )
    }

}

@Composable
fun KanbanScreenContent(
    uiState: KanbanUiState,
    showCompleted: Boolean,
    onToggleArchive: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val innerPadding = LocalScaffoldPadding.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Active/Archive Toggle
        SegmentedToggle(
            options = listOf("Active", "Archive"),
            selectedIndex = if (showCompleted) 1 else 0,
            onOptionSelected = { onToggleArchive() },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
//                .padding(vertical = 8.dp)
        )

        when (uiState) {
            is KanbanUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingSpinner() }
            }
            is KanbanUiState.Ready -> {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KanbanColumn(
                        title = "High",
                        importance = Importance.HIGH,
                        tasks = uiState.highTasks,
                        onTaskClick = onTaskClick
                    )
                    KanbanColumn(
                        title = "Medium",
                        importance = Importance.MEDIUM,
                        tasks = uiState.mediumTasks,
                        onTaskClick = onTaskClick
                    )
                    KanbanColumn(
                        title = "Low",
                        importance = Importance.LOW,
                        tasks = uiState.lowTasks,
                        onTaskClick = onTaskClick
                    )
                }
            }
        }
    }
}