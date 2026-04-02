package dev.sagi.monotask.ui.brief

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.component.display.CountBadge
import dev.sagi.monotask.ui.focus.UserHeader
import dev.sagi.monotask.ui.theme.LocalCustomColors
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.util.toRelativeDate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.util.Constants.Theme.SCREEN_PADDING

@Composable
fun BriefScreen(briefVM: BriefViewModel) {
    val uiState by briefVM.uiState.collectAsStateWithLifecycle()
    BriefContent(uiState = uiState)
}

@Composable
private fun BriefContent(uiState: BriefUiState) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is BriefUiState.Loading -> Unit
            is BriefUiState.Ready   -> ReadyContent(state = uiState)
        }
    }
}

@Composable
private fun ReadyContent(state: BriefUiState.Ready) {
    val scaffoldPadding = LocalScaffoldPadding.current
    val density = LocalDensity.current

    val briefStatus = when {
        state.overdueTasks.isNotEmpty()     -> BriefStatus.OVERDUE
        state.dueTodayTasks.isNotEmpty()    -> BriefStatus.ON_TRACK
        else                                -> BriefStatus.ALL_CLEAR
    }

    var briefStatusAnimated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { briefStatusAnimated = true }

    var overdueExpanded  by remember { mutableStateOf(false) }
    var dueTodayExpanded by remember { mutableStateOf(false) }
    val anyExpanded = overdueExpanded || dueTodayExpanded

    // Fill the remaining viewport below the sections so the illustration is centered there.
    // Illustration item is at index 3. When expanded, remainingPx ≤ 0 → illustration hidden.
    val listState = rememberLazyListState()
    val illustrationHeightDp by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val illustrationItem = info.visibleItemsInfo.find { it.index == 3 }
                ?: return@derivedStateOf 0.dp
            val remainingPx = maxOf(
                info.viewportEndOffset - info.afterContentPadding - illustrationItem.offset,
                0
            )
            with(density) { remainingPx.toDp() }
        }
    }

    LazyColumn(
        state               = listState,
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = SCREEN_PADDING),
        contentPadding      = PaddingValues(
            top    = scaffoldPadding.calculateTopPadding(),
            bottom = scaffoldPadding.calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {  // index 0
            UserHeader(
                user          = state.user,
                currentStreak = state.user?.stats?.currentStreak ?: 0
            )
            Text(
                text      = "all workspaces · ${state.pendingCount} active tasks",
                style     = MaterialTheme.typography.titleSmall,
                color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
        }

        item {  // index 1
            ExpandableTaskSection(
                label          = "Overdue",
                iconRes        = R.drawable.ic_due_soon,
                tasks          = state.overdueTasks,
                workspaceNames = state.workspaceNames,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor   = MaterialTheme.colorScheme.error,
                expanded       = overdueExpanded,
                onExpandChange = { overdueExpanded = it }
            )
        }

        item {  // index 2
            ExpandableTaskSection(
                label          = "Due Today",
                iconRes        = R.drawable.ic_due_calendar,
                tasks          = state.dueTodayTasks,
                workspaceNames = state.workspaceNames,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.primary,
                expanded       = dueTodayExpanded,
                onExpandChange = { dueTodayExpanded = it }
            )
        }

        item {  // index 3: illustration, fills remaining height
            if (!anyExpanded && illustrationHeightDp > 0.dp) {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(illustrationHeightDp),
                    contentAlignment = Alignment.Center
                ) {
                    BriefStatus(
                        status  = briefStatus,
                        animate = !briefStatusAnimated
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableTaskSection(
    label: String,
    iconRes: Int,
    tasks: List<Task>,
    workspaceNames: Map<String, String>,
    containerColor: Color,
    contentColor: Color,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label       = "chevron_$label"
    )

    GlassSurface(
        shape    = MaterialTheme.shapes.medium,
        blurred  = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable { onExpandChange(!expanded) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
//                    Icon(
//                        painter            = painterResource(iconRes),
//                        contentDescription = null,
//                        modifier           = Modifier.size(18.dp),
//                    )
                    Text(
                        text       = label,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CountBadge(
                        count = tasks.size,
                        color = contentColor
                    )
                    Icon(
                        painter            = painterResource(R.drawable.ic_chevron),
                        contentDescription = null,
                        modifier           = Modifier
                            .size(18.dp)
                            .rotate(chevronRotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded task list
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (tasks.isEmpty()) {
                        Text(
                            text  = "No tasks here",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        tasks.forEach { task ->
                            TaskBriefRow(task = task, workspaceNames = workspaceNames)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskBriefRow(task: Task, workspaceNames: Map<String, String>) {
    val customColors  = LocalCustomColors.current
    val subtleColor   = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    val importanceColor = when (task.importance) {
        Importance.HIGH   -> customColors.importanceHighContent
        Importance.MEDIUM -> customColors.importanceMediumContent
        Importance.LOW    -> customColors.importanceLowContent
    }

    val workspaceName = workspaceNames[task.workspaceId] ?: task.workspaceId
    val dueDateStr    = task.dueDate?.toRelativeDate()?.text

    val subtitleParts = buildList {
        add(workspaceName)
        if (dueDateStr != null) add(dueDateStr)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Left border accent
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .padding(top = 4.dp, bottom = 2.dp)
                .clip(CircleShape)
                .glassBackground(baseColor = importanceColor)
        )
        Spacer(Modifier.width(10.dp))
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text     = task.title,
                style    = MaterialTheme.typography.titleSmall,
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text     = subtitleParts.joinToString(separator = "  ·  "),
                style    = MaterialTheme.typography.labelSmall,
                color    = subtleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ========== Preview ==========

private val previewReadyState = BriefUiState.Ready(
    overdueTasks  = listOf(
        Task(id = "1", title = "Submit assignment", importance = Importance.HIGH, workspaceId = "study"),
        Task(id = "2", title = "Pay electricity bill", importance = Importance.MEDIUM, workspaceId = "personal"),
    ),
    dueTodayTasks = listOf(
        Task(id = "3", title = "Team standup notes", importance = Importance.LOW, workspaceId = "work"),
    ),
    pendingCount   = 12,
    workspaceNames = mapOf("study" to "Study", "personal" to "Personal", "work" to "Work"),
    user           = User(id = "u1", displayName = "Sagi")
)

@Preview(showSystemUi = true, name = "BriefScreen: Ready")
@Composable
private fun BriefScreenPreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            BriefContent(uiState = previewReadyState)
        }
    }
}
