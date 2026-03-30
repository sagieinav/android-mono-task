package dev.sagi.monotask.ui.kanban

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import dev.sagi.monotask.ui.component.core.GlassChip
import dev.sagi.monotask.ui.component.core.GlassDropdownItem
import dev.sagi.monotask.ui.component.core.GlassDropdownMenu
import dev.sagi.monotask.ui.component.core.SegmentedToggle
import dev.sagi.monotask.ui.component.task.EditTaskSheet
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.LocalSnackbarHostState
import dev.sagi.monotask.util.Constants
import dev.sagi.monotask.util.Constants.Theme.KANBAN_PADDING
import dev.sagi.monotask.util.Constants.Theme.SCREEN_PADDING
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import dev.sagi.monotask.ui.component.core.DropdownTriggerPill
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.theme.monoShadowWorkaround
import java.util.Date

private const val COLUMN_STAGGER_MS = 80

private fun sortLabel(order: SortOrder) = when (order) {
    SortOrder.DUE_ASC      -> "Due date  ↑"
    SortOrder.DUE_DESC     -> "Due date  ↓"
    SortOrder.CREATED_ASC  -> "Created  ↑"
    SortOrder.CREATED_DESC -> "Created  ↓"
}

@Composable
fun KanbanScreen(
    navController: NavHostController,
    kanbanVM: KanbanViewModel
) {
    val uiState           by kanbanVM.uiState.collectAsStateWithLifecycle()
    val editingTask       by kanbanVM.editingTask.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    val onKanbanEvent: (KanbanEvent) -> Unit = remember { { kanbanVM.onEvent(it) } }

    LaunchedEffect(Unit) { onKanbanEvent(KanbanEvent.ResetArchive) }

    LaunchedEffect(Unit) {
        kanbanVM.uiEffect.collectLatest { effect ->
            when (effect) {
                is KanbanUiEffect.NavigateToFocus ->
                    navController.navigate(Screen.Focus.route)
                is KanbanUiEffect.ShowError ->
                    snackbarHostState.showSnackbar(
                        message           = effect.message,
                        withDismissAction = true,
                        duration          = SnackbarDuration.Short
                    )
            }
        }
    }

    KanbanScreenContent(
        uiState              = uiState,
        onKanbanEvent        = onKanbanEvent,
        onSortOrderChanged   = kanbanVM::setSortOrder
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
            }
        )
    }
}

@Composable
fun KanbanScreenContent(
    uiState: KanbanUiState,
    onKanbanEvent: (KanbanEvent) -> Unit,
    onSortOrderChanged: (SortOrder) -> Unit
) {
    val innerPadding = LocalScaffoldPadding.current

    // Cache last Ready so Loading doesn't flash empty content
    var lastReady        by remember { mutableStateOf<KanbanUiState.Ready?>(null) }
    var showSortDropdown by remember { mutableStateOf(false) }
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
                    imgRes        = R.drawable.img_empty_hyperfocus_yellow,
                    title         = "Hyperfocusing",
                    subtitle      = "Kanban's locked. Stay in the zone.",
                    isMainContent = true,
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
                        top    = innerPadding.calculateTopPadding() + SCREEN_PADDING,
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                // gap between control row and content
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // ========== Control row ==========
                val controlRowHeight = 44.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    SegmentedToggle(
                        options          = listOf("Active", "Archive"),
                        selectedIndex    = if (ready.isArchive) 1 else 0,
                        onOptionSelected = { index ->
                            if ((index == 1) != ready.isArchive) onKanbanEvent(KanbanEvent.ToggleArchive)
                        },
                        modifier = Modifier.height(controlRowHeight)
                    )

                    Box {
                        GlassSurface(
                            blurred = false,
                            shape = CircleShape,
                            modifier = Modifier
                                .height(Constants.Theme.TOP_BAR_ITEM_HEIGHT)
                                .monoShadowWorkaround(CircleShape)
                                .clickable(onClick = { showSortDropdown = true }),
                            baseColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .align(Alignment.Center)
                                ,
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val color = MaterialTheme.colorScheme.onSurfaceVariant
                                Text(
                                    text = sortLabel(ready.sortOrder),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = color,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
//                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .widthIn(max = 140.dp)
                                )
                            }
                        }
                        GlassDropdownMenu(
                            expanded  = showSortDropdown,
                            onDismiss = { showSortDropdown = false }
                        ) {
                            SortOrder.entries.forEach { order ->
                                GlassDropdownItem(
                                    label    = sortLabel(order),
                                    selected = ready.sortOrder == order,
                                    onClick  = {
                                        onSortOrderChanged(order)
                                        showSortDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ========== Content area ==========
                if (ready.highTasks.isEmpty() && ready.mediumTasks.isEmpty() && ready.lowTasks.isEmpty()) {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            imgRes        = R.drawable.img_empty_kanban_yellow,
                            title         = "Nothing here yet",
                            subtitle      = "Add a task and get the ball rolling.",
                            isMainContent = true,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = Constants.Theme.SCREEN_PADDING),
                        horizontalArrangement = Arrangement.spacedBy(KANBAN_PADDING)
                    ) {
                        KanbanColumn(
                            title            = "High",
                            importance       = Importance.HIGH,
                            tasks            = ready.highTasks,
                            isArchive        = ready.isArchive,
                            onKanbanEvent    = onKanbanEvent,
                            animationDelayMs = 0
                        )
                        KanbanColumn(
                            title            = "Medium",
                            importance       = Importance.MEDIUM,
                            tasks            = ready.mediumTasks,
                            isArchive        = ready.isArchive,
                            onKanbanEvent    = onKanbanEvent,
                            animationDelayMs = 1 * COLUMN_STAGGER_MS
                        )
                        KanbanColumn(
                            title            = "Low",
                            importance       = Importance.LOW,
                            tasks            = ready.lowTasks,
                            isArchive        = ready.isArchive,
                            onKanbanEvent    = onKanbanEvent,
                            animationDelayMs = 2 * COLUMN_STAGGER_MS
                        )
                    }
                }
            }
        }
    }
}
