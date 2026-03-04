package dev.sagi.monotask.ui.kanban

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.component.*
import dev.sagi.monotask.ui.shared.SharedWorkspaceViewModel
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding

@Composable
fun KanbanScreen(
    navController: NavHostController,
    sharedWorkspaceVM: SharedWorkspaceViewModel,
    viewModel: KanbanViewModel = viewModel()
) {
    val selectedWorkspace by sharedWorkspaceVM.selectedWorkspace.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startObservingTasks(sharedWorkspaceVM.selectedWorkspace)
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
        if (task.id.isBlank()) {
            CreateTaskSheet(
                onDismiss = { viewModel.dismissEditSheet() },
                onAddTask = { title, desc, importance, tags, dueDate ->
                    viewModel.addTask(
                        title, desc, importance,
                        dueDate?.let { com.google.firebase.Timestamp(java.util.Date(it)) },
                        tags, selectedWorkspace?.id ?: ""
                    )
                    viewModel.dismissEditSheet()
                }
            )
        } else {
            EditTaskSheet(
                task = task,
                onDismiss = { viewModel.dismissEditSheet() },
                onSave = { title, desc, importance, tags, dueDate ->
                    viewModel.updateTask(
                        task.copy(
                        title = title, description = desc, importance = importance, tags = tags,
                        dueDate = dueDate?.let { com.google.firebase.Timestamp(java.util.Date(it)) }
                    ))
                    viewModel.dismissEditSheet()
                },
                onDelete = { viewModel.deleteTask(task.id) }
            )
        }
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
            .padding(top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding())
    ) {
        ActiveArchiveToggle(
            showCompleted = showCompleted,
            onToggle = onToggleArchive,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KanbanColumn("High",   uiState.highTasks,   onTaskClick)
                    KanbanColumn("Medium", uiState.mediumTasks, onTaskClick)
                    KanbanColumn("Low",    uiState.lowTasks,    onTaskClick)
                }
            }
        }
    }
}
