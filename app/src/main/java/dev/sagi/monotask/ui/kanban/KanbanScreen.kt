package dev.sagi.monotask.ui.kanban

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.display.EmptyState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import dev.sagi.monotask.ui.navigation.Screen
import kotlinx.coroutines.flow.collectLatest
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.ui.component.core.SegmentedToggle
import dev.sagi.monotask.ui.component.task.EditTaskSheet
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.LocalSnackbarHostState
import dev.sagi.monotask.util.Constants.Theme.KANBAN_PADDING
import dev.sagi.monotask.util.Constants.Theme.SCREEN_PADDING
import androidx.compose.material3.SnackbarDuration
import dev.sagi.monotask.ui.component.display.IllustrationSize
import dev.sagi.monotask.util.Constants.Theme.TOP_BAR_ITEM_HEIGHT
import java.util.Date

private const val COLUMN_STAGGER_MS = 80


@Composable
fun KanbanScreen(
    navController: NavHostController,
    kanbanVM: KanbanViewModel
) {
    val uiState by kanbanVM.uiState.collectAsStateWithLifecycle()
    val editingTask by kanbanVM.editingTask.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    val onKanbanEvent: (KanbanEvent) -> Unit = remember { { kanbanVM.onEvent(it) } }

    LaunchedEffect(Unit) { onKanbanEvent(KanbanEvent.ResetArchive) }

    LaunchedEffect(Unit) {
        kanbanVM.effect.collectLatest { effect ->
            when (effect) {
                is KanbanUiEffect.NavigateToFocus ->
                    navController.navigate(Screen.Focus.route)
                is KanbanUiEffect.ShowError ->
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
            }
        }
    }

    KanbanScreenContent(
        uiState = uiState,
        onKanbanEvent = onKanbanEvent
    )

    editingTask?.let { task ->
        EditTaskSheet(
            task      = task,
            onDismiss = { onKanbanEvent(KanbanEvent.DismissEditSheet) },
            onSave    = { title, desc, importance, tags, dueDate ->
                onKanbanEvent(KanbanEvent.UpdateTask(task.copy(
                    title = title,
                    description = desc,
                    importance = importance,
                    tags = tags,
                    dueDate = dueDate?.let { Timestamp(Date(it)) }
                )))
                onKanbanEvent(KanbanEvent.DismissEditSheet)
            }
        )
    }
}

@Composable
fun KanbanScreenContent(
    uiState: KanbanUiState,
    onKanbanEvent: (KanbanEvent) -> Unit
) {
    val innerPadding = LocalScaffoldPadding.current

    // Cache last Ready so Loading doesn't flash empty content
    var lastReady by remember { mutableStateOf<KanbanUiState.Ready?>(null) }
    if (uiState is KanbanUiState.Ready) lastReady = uiState
    val ready = lastReady

    when {
        uiState is KanbanUiState.Locked -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top    = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    imgRes = R.drawable.img_empty_hyperfocus,
                    title = "Hyperfocusing",
                    subtitle = "Kanban's locked. Stay in the zone.",
                    size = IllustrationSize.Large,
                )
            }
        }

        ready == null -> { /* Initial load: nothing to show yet */ }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // No horizontal padding on the Column, for smooth horizontal scrolling
                    .padding(
                        top = innerPadding.calculateTopPadding() + SCREEN_PADDING,
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                // gap between control row and content
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Active/Archive Toggle
                SegmentedToggle(
                    options = listOf("Active", "Archive"),
                    selectedIndex = if (ready.isArchive) 1 else 0,
                    onOptionSelected = { index ->
                        if ((index == 1) != ready.isArchive) onKanbanEvent(KanbanEvent.ToggleArchive)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(TOP_BAR_ITEM_HEIGHT)
                )

                // ========== Content area ==========
                if (ready.highTasks.isEmpty() && ready.mediumTasks.isEmpty() && ready.lowTasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            imgRes = R.drawable.img_empty_kanban,
                            title = "Nothing here yet",
                            subtitle = "Add a task and get the ball rolling.",
                            size = IllustrationSize.Large,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = SCREEN_PADDING),
                        horizontalArrangement = Arrangement.spacedBy(KANBAN_PADDING)
                    ) {
                        KanbanColumn(
                            title = "High",
                            importance = Importance.HIGH,
                            tasks = ready.highTasks,
                            isArchive = ready.isArchive,
                            onKanbanEvent = onKanbanEvent,
                            animationDelayMs = 0
                        )
                        KanbanColumn(
                            title = "Medium",
                            importance = Importance.MEDIUM,
                            tasks = ready.mediumTasks,
                            isArchive = ready.isArchive,
                            onKanbanEvent = onKanbanEvent,
                            animationDelayMs = 1 * COLUMN_STAGGER_MS
                        )
                        KanbanColumn(
                            title = "Low",
                            importance = Importance.LOW,
                            tasks = ready.lowTasks,
                            isArchive = ready.isArchive,
                            onKanbanEvent = onKanbanEvent,
                            animationDelayMs = 2 * COLUMN_STAGGER_MS
                        )
                    }
                }
            }
        }
    }
}


