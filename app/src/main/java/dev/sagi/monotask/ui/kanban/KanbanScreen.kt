package dev.sagi.monotask.ui.kanban

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import dev.sagi.monotask.ui.navigation.Screen
import kotlinx.coroutines.flow.collectLatest
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.ui.component.core.SegmentedToggle
import dev.sagi.monotask.ui.component.task.EditTaskSheet
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.util.Constants
import java.util.Date

private const val COLUMN_STAGGER_MS = 80

@Composable
fun KanbanScreen(
    navController: NavHostController,
    kanbanVM: KanbanViewModel
) {
    val uiState     by kanbanVM.uiState.collectAsStateWithLifecycle()
    val editingTask by kanbanVM.editingTask.collectAsStateWithLifecycle()

    val onKanbanEvent: (KanbanEvent) -> Unit = remember { { kanbanVM.onEvent(it) } }

    LaunchedEffect(Unit) {
        kanbanVM.uiEffect.collectLatest { effect ->
            when (effect) {
                is KanbanUiEffect.NavigateToFocus ->
                    navController.navigate(Screen.Focus.route)
                is KanbanUiEffect.ShowError -> { /* handled elsewhere if needed */ }
            }
        }
    }

    KanbanScreenContent(
        uiState       = uiState,
        onKanbanEvent = onKanbanEvent
    )

    editingTask?.let { task ->
        EditTaskSheet(
            task      = task,
            onDismiss = { onKanbanEvent(KanbanEvent.DismissEditSheet) },
            onSave    = { title, desc, importance, tags, dueDate ->
                onKanbanEvent(KanbanEvent.UpdateTask(task.copy(
                    title       = title,
                    description = desc,
                    importance  = importance,
                    tags        = tags,
                    dueDate     = dueDate?.let { Timestamp(Date(it)) }
                )))
                onKanbanEvent(KanbanEvent.DismissEditSheet)
            },
            onDelete = { onKanbanEvent(KanbanEvent.DeleteTask(task.id)) }
        )
    }
}

@Composable
fun KanbanScreenContent(
    uiState: KanbanUiState,
    onKanbanEvent: (KanbanEvent) -> Unit
) {
    val innerPadding = LocalScaffoldPadding.current

    var lastReady by remember { mutableStateOf<KanbanUiState.Ready?>(null) }
    val displayState = (uiState as? KanbanUiState.Ready)?.also { lastReady = it } ?: lastReady

    Column(
        modifier = Modifier
            .fillMaxSize()
            // No horizontal padding, for smooth horizontal scrolling
            .padding(
                top    = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
    ),
        // gap between toggle and content
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SegmentedToggle(
            options          = listOf("Active", "Archive"),
            selectedIndex    = if (uiState is KanbanUiState.Ready && uiState.isArchive) 1 else 0,
            onOptionSelected = { onKanbanEvent(KanbanEvent.ToggleArchive) },
            modifier         = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = Constants.Theme.SCREEN_PADDING / 2)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = Constants.Theme.SCREEN_PADDING)
            ,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KanbanColumn(
                title             = "High",
                importance        = Importance.HIGH,
                tasks             = displayState?.highTasks   ?: emptyList(),
                isArchive         = displayState?.isArchive   ?: false,
                onKanbanEvent     = onKanbanEvent,
                animationDelayMs  = 0 * COLUMN_STAGGER_MS
            )
            KanbanColumn(
                title             = "Medium",
                importance        = Importance.MEDIUM,
                tasks             = displayState?.mediumTasks ?: emptyList(),
                isArchive         = displayState?.isArchive   ?: false,
                onKanbanEvent     = onKanbanEvent,
                animationDelayMs  = 1 * COLUMN_STAGGER_MS
            )
            KanbanColumn(
                title             = "Low",
                importance        = Importance.LOW,
                tasks             = displayState?.lowTasks    ?: emptyList(),
                isArchive         = displayState?.isArchive   ?: false,
                onKanbanEvent     = onKanbanEvent,
                animationDelayMs  = 2 * COLUMN_STAGGER_MS
            )
        }
    }
}
